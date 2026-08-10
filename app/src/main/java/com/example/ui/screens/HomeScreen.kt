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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.example.data.model.ContractType
import com.example.ui.theme.SleekBorderOutline
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenBorder
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    savedContracts: List<ContractEntity>,
    onSelectTemplate: (ContractType) -> Unit,
    onOpenContractDetails: (Long) -> Unit,
    onSharePdf: (ContractEntity) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Top Legally Binding Sparkle Badge Pill
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(SleekDarkSurface)
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(30.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = SleekLimeGreenPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "LEGALLY BINDING MOBILE GENERATOR",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = SleekLimeGreenPrimary,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }

        // Section Title
        item {
            Column(modifier = Modifier.padding(vertical = 2.dp)) {
                Text(
                    text = "Select Contract Type",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = SleekTextWhite
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Choose a template to instantly build a US & EU compliant agreement on your phone.",
                    fontSize = 13.sp,
                    color = SleekTextMuted,
                    lineHeight = 18.sp
                )
            }
        }

        // 3 Global Contract Templates matching screenshot
        item {
            SleekTemplateCard(
                type = ContractType.SOFTWARE_DEV,
                title = "Software Development Agreement",
                subtitle = "For Web, Mobile App & SaaS Developers",
                tagText = "Most Popular",
                targetUserText = "Freelance Software Engineers, Full-Stack Devs, & Technical Agencies",
                icon = Icons.Default.Code,
                onClick = { onSelectTemplate(ContractType.SOFTWARE_DEV) },
                testTag = "template_software_button"
            )
        }

        item {
            SleekTemplateCard(
                type = ContractType.GRAPHIC_DESIGN,
                title = "Graphic & Logo Design Contract",
                subtitle = "For Designers, Illustrators & UI/UX Creators",
                tagText = "Essential",
                targetUserText = "Brand Designers, UI/UX Specialists, Illustrators & Visual Artists",
                icon = Icons.Default.Palette,
                onClick = { onSelectTemplate(ContractType.GRAPHIC_DESIGN) },
                testTag = "template_design_button"
            )
        }

        item {
            SleekTemplateCard(
                type = ContractType.SOCIAL_MEDIA,
                title = "Social Media Management Agreement",
                subtitle = "For SMMs, Content Creators & Growth Agencies",
                tagText = "Recurring Income",
                targetUserText = "Social Media Managers, Copywriters, Content Strategists & Agencies",
                icon = Icons.Default.Share,
                onClick = { onSelectTemplate(ContractType.SOCIAL_MEDIA) },
                testTag = "template_social_button"
            )
        }

        // End-to-End Security Encryption Notice
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(SleekLimeGreenContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = SleekLimeGreenPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "AES-256 Encrypted Local & Cloud Vault",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp,
                            color = SleekTextWhite
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "All contracts stored with cryptographic hashes compliant with US ESIGN Act and EU eIDAS regulations.",
                            fontSize = 11.5.sp,
                            lineHeight = 15.sp,
                            color = SleekTextMuted
                        )
                    }
                }
            }
        }

        // Saved Contracts Section
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Agreements (${savedContracts.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = SleekTextWhite
                    )
                )
                Text(
                    text = "View All",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = SleekLimeGreenPrimary,
                    modifier = Modifier.clickable { /* Tab handles viewing all */ }
                )
            }
        }

        if (savedContracts.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SleekDarkSurface)
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = SleekTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No saved contracts yet.",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = SleekTextWhite
                        )
                        Text(
                            text = "Tap any template above to instantly generate your first agreement.",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted
                        )
                    }
                }
            }
        } else {
            items(savedContracts) { contract ->
                SleekSavedContractItem(
                    contract = contract,
                    onClick = { onOpenContractDetails(contract.id) },
                    onSharePdf = { onSharePdf(contract) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
fun SleekTemplateCard(
    type: ContractType,
    title: String,
    subtitle: String,
    tagText: String,
    targetUserText: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(24.dp))
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Square Box
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Main Info Block
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = SleekTextWhite
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = SleekTextMuted
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Green Badge Pill + Target User Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(SleekLimeGreenContainer)
                            .border(1.dp, SleekLimeGreenBorder, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = tagText,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = SleekLimeGreenPrimary
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = targetUserText,
                        fontSize = 10.5.sp,
                        color = SleekTextMuted,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right Arrow Circle Action Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1E293B)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Create",
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun SleekSavedContractItem(
    contract: ContractEntity,
    onClick: () -> Unit,
    onSharePdf: () -> Unit
) {
    val dateString = SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.US).format(Date(contract.updatedAt))

    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (contract.status == "SIGNED") SleekLimeGreenContainer
                        else Color(0xFF0F172A)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (contract.status == "SIGNED") Icons.Default.CheckCircle else Icons.Default.PictureAsPdf,
                    contentDescription = null,
                    tint = if (contract.status == "SIGNED") SleekLimeGreenPrimary else SleekTextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contract.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = SleekTextWhite
                )
                Text(
                    text = "${contract.partyA} • ${contract.partyB}",
                    fontSize = 11.5.sp,
                    color = SleekTextMuted,
                    maxLines = 1
                )
                Text(
                    text = dateString,
                    fontSize = 10.5.sp,
                    color = SleekTextMuted.copy(alpha = 0.7f)
                )
            }

            IconButton(onClick = onSharePdf) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Share",
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

