package com.example.ui.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.billing.BillingManager
import com.example.data.billing.BillingUiState
import com.example.data.db.AppDatabase
import com.example.data.db.ContractDao
import com.example.data.generator.ContractTemplateEngine
import com.example.data.model.ContractEntity
import com.example.data.model.ContractType
import com.example.data.model.FormField
import com.example.data.pdf.PdfExporter
import com.example.data.security.CryptoVault
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.UUID

data class FormState(
    val type: ContractType = ContractType.SOFTWARE_DEV,
    val fields: List<FormField> = emptyList(),
    val formValues: Map<String, String> = emptyMap(),
    val isGenerating: Boolean = false
)

class ContractViewModel @JvmOverloads constructor(
    application: Application,
    private val contractDao: ContractDao = AppDatabase.getDatabase(application).contractDao(),
    private val billingManager: BillingManager = BillingManager(application)
) : AndroidViewModel(application) {

    val allContracts: StateFlow<List<ContractEntity>> = contractDao.getAllContracts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val billingUiState: StateFlow<BillingUiState> = billingManager.uiState

    private val _selectedContract = MutableStateFlow<ContractEntity?>(null)
    val selectedContract: StateFlow<ContractEntity?> = _selectedContract.asStateFlow()

    private val _showPaywall = MutableStateFlow(false)
    val showPaywall: StateFlow<Boolean> = _showPaywall.asStateFlow()

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    init {
        selectContractTypeForNew(ContractType.SOFTWARE_DEV)
    }

    fun openPaywall() {
        _showPaywall.value = true
    }

    fun dismissPaywall() {
        _showPaywall.value = false
    }

    fun purchaseMonthlyPlan(activity: Activity) {
        billingManager.purchaseMonthlyPlan(activity) { success, msg ->
            if (success) {
                _showPaywall.value = false
            }
        }
    }

    fun purchaseSinglePass(activity: Activity) {
        val currentId = _selectedContract.value?.id
        billingManager.purchaseSinglePass(activity, currentId) { success, msg ->
            if (success) {
                viewModelScope.launch {
                    val current = _selectedContract.value
                    if (current != null) {
                        val updated = current.copy(
                            isPurchasedPass = true,
                            isLocked = true,
                            status = if (current.status == "DRAFT") "SIGNED" else current.status,
                            updatedAt = System.currentTimeMillis()
                        )
                        contractDao.insertContract(updated)
                        _selectedContract.value = updated
                    }
                    _showPaywall.value = false
                }
            }
        }
    }

    fun restorePurchases() {
        billingManager.restorePurchases { _, _ -> }
    }

    fun loadContractById(id: Long) {
        viewModelScope.launch {
            val contract = contractDao.getContractById(id)
            _selectedContract.value = contract
        }
    }

    fun selectContractTypeForNew(type: ContractType) {
        val initialFields = ContractTemplateEngine.getDefaultFieldsForType(type)
        val initialValues = initialFields.associate { it.id to it.value }
        _formState.value = FormState(
            type = type,
            fields = initialFields,
            formValues = initialValues,
            isGenerating = false
        )
    }

    fun updateFormField(fieldId: String, value: String) {
        val current = _formState.value
        val updatedValues = current.formValues.toMutableMap().apply {
            put(fieldId, value)
        }
        val updatedFields = current.fields.map { field ->
            if (field.id == fieldId) field.copy(value = value) else field
        }
        _formState.value = current.copy(
            fields = updatedFields,
            formValues = updatedValues
        )
    }

    /**
     * Checks whether the user can create or generate a new contract.
     * Rule:
     * - Monthly Pro subscribers can create unlimited contracts.
     * - Free users who have not used their 1 Single Contract Pass can create 1 contract.
     * - Users who have already purchased/used their 1 Single Contract Pass CANNOT create or generate any new contracts unless they subscribe to Monthly Pro.
     */
    fun canCreateNewContract(): Boolean {
        val billing = billingUiState.value
        if (billing.isMonthlySubscribed) return true
        if (!billing.hasUsedSinglePass) return true
        return false
    }

    /**
     * Checks whether the user can access restricted operations (PDF download, share, remote link, QR code)
     * for a given contract.
     * Returns true if:
     * - Monthly Pro is active, OR
     * - This specific contract was purchased via the 1-time single pass (and archived/locked).
     */
    fun canAccessContractFeatures(contract: ContractEntity?): Boolean {
        val billing = billingUiState.value
        if (billing.isMonthlySubscribed) return true
        if (contract != null && (contract.isPurchasedPass || contract.isLocked)) return true
        return false
    }

    /**
     * Checks whether the user can sign a new contract.
     * Rule:
     * 1. 1st contract can be signed/unlocked via Single Pass ($0.99) or Monthly Pro.
     * 2. 2nd contract and subsequent require Monthly Pro subscription if single pass was already used.
     */
    fun canUserSignContract(contract: ContractEntity?): Boolean {
        val billing = billingUiState.value
        if (billing.isMonthlySubscribed) return true
        if (contract != null && (contract.isPurchasedPass || contract.isLocked)) return true
        if (!billing.hasUsedSinglePass) return true
        return false
    }

    fun generateAndSaveContract(context: Context? = null, onContractCreated: (Long) -> Unit) {
        if (!canCreateNewContract()) {
            context?.let {
                Toast.makeText(
                    it,
                    "Single Contract Pass has already been used for 1 contract. Upgrade to Monthly Pro to create and generate new contracts.",
                    Toast.LENGTH_LONG
                ).show()
            }
            _showPaywall.value = true
            return
        }

        val currentState = _formState.value
        val type = currentState.type
        val values = currentState.formValues

        _formState.value = currentState.copy(isGenerating = true)

        viewModelScope.launch {
            val generatedDraft = ContractTemplateEngine.generateDraftText(type, values)

            val partyA = values["dev_name"] ?: values["designer_name"] ?: values["agency_name"] ?: values["client_name"] ?: "Party A"
            val partyB = values["client_name"] ?: values["company_name"] ?: "Party B"
            val partyAEmail = values["dev_email"] ?: values["designer_email"] ?: values["agency_email"] ?: ""
            val partyBEmail = values["client_email"] ?: ""
            val title = "${type.title.substringBefore(" Agreement").substringBefore(" Contract")} - $partyB"

            val jsonObject = JSONObject()
            values.forEach { (k, v) -> jsonObject.put(k, v) }

            val uniqueToken = UUID.randomUUID().toString().replace("-", "").take(16)

            val entity = ContractEntity(
                title = title,
                type = type.name,
                partyA = partyA,
                partyB = partyB,
                partyAEmail = partyAEmail,
                partyBEmail = partyBEmail,
                formValuesJson = jsonObject.toString(),
                generatedDraftText = generatedDraft,
                remoteSigningToken = uniqueToken,
                status = "DRAFT",
                isPurchasedPass = false,
                isLocked = false,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val newId = contractDao.insertContract(entity)
            val savedContract = entity.copy(id = newId)
            _selectedContract.value = savedContract
            _formState.value = _formState.value.copy(isGenerating = false)

            onContractCreated(newId)
        }
    }

    fun attachSignatureAndSign(context: Context, signatureBase64: String) {
        val current = _selectedContract.value ?: return

        // If contract is locked, modifications are prevented
        if (current.isLocked) {
            Toast.makeText(context, "This contract is archived & locked. It cannot be modified.", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if user already used 1-time single pass on another contract and is not monthly subscribed
        val billing = billingUiState.value
        if (billing.hasUsedSinglePass && !billing.isMonthlySubscribed && !current.isPurchasedPass) {
            Toast.makeText(context, "Single Contract Pass already used. Subscribe to Monthly Pro for additional contracts.", Toast.LENGTH_LONG).show()
            _showPaywall.value = true
            return
        }

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val auditHash = CryptoVault.generateESignatureHash(
                contractTitle = current.title,
                partyA = current.partyA,
                partyB = current.partyB,
                signatureBase64 = signatureBase64,
                timestamp = timestamp
            )

            // When signed, mark as SIGNED. If monthly subscribed or purchased, lock as archived
            val isNowLocked = billing.isMonthlySubscribed || current.isPurchasedPass
            val updated = current.copy(
                signatureBase64 = signatureBase64,
                signatureTimestamp = timestamp,
                signatureHash = auditHash,
                status = if (isNowLocked) "ARCHIVED" else "SIGNED",
                isLocked = isNowLocked,
                updatedAt = timestamp
            )

            contractDao.insertContract(updated)
            _selectedContract.value = updated
            Toast.makeText(context, "Contract signed & secured with SHA-256 seal!", Toast.LENGTH_SHORT).show()
        }
    }

    fun attachPartyBSignatureAndSign(context: Context, partyBSigBase64: String) {
        val current = _selectedContract.value ?: return
        if (current.isLocked) {
            Toast.makeText(context, "This contract is archived & locked.", Toast.LENGTH_SHORT).show()
            return
        }

        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val updated = current.copy(
                partyBSignatureBase64 = partyBSigBase64,
                partyBSignatureTimestamp = timestamp,
                updatedAt = timestamp
            )
            contractDao.insertContract(updated)
            _selectedContract.value = updated
            Toast.makeText(context, "Recipient signature attached!", Toast.LENGTH_SHORT).show()
        }
    }

    fun downloadAndOpenPdfWithQuota(context: Context) {
        val current = _selectedContract.value
        if (current == null) {
            Toast.makeText(context, "No contract selected.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!canAccessContractFeatures(current)) {
            _showPaywall.value = true
            return
        }

        val pdfFile = PdfExporter.generatePdfFile(context, current)
        if (pdfFile != null) {
            PdfExporter.openAndDownloadPdf(context, pdfFile, current.title)
        } else {
            Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportAndSharePdfWithQuota(context: Context, recipientEmail: String = "") {
        val current = _selectedContract.value
        if (current == null) {
            Toast.makeText(context, "No contract selected.", Toast.LENGTH_SHORT).show()
            return
        }

        if (!canAccessContractFeatures(current)) {
            _showPaywall.value = true
            return
        }

        val pdfFile = PdfExporter.generatePdfFile(context, current)
        if (pdfFile != null) {
            if (recipientEmail.isNotBlank()) {
                PdfExporter.sharePdfViaEmail(context, pdfFile, current.title, recipientEmail)
            } else {
                PdfExporter.sharePdfGeneral(context, pdfFile, current.title)
            }
        } else {
            Toast.makeText(context, "Failed to generate PDF.", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyOrShareRemoteLinkWithQuota(context: Context, link: String, onActionAllowed: () -> Unit) {
        val current = _selectedContract.value
        if (!canAccessContractFeatures(current)) {
            _showPaywall.value = true
            return
        }
        onActionAllowed()
    }

    fun showQrCodeWithQuota(onActionAllowed: () -> Unit) {
        val current = _selectedContract.value
        if (!canAccessContractFeatures(current)) {
            _showPaywall.value = true
            return
        }
        onActionAllowed()
    }

    fun deleteContract(contract: ContractEntity) {
        viewModelScope.launch {
            contractDao.deleteContract(contract)
            _selectedContract.value = null
        }
    }
}
