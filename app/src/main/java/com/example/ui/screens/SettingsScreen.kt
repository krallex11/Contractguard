package com.example.ui.screens

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite

@Composable
fun SettingsScreen() {
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

