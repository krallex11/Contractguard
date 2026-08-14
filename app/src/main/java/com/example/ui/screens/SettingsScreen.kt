package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.billing.BillingUiState
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenOnPrimary
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun SettingsScreen(
    billingUiState: BillingUiState = BillingUiState(),
    onOpenPaywall: () -> Unit = {},
    onManageSubscriptions: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(SleekLimeGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = SleekLimeGreenPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "ContractGuard: Contract Maker",
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = SleekTextWhite
                    )

                    Text(
                        text = "com.fixbangstudio.contractmaker",
                        fontSize = 12.sp,
                        color = SleekTextMuted
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(SleekLimeGreenContainer)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = SleekLimeGreenPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Ad-Free • Global Pro v1.0.0",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = SleekLimeGreenPrimary
                        )
                    }
                }
            }
        }

        // Active Subscription & Pass Balance Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = SleekLimeGreenPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Subscription & Pass Status",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SleekTextWhite
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SleekLimeGreenContainer)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (billingUiState.isMonthlySubscribed) "MONTHLY ACTIVE"
                                else if (billingUiState.hasUsedSinglePass) "SINGLE PASS: USED (1/1)"
                                else "SINGLE PASS: AVAILABLE (0/1)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = SleekLimeGreenPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (billingUiState.isMonthlySubscribed)
                            "Active Monthly Pro Subscription (${billingUiState.monthlyProductPrice}). Unlimited contract generation, signing, PDF exports, and remote sharing across all contracts."
                        else if (billingUiState.hasUsedSinglePass)
                            "Single Contract Pass (1/1 used): Your single purchased contract is permanently archived with unrestricted PDF exports. Monthly Pro subscription is required to sign and finalize 2nd and additional contracts."
                        else
                            "Single Contract Pass available (${billingUiState.singleProductPrice}): Allows permanent unlock and archiving of 1 contract with full unrestricted export and sharing tools. Upgrade to Monthly Pro for unlimited contracts.",
                        fontSize = 12.sp,
                        color = SleekTextMuted,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onOpenPaywall,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SleekLimeGreenPrimary,
                                contentColor = SleekLimeGreenOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Receipt,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (billingUiState.isMonthlySubscribed) "View Plan" else "Upgrade to Pro",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        androidx.compose.material3.OutlinedButton(
                            onClick = onManageSubscriptions,
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, SleekDarkCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp)
                        ) {
                            Text(
                                text = "Manage in Play",
                                color = SleekTextMuted,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Security & Legal Compliance",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = SleekTextWhite,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        item {
            SettingsFeatureItem(
                title = "AES-256 End-to-End Encryption",
                subtitle = "All metadata and draft contracts are encrypted on-device before storage.",
                icon = Icons.Default.Lock
            )
        }

        item {
            SettingsFeatureItem(
                title = "SHA-256 Digital E-Signature Seal",
                subtitle = "Cryptographic timestamp hash generated for both local and remote signatories.",
                icon = Icons.Default.Security
            )
        }

        item {
            SettingsFeatureItem(
                title = "US ESIGN Act & EU eIDAS Standard",
                subtitle = "Contract templates follow federal US & European Union legal guidelines.",
                icon = Icons.Default.Gavel
            )
        }

        item {
            SettingsFeatureItem(
                title = "Instant PDF & Remote E-Signing Link",
                subtitle = "Export vector PDFs or send QR code links for remote counterparty signatures.",
                icon = Icons.Default.Info
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun SettingsFeatureItem(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(18.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SleekLimeGreenContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekTextWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 11.5.sp,
                    color = SleekTextMuted
                )
            }
        }
    }
}
