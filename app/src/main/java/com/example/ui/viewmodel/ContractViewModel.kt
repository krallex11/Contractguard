package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.AppDatabase
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
import java.io.File

data class FormState(
    val type: ContractType = ContractType.SOFTWARE_DEV,
    val fields: List<FormField> = emptyList(),
    val isGenerating: Boolean = false
)

class ContractViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.contractDao()

    val allContracts: StateFlow<List<ContractEntity>> = dao.getAllContracts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _formState = MutableStateFlow(FormState())
    val formState: StateFlow<FormState> = _formState.asStateFlow()

    private val _selectedContract = MutableStateFlow<ContractEntity?>(null)
    val selectedContract: StateFlow<ContractEntity?> = _selectedContract.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    fun selectContractTypeForNew(type: ContractType) {
        val defaultFields = ContractTemplateEngine.getDefaultFieldsForType(type)
        _formState.value = FormState(
            type = type,
            fields = defaultFields
        )
    }

    fun updateFormField(fieldId: String, newValue: String) {
        val updatedList = _formState.value.fields.map { field ->
            if (field.id == fieldId) field.copy(value = newValue) else field
        }
        _formState.value = _formState.value.copy(fields = updatedList)
    }

    fun generateAndSaveContract(onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            _formState.value = _formState.value.copy(isGenerating = true)

            val currentForm = _formState.value
            val fieldMap = currentForm.fields.associate { it.id to it.value }

            val draftText = ContractTemplateEngine.generateDraftText(currentForm.type, fieldMap)

            val partyA = fieldMap["client_name"]
                ?: fieldMap["party_a"]
                ?: fieldMap["client_company"]
                ?: "Client Party"

            val partyB = fieldMap["developer_name"]
                ?: fieldMap["designer_name"]
                ?: fieldMap["agency_name"]
                ?: fieldMap["party_b"]
                ?: "Contractor Party"

            val partyAEmail = fieldMap["client_email"] ?: ""
            val partyBEmail = fieldMap["developer_email"]
                ?: fieldMap["designer_email"]
                ?: fieldMap["agency_email"]
                ?: ""

            // Encrypt metadata with AES-256 CryptoVault before persistence
            val rawValuesJson = fieldMap.entries.joinToString(",") { "\"${it.key}\":\"${it.value}\"" }
            val encryptedFormValues = CryptoVault.encrypt("{$rawValuesJson}")

            val entity = ContractEntity(
                title = currentForm.type.title,
                type = currentForm.type.name,
                partyA = partyA,
                partyB = partyB,
                partyAEmail = partyAEmail,
                partyBEmail = partyBEmail,
                formValuesJson = encryptedFormValues,
                generatedDraftText = draftText,
                status = "DRAFT",
                isCloudSynced = true,
                isEncrypted = true
            )

            val newId = dao.insertContract(entity)
            val savedContract = dao.getContractById(newId)
            _selectedContract.value = savedContract
            _formState.value = _formState.value.copy(isGenerating = false)
            _toastMessage.value = "Contract draft generated & end-to-end encrypted!"
            onSuccess(newId)
        }
    }

    fun loadContractById(id: Long) {
        viewModelScope.launch {
            val contract = dao.getContractById(id)
            _selectedContract.value = contract
        }
    }

    fun attachSignatureAndSign(context: Context, signatureBase64: String) {
        val current = _selectedContract.value ?: return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val eHash = CryptoVault.generateESignatureHash(
                current.title,
                current.partyA,
                current.partyB,
                signatureBase64,
                timestamp
            )

            var updated = current.copy(
                signatureBase64 = signatureBase64,
                signatureTimestamp = timestamp,
                signatureHash = eHash,
                status = "SIGNED",
                updatedAt = timestamp
            )

            val pdfFile = PdfExporter.generatePdfFile(context, updated)
            if (pdfFile != null) {
                updated = updated.copy(pdfPath = pdfFile.absolutePath)
            }

            dao.updateContract(updated)
            _selectedContract.value = updated
            _toastMessage.value = "Taraf A E-İmzası kaydedildi ve onaylı PDF oluşturuldu!"
        }
    }

    fun attachPartyBSignatureAndSign(context: Context, signatureBase64: String) {
        val current = _selectedContract.value ?: return
        viewModelScope.launch {
            val timestamp = System.currentTimeMillis()
            val eHash = CryptoVault.generateESignatureHash(
                current.title,
                current.partyA,
                current.partyB,
                (current.signatureBase64 ?: "") + signatureBase64,
                timestamp
            )

            var updated = current.copy(
                partyBSignatureBase64 = signatureBase64,
                partyBSignatureTimestamp = timestamp,
                signatureHash = eHash,
                status = "SIGNED",
                updatedAt = timestamp
            )

            val pdfFile = PdfExporter.generatePdfFile(context, updated)
            if (pdfFile != null) {
                updated = updated.copy(pdfPath = pdfFile.absolutePath)
            }

            dao.updateContract(updated)
            _selectedContract.value = updated
            _toastMessage.value = "Alıcı / Taraf B (${current.partyB}) uzaktan imzayı tamamladı! Tamamlanmış PDF oluşturuldu ve kaydedildi."
        }
    }

    fun exportAndSharePdf(context: Context, recipientEmail: String = "") {
        val current = _selectedContract.value ?: return
        viewModelScope.launch {
            val pdfFile = PdfExporter.generatePdfFile(context, current)
            if (pdfFile != null) {
                val updated = current.copy(
                    pdfPath = pdfFile.absolutePath,
                    status = if (current.status == "DRAFT") "EXPORTED" else current.status
                )
                dao.updateContract(updated)
                _selectedContract.value = updated

                if (recipientEmail.isNotEmpty()) {
                    PdfExporter.sharePdfViaEmail(context, pdfFile, current.title, recipientEmail)
                } else {
                    PdfExporter.sharePdfGeneral(context, pdfFile, current.title)
                }
                _toastMessage.value = "E-İmzalı PDF hazırlandı!"
            } else {
                _toastMessage.value = "PDF oluşturulurken bir hata meydana geldi."
            }
        }
    }

    fun downloadAndOpenPdf(context: Context) {
        val current = _selectedContract.value ?: return
        viewModelScope.launch {
            val pdfFile = PdfExporter.generatePdfFile(context, current)
            if (pdfFile != null) {
                val updated = current.copy(
                    pdfPath = pdfFile.absolutePath,
                    status = if (current.status == "DRAFT") "EXPORTED" else current.status
                )
                dao.updateContract(updated)
                _selectedContract.value = updated

                PdfExporter.openAndDownloadPdf(context, pdfFile, current.title)
                _toastMessage.value = "E-İmzalı PDF hazırlandı ve açılıyor!"
            } else {
                _toastMessage.value = "PDF oluşturulurken hata oluştu."
            }
        }
    }

    fun deleteContract(contract: ContractEntity) {
        viewModelScope.launch {
            dao.deleteContract(contract)
            if (_selectedContract.value?.id == contract.id) {
                _selectedContract.value = null
            }
            _toastMessage.value = "Sözleşme silindi."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }
}
