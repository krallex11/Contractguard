package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.NavTab
import com.example.ui.components.PaywallBottomSheet
import com.example.ui.components.SleekBottomNavBar
import com.example.ui.screens.DraftPreviewScreen
import com.example.ui.screens.FormInputScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedContractsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignaturesScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.ContractGuardTheme
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import com.example.ui.viewmodel.ContractViewModel

enum class ScreenState {
    SPLASH,
    MAIN_TABS,
    FORM_INPUT,
    DRAFT_PREVIEW
}

class MainActivity : ComponentActivity() {

    private val viewModel: ContractViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ContractGuardTheme {
                ContractGuardApp(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContractGuardApp(viewModel: ContractViewModel) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf(ScreenState.SPLASH) }
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }

    val allContracts by viewModel.allContracts.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val selectedContract by viewModel.selectedContract.collectAsStateWithLifecycle()

    val billingUiState by viewModel.billingUiState.collectAsStateWithLifecycle()
    val showPaywallSheet by viewModel.showPaywall.collectAsStateWithLifecycle()

    if (showPaywallSheet) {
        PaywallBottomSheet(
            billingUiState = billingUiState,
            onDismiss = { viewModel.dismissPaywall() },
            onPurchaseMonthly = { activity -> viewModel.purchaseMonthlyPlan(activity) },
            onPurchaseSinglePass = { activity -> viewModel.purchaseSinglePass(activity) },
            onRestorePurchases = { viewModel.restorePurchases() },
            onManageSubscriptions = { viewModel.openManageSubscriptions(context) }
        )
    }

    Scaffold(
        containerColor = SleekDarkBackground,
        topBar = {
            if (currentScreen == ScreenState.MAIN_TABS) {
                SleekHeaderBar(
                    isMonthlySubscribed = billingUiState.isMonthlySubscribed,
                    hasUsedSinglePass = billingUiState.hasUsedSinglePass,
                    onOpenPaywall = { viewModel.openPaywall() }
                )
            }
        },
        bottomBar = {
            if (currentScreen == ScreenState.MAIN_TABS) {
                SleekBottomNavBar(
                    selectedTab = selectedTab,
                    onTabSelected = { tab -> selectedTab = tab }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    ScreenState.SPLASH -> {
                        SplashScreen(
                            onSplashFinished = {
                                currentScreen = ScreenState.MAIN_TABS
                            }
                        )
                    }

                    ScreenState.MAIN_TABS -> {
                        when (selectedTab) {
                            NavTab.HOME -> HomeScreen(
                                savedContracts = allContracts,
                                isLockedForNewContracts = !viewModel.canCreateNewContract(),
                                onSelectTemplate = { type ->
                                    if (!viewModel.canCreateNewContract()) {
                                        Toast.makeText(
                                            context,
                                            "Single Contract Pass has already been used for 1 contract. Upgrade to Monthly Pro to create new contracts.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                        viewModel.openPaywall()
                                    } else {
                                        viewModel.selectContractTypeForNew(type)
                                        currentScreen = ScreenState.FORM_INPUT
                                    }
                                },
                                onOpenContractDetails = { id ->
                                    viewModel.loadContractById(id)
                                    currentScreen = ScreenState.DRAFT_PREVIEW
                                },
                                onSharePdf = { contract ->
                                    viewModel.loadContractById(contract.id)
                                    viewModel.exportAndSharePdfWithQuota(context)
                                }
                            )

                            NavTab.CLOUD_FILES -> SavedContractsScreen(
                                contracts = allContracts,
                                onOpenContractDetails = { id ->
                                    viewModel.loadContractById(id)
                                    currentScreen = ScreenState.DRAFT_PREVIEW
                                },
                                onSharePdf = { contract ->
                                    viewModel.loadContractById(contract.id)
                                    viewModel.exportAndSharePdfWithQuota(context)
                                }
                            )

                            NavTab.SIGNATURES -> SignaturesScreen(
                                signedContracts = allContracts.filter { it.status == "SIGNED" },
                                onOpenContractDetails = { id ->
                                    viewModel.loadContractById(id)
                                    currentScreen = ScreenState.DRAFT_PREVIEW
                                },
                                onSharePdf = { contract ->
                                    viewModel.loadContractById(contract.id)
                                    viewModel.exportAndSharePdfWithQuota(context)
                                }
                            )

                            NavTab.SETTINGS -> SettingsScreen(
                                billingUiState = billingUiState,
                                onOpenPaywall = { viewModel.openPaywall() },
                                onManageSubscriptions = { viewModel.openManageSubscriptions(context) }
                            )
                        }
                    }

                    ScreenState.FORM_INPUT -> {
                        FormInputScreen(
                            contractType = formState.type,
                            fields = formState.fields,
                            isGenerating = formState.isGenerating,
                            onFieldValueChange = { fieldId, valStr ->
                                viewModel.updateFormField(fieldId, valStr)
                            },
                            onGenerateClicked = {
                                viewModel.generateAndSaveContract(context) { newId ->
                                    currentScreen = ScreenState.DRAFT_PREVIEW
                                }
                            },
                            onBackClicked = {
                                currentScreen = ScreenState.MAIN_TABS
                            }
                        )
                    }

                    ScreenState.DRAFT_PREVIEW -> {
                        val currentContract = selectedContract
                        if (currentContract != null) {
                            DraftPreviewScreen(
                                contract = currentContract,
                                onAttachSignature = { signatureBase64 ->
                                    viewModel.attachSignatureAndSign(context, signatureBase64)
                                },
                                onAttachPartyBSignature = { partyBSigBase64 ->
                                    viewModel.attachPartyBSignatureAndSign(context, partyBSigBase64)
                                },
                                onDownloadPdf = {
                                    viewModel.downloadAndOpenPdfWithQuota(context)
                                },
                                onExportAndSharePdf = { recipientEmail ->
                                    viewModel.exportAndSharePdfWithQuota(context, recipientEmail)
                                },
                                onCopyRemoteLink = { link, action ->
                                    viewModel.copyOrShareRemoteLinkWithQuota(context, link, action)
                                },
                                onShowQr = { action ->
                                    viewModel.showQrCodeWithQuota(action)
                                },
                                onDeleteContract = {
                                    viewModel.deleteContract(currentContract)
                                    currentScreen = ScreenState.MAIN_TABS
                                },
                                onBackClicked = {
                                    currentScreen = ScreenState.MAIN_TABS
                                }
                            )
                        } else {
                            // Fallback if null
                            LaunchedEffect(Unit) {
                                currentScreen = ScreenState.MAIN_TABS
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SleekHeaderBar(
    isMonthlySubscribed: Boolean = false,
    hasUsedSinglePass: Boolean = false,
    onOpenPaywall: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 20.dp, top = 40.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "AES-256 VAULT",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = SleekLimeGreenPrimary,
                letterSpacing = 1.5.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "ContractGuard",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = SleekTextWhite,
                letterSpacing = (-0.5).sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SleekLimeGreenContainer)
                    .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenPaywall)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = SleekLimeGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isMonthlySubscribed) "PRO ACTIVE"
                        else if (hasUsedSinglePass) "PASS: 1/1 USED"
                        else "UPGRADE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekLimeGreenPrimary
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(SleekLimeGreenContainer)
                    .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
