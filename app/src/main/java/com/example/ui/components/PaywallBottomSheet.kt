package com.example.ui.components

import android.app.Activity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.billing.BillingUiState
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenOnPrimary
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekRedAlert
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallBottomSheet(
    billingUiState: BillingUiState,
    onDismiss: () -> Unit,
    onPurchaseMonthly: (Activity) -> Unit,
    onPurchaseSinglePass: (Activity) -> Unit,
    onRestorePurchases: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hasUsedSinglePass = billingUiState.hasUsedSinglePass

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SleekDarkBackground,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close Button & Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekLimeGreenContainer)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SleekLimeGreenPrimary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "LEGAL UPGRADE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SleekLimeGreenPrimary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp).testTag("close_paywall_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = SleekTextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Title
            Text(
                text = "Unlock Legally Binding Contracts",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 21.sp,
                color = SleekTextWhite,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (hasUsedSinglePass && !billingUiState.isMonthlySubscribed)
                    "You have used your 1 Single Contract Pass. Subscribe to Monthly Pro for unlimited contracts & signatures."
                else
                    "Choose an option to archive and export your official PDF agreements with SHA-256 digital seals.",
                fontSize = 12.5.sp,
                color = SleekTextMuted,
                textAlign = TextAlign.Center,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Option 1: Monthly Unlimited Pro (Primary Recommendation)
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.5.dp, SleekLimeGreenPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .clickable {
                        activity?.let { onPurchaseMonthly(it) }
                    }
                    .testTag("purchase_monthly_option")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SleekLimeGreenContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AllInclusive,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Monthly Pro Unlimited",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SleekTextWhite
                                )
                                Text(
                                    text = "Full unlimited access to all features",
                                    fontSize = 11.sp,
                                    color = SleekTextMuted
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekLimeGreenPrimary)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "RECOMMENDED",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                color = SleekLimeGreenOnPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    PaywallFeatureRow(text = "Unlimited contract generation & templates")
                    PaywallFeatureRow(text = "Unlimited digital signatures & SHA-256 seals")
                    PaywallFeatureRow(text = "Unlimited PDF downloads & instant sharing")
                    PaywallFeatureRow(text = "Unlimited remote e-signing links & QR codes")

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = { activity?.let { onPurchaseMonthly(it) } },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = SleekLimeGreenPrimary,
                            contentColor = SleekLimeGreenOnPrimary
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("subscribe_monthly_button")
                    ) {
                        Text(
                            text = "Subscribe • ${billingUiState.monthlyProductPrice}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Option 2: Single Contract Pass ($0.99)
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                shape = RoundedCornerShape(22.dp),
                border = BorderStroke(1.dp, if (hasUsedSinglePass) SleekDarkCardBorder else SleekDarkCardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .testTag("purchase_single_option")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (hasUsedSinglePass) Color(0xFF1E293B) else Color(0xFF1E293B)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasUsedSinglePass) Icons.Default.Lock else Icons.Default.Description,
                                    contentDescription = null,
                                    tint = if (hasUsedSinglePass) SleekTextMuted else SleekLimeGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Single Contract Pass",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (hasUsedSinglePass) SleekTextMuted else SleekTextWhite
                                )
                                Text(
                                    text = if (hasUsedSinglePass) "1/1 Single Pass Used (Exhausted)" else "Permanent unlock for 1 contract",
                                    fontSize = 11.sp,
                                    color = if (hasUsedSinglePass) SleekRedAlert else SleekTextMuted
                                )
                            }
                        }

                        if (hasUsedSinglePass) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF331B1B))
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "EXHAUSTED",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekRedAlert
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (!hasUsedSinglePass) {
                        Text(
                            text = "• Unlocks and permanently archives ONLY this 1 specific agreement.\n• All entered details become sealed & immutable (tamper-proof).\n• Single Pass CANNOT be used to generate new or extra contracts.\n• Unlocks unrestricted PDF downloads, sharing & remote signing for this agreement.",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedButton(
                            onClick = { activity?.let { onPurchaseSinglePass(it) } },
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, SleekLimeGreenPrimary),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("buy_single_pass_button")
                        ) {
                            Text(
                                text = "Unlock This 1 Contract • ${billingUiState.singleProductPrice}",
                                color = SleekLimeGreenPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    } else {
                        Text(
                            text = "You have already used your 1 single contract purchase. That contract is archived. To create, edit, or sign new contracts, please subscribe to Monthly Pro Unlimited above.",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { activity?.let { onPurchaseMonthly(it) } },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekDarkBackground,
                                contentColor = SleekLimeGreenPrimary
                            ),
                            border = BorderStroke(1.dp, SleekLimeGreenPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(44.dp)
                        ) {
                            Text("Upgrade to Monthly Pro to Create More", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore Purchases & Terms
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onRestorePurchases,
                    modifier = Modifier.testTag("restore_purchases_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = null,
                        tint = SleekTextMuted,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "Restore Purchases",
                        fontSize = 12.sp,
                        color = SleekTextMuted
                    )
                }
            }

            Text(
                text = "Subscriptions auto-renew monthly via Google Play until cancelled in Play Store settings. ESIGN Act & eIDAS legally compliant.",
                fontSize = 10.sp,
                color = SleekTextMuted.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun PaywallFeatureRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 3.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = SleekLimeGreenPrimary,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = SleekTextWhite
        )
    }
}
