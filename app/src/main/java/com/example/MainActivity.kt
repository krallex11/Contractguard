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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.components.SleekBottomNavBar
import com.example.ui.screens.DraftPreviewScreen
import com.example.ui.screens.FormInputScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SavedContractsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SignaturesScreen
import com.example.ui.theme.ContractGuardTheme
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import com.example.ui.viewmodel.ContractViewModel

enum class ScreenState {
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
    var currentScreen by remember { mutableStateOf(ScreenState.MAIN_TABS) }
    var selectedTab by remember { mutableStateOf(NavTab.HOME) }

    val allContracts by viewModel.allContracts.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    val selectedContract by viewModel.selectedContract.collectAsStateWithLifecycle()
    val toastMessage by viewModel.toastMessage.collectAsStateWithLifecycle()

    LaunchedEffect(toastMessage) {
        toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    Scaffold(
        containerColor = SleekDarkBackground,
        topBar = {
            if (currentScreen == ScreenState.MAIN_TABS) {
                SleekHeaderBar()
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
                    ScreenState.MAIN_TABS -> {
                        when (selectedTab) {
                            NavTab.HOME -> HomeScreen(
                                savedContracts = allContracts,
                                onSelectTemplate = { type ->
                                    viewModel.selectContractTypeForNew(type)
                                    currentScreen = ScreenState.FORM_INPUT
                                },
                                onOpenContractDetails = { id ->
                                    viewModel.loadContractById(id)
                                    currentScreen = ScreenState.DRAFT_PREVIEW
                                },
                                onSharePdf = { contract ->
                                    viewModel.loadContractById(contract.id)
                                    viewModel.exportAndSharePdf(context)
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
                                    viewModel.exportAndSharePdf(context)
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
                                    viewModel.exportAndSharePdf(context)
                                }
                            )

                            NavTab.SETTINGS -> SettingsScreen()
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
                                viewModel.generateAndSaveContract { newId ->
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
                                    viewModel.downloadAndOpenPdf(context)
                                },
                                onExportAndSharePdf = { recipientEmail ->
                                    viewModel.exportAndSharePdf(context, recipientEmail)
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
fun SleekHeaderBar() {
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

