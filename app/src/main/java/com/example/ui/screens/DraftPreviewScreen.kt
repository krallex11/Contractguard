package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ContractEntity
import com.example.ui.components.SignaturePad
import com.example.ui.theme.SleekDarkBackground
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenOnPrimary
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekRedAlert
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DraftPreviewScreen(
    contract: ContractEntity,
    onAttachSignature: (String) -> Unit,
    onAttachPartyBSignature: (String) -> Unit,
    onDownloadPdf: () -> Unit,
    onExportAndSharePdf: (String) -> Unit,
    onCopyRemoteLink: (String, () -> Unit) -> Unit,
    onShowQr: (() -> Unit) -> Unit,
    onDeleteContract: () -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }
    var showDomainDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var webHostBaseUrl by remember { mutableStateOf("https://contractguard-5511.vercel.app") }
    var tempDomainInput by remember { mutableStateOf(webHostBaseUrl) }

    val hasUserSigned = !contract.signatureBase64.isNullOrEmpty() || contract.status == "SIGNED" || contract.status == "ARCHIVED"
    val isContractArchived = contract.isLocked || contract.isPurchasedPass || contract.status == "ARCHIVED"

    val remoteSigningLink = "$webHostBaseUrl/sign/${contract.remoteSigningToken ?: contract.id}"

    // Safely decode signature bitmaps
    val partyASigBitmap = remember(contract.signatureBase64) {
        contract.signatureBase64?.let { base64 ->
            try {
                val cleanBase64 = if (base64.contains(",")) base64.substringAfter(",") else base64
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    val partyBSigBitmap = remember(contract.partyBSignatureBase64) {
        contract.partyBSignatureBase64?.let { base64 ->
            try {
                val cleanBase64 = if (base64.contains(",")) base64.substringAfter(",") else base64
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    Scaffold(
        containerColor = SleekDarkBackground,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = contract.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = SleekTextWhite,
                            maxLines = 1
                        )
                        Text(
                            text = if (isContractArchived) "Archived Legal Record • Sealed" else if (hasUserSigned) "Signed • Ready to Export" else "Draft • Pending Signature",
                            fontSize = 11.5.sp,
                            color = if (isContractArchived || hasUserSigned) SleekLimeGreenPrimary else SleekTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = SleekTextWhite
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onExportAndSharePdf("") },
                        modifier = Modifier.testTag("share_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share PDF",
                            tint = SleekLimeGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = onDownloadPdf,
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "Download PDF",
                            tint = SleekLimeGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_contract_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = SleekRedAlert
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SleekDarkBackground)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Legal Assent Header Card
            item {
                Spacer(modifier = Modifier.height(2.dp))
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (isContractArchived) SleekLimeGreenPrimary.copy(alpha = 0.6f) else SleekDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SleekLimeGreenContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isContractArchived) Icons.Default.Lock else if (hasUserSigned) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SleekLimeGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isContractArchived) "ARCHIVED & CERTIFIED RECORD (IMMUTABLE)" else if (hasUserSigned) "E-Signed Legal Agreement" else "Draft Pending Signature",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = if (isContractArchived) SleekLimeGreenPrimary else SleekTextWhite
                            )
                            Text(
                                text = if (isContractArchived) "Sealed against modifications. Unrestricted PDF export & sharing active." else "US ESIGN Act & EU eIDAS Compliant • SHA-256 Audit Trail",
                                fontSize = 11.sp,
                                color = SleekTextMuted
                            )
                        }
                    }
                }
            }

            // Draft Paper Document Text
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "CONTRACT TERMS & PROVISIONS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SleekLimeGreenPrimary,
                                letterSpacing = 0.5.sp
                            )
                            val dateStr = SimpleDateFormat("MM/dd/yyyy", Locale.US).format(Date(contract.createdAt))
                            Text(
                                text = dateStr,
                                fontSize = 11.sp,
                                color = SleekTextMuted
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = SleekDarkCardBorder
                        )

                        Text(
                            text = contract.generatedDraftText,
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            color = SleekTextWhite
                        )
                    }
                }
            }

            // Signature Pad or E-Signature Seal Display (Party A)
            item {
                if (hasUserSigned || isContractArchived) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Your Electronic Signature (Party A - Issuer)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SleekTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "Signatory: ${contract.partyA}",
                                fontSize = 12.sp,
                                color = SleekTextWhite,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (partyASigBitmap != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = partyASigBitmap,
                                        contentDescription = "Party A Signature Image",
                                        modifier = Modifier.fillMaxHeight()
                                    )
                                }
                            }

                            if (contract.signatureTimestamp != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val signDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(Date(contract.signatureTimestamp))
                                Text(
                                    text = "Signed Date: $signDate",
                                    fontSize = 11.5.sp,
                                    color = SleekTextMuted
                                )
                            }

                            if (!contract.partyBSignatureBase64.isNullOrEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SleekDarkCardBorder)

                                Text(
                                    text = "Recipient Signature (Party B): ${contract.partyB}",
                                    fontSize = 12.sp,
                                    color = SleekLimeGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )

                                if (partyBSigBitmap != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(54.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = partyBSigBitmap,
                                            contentDescription = "Party B Signature Image",
                                            modifier = Modifier.fillMaxHeight()
                                        )
                                    }
                                }

                                if (contract.partyBSignatureTimestamp != null) {
                                    val partyBSignDate = SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.US).format(Date(contract.partyBSignatureTimestamp))
                                    Text(
                                        text = "Recipient Signed Date: $partyBSignDate (Verified via Web Portal)",
                                        fontSize = 11.5.sp,
                                        color = SleekTextMuted
                                    )
                                }
                            }

                            if (!contract.signatureHash.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(10.dp))
                                        .padding(10.dp)
                                    ) {
                                    Text(
                                        text = "SHA-256 Audit Seal: ${contract.signatureHash}",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SleekLimeGreenPrimary
                                    )
                                }
                            }

                            if (isContractArchived) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "🔒 Sealed: Changes to terms and signatures on this archived contract are disabled.",
                                    fontSize = 11.sp,
                                    color = SleekTextMuted,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }
                        }
                    }
                } else {
                    SignaturePad(
                        onSignatureCaptured = { base64 ->
                            if (base64.isNotEmpty()) {
                                onAttachSignature(base64)
                            }
                        }
                    )
                }
            }

            // Bottom Action Card for PDF Download & Share (Always clickable)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = SleekLimeGreenPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Official PDF Contract",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SleekTextWhite
                                )
                                Text(
                                    text = if (isContractArchived) "Unrestricted access • Download or share anytime." else "Download or share the certified legal agreement.",
                                    fontSize = 11.5.sp,
                                    color = SleekTextMuted
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onDownloadPdf,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekLimeGreenPrimary,
                                    contentColor = SleekLimeGreenOnPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("download_pdf_bottom_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Download PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { onExportAndSharePdf("") },
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SleekLimeGreenPrimary),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("share_pdf_bottom_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share PDF", color = SleekTextWhite, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Remote Counterparty E-Signature Module at Very Bottom (Always clickable)
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, SleekLimeGreenPrimary.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Remote E-Signing Link (Recipient)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SleekTextWhite
                                )
                            }

                            if (contract.partyBSignatureBase64 != null) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(SleekLimeGreenContainer)
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = SleekLimeGreenPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Recipient Signed",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekLimeGreenPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Share this link with ${contract.partyB} to let them review and sign via web browser:",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0F172A))
                                .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = remoteSigningLink,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace,
                                color = SleekLimeGreenPrimary,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    tempDomainInput = webHostBaseUrl
                                    showDomainDialog = true
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Edit Domain",
                                    tint = SleekTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    onCopyRemoteLink(remoteSigningLink) {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        val clip = ClipData.newPlainText("Remote Signing Link", remoteSigningLink)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Link copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("copy_remote_link_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy Link", fontSize = 11.sp, color = SleekTextWhite)
                            }

                            OutlinedButton(
                                onClick = {
                                    onCopyRemoteLink(remoteSigningLink) {
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Please review and sign the contract '${contract.title}' using this secure link:\n$remoteSigningLink")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Share Remote Signing Link")
                                        context.startActivity(shareIntent)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("share_remote_link_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share Link", fontSize = 11.sp, color = SleekTextWhite)
                            }

                            OutlinedButton(
                                onClick = {
                                    onShowQr {
                                        showQrDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f).testTag("show_qr_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = null,
                                    tint = SleekLimeGreenPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Show QR", fontSize = 11.sp, color = SleekTextWhite)
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }

    // QR Code Dialog for Counterparty Scanning
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Text(
                    text = "Remote Signing QR Code",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SleekTextWhite
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Have ${contract.partyB} scan this QR code to open the e-signature web portal.",
                        fontSize = 12.sp,
                        color = SleekTextMuted
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.White)
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "QR Code",
                            tint = Color.Black,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "ESIGN Act & eIDAS Compliant",
                        fontSize = 11.sp,
                        color = SleekLimeGreenPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showQrDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLimeGreenPrimary, contentColor = SleekLimeGreenOnPrimary)
                ) {
                    Text("Close")
                }
            },
            containerColor = SleekDarkSurface
        )
    }

    // Domain Config Dialog
    if (showDomainDialog) {
        AlertDialog(
            onDismissRequest = { showDomainDialog = false },
            title = {
                Text(
                    text = "Web Hosting Domain Configuration",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SleekTextWhite
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your primary domain (e.g., https://contractguard-5511.vercel.app).",
                        fontSize = 12.sp,
                        color = SleekTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempDomainInput,
                        onValueChange = { tempDomainInput = it },
                        placeholder = { Text("https://contractguard-5511.vercel.app", fontSize = 12.sp, color = SleekTextMuted) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = SleekLimeGreenPrimary,
                            unfocusedBorderColor = SleekDarkCardBorder,
                            focusedTextColor = SleekTextWhite,
                            unfocusedTextColor = SleekTextWhite
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        webHostBaseUrl = tempDomainInput.trimEnd('/')
                        showDomainDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekLimeGreenPrimary, contentColor = SleekLimeGreenOnPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDomainDialog = false }) {
                    Text("Cancel", color = SleekTextMuted)
                }
            },
            containerColor = SleekDarkSurface
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Contract?", fontWeight = FontWeight.Bold, color = SleekTextWhite) },
            text = { Text("This action will permanently delete the contract from your device.", fontSize = 13.sp, color = SleekTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteContract()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekRedAlert)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = SleekTextMuted)
                }
            },
            containerColor = SleekDarkSurface
        )
    }
}
