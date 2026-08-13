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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
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
    onAttachPartyBSignature: (String) -> Unit = {},
    onDownloadPdf: () -> Unit = {},
    onExportAndSharePdf: (String) -> Unit,
    onDeleteContract: () -> Unit,
    onBackClicked: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var webHostBaseUrl by remember { mutableStateOf("https://contractguard-5511.vercel.app") }

    var showDomainDialog by remember { mutableStateOf(false) }
    var tempDomainInput by remember { mutableStateOf(webHostBaseUrl) }

    val encodedTitle = remember(contract.title) { java.net.URLEncoder.encode(contract.title, "UTF-8") }
    val encodedPartyA = remember(contract.partyA) { java.net.URLEncoder.encode(contract.partyA, "UTF-8") }
    val encodedPartyB = remember(contract.partyB) { java.net.URLEncoder.encode(contract.partyB, "UTF-8") }

    val encodedSigA = remember(contract.signatureBase64) {
        if (!contract.signatureBase64.isNullOrEmpty()) {
            try { java.net.URLEncoder.encode(contract.signatureBase64, "UTF-8") } catch (e: Exception) { "" }
        } else ""
    }

    // Short, clean & concise remote signing URL with compact dynamic signature
    val remoteSigningLink = remember(webHostBaseUrl, contract.id, encodedPartyA, encodedPartyB, encodedTitle, encodedSigA) {
        "$webHostBaseUrl/#id=${contract.id}&partyA=$encodedPartyA&partyB=$encodedPartyB&title=$encodedTitle${if (encodedSigA.isNotEmpty()) "&sigA=$encodedSigA" else ""}"
    }
    val hasUserSigned = !contract.signatureBase64.isNullOrEmpty() || contract.status == "SIGNED"

    val partyASigBitmap = remember(contract.signatureBase64) {
        contract.signatureBase64?.let { base64 ->
            try {
                val cleanStr = base64.substringAfter("base64,")
                val bytes = Base64.decode(cleanStr, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        }
    }

    val partyBSigBitmap = remember(contract.partyBSignatureBase64) {
        contract.partyBSignatureBase64?.let { base64 ->
            try {
                val cleanStr = base64.substringAfter("base64,")
                val bytes = Base64.decode(cleanStr, Base64.DEFAULT)
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
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = SleekTextWhite
                            )
                        )
                        Text(
                            text = "Sözleşme Taslağı & İnceleme",
                            fontSize = 11.5.sp,
                            color = SleekTextMuted
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClicked, modifier = Modifier.testTag("back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Geri",
                            tint = SleekLimeGreenPrimary
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (hasUserSigned) {
                                onExportAndSharePdf("")
                            } else {
                                Toast.makeText(context, "Lütfen önce kendi imzanızı atın.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.testTag("share_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "PDF Paylaş",
                            tint = SleekLimeGreenPrimary
                        )
                    }

                    IconButton(
                        onClick = {
                            if (hasUserSigned) {
                                onDownloadPdf()
                            } else {
                                Toast.makeText(context, "Lütfen önce kendi imzanızı atın.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = hasUserSigned,
                        modifier = Modifier.testTag("download_pdf_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = "PDF İndir",
                            tint = if (hasUserSigned) SleekLimeGreenPrimary else SleekTextMuted
                        )
                    }

                    IconButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.testTag("delete_contract_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Sil",
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
                        .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
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
                                imageVector = if (hasUserSigned) Icons.Default.CheckCircle else Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = SleekLimeGreenPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (hasUserSigned) "E-İmzalı Sözleşme Belgesi" else "Sözleşme Taslağı Onay Bekliyor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = SleekTextWhite
                            )
                            Text(
                                text = "5070 Sayılı Elektronik İmza Kanunu Uyumlu • SHA-256 Doğrulamalı",
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
                                text = "SÖZLEŞME METNİ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SleekLimeGreenPrimary,
                                letterSpacing = 0.5.sp
                            )
                            val dateStr = SimpleDateFormat("dd.MM.yyyy", Locale("tr")).format(Date(contract.createdAt))
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

            // Signature Pad or E-Signature Seal Display (Taraf A)
            item {
                if (contract.status == "SIGNED" && !contract.signatureBase64.isNullOrEmpty()) {
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
                                    text = "Kendi İmzanız (Taraf A - Düzenleyen)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SleekTextWhite
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "İmzalayan: ${contract.partyA}",
                                fontSize = 12.sp,
                                color = SleekTextWhite,
                                fontWeight = FontWeight.SemiBold
                            )

                            if (partyASigBitmap != null) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(50.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = partyASigBitmap,
                                        contentDescription = "Taraf A İmza Görseli",
                                        modifier = Modifier.fillMaxHeight()
                                    )
                                }
                            }

                            if (contract.signatureTimestamp != null) {
                                Spacer(modifier = Modifier.height(4.dp))
                                val signDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr")).format(Date(contract.signatureTimestamp))
                                Text(
                                    text = "İmza Tarihi: $signDate",
                                    fontSize = 11.5.sp,
                                    color = SleekTextMuted
                                )
                            }

                            if (!contract.partyBSignatureBase64.isNullOrEmpty()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = SleekDarkCardBorder)

                                Text(
                                    text = "Alıcı / Müşteri İmzası (Taraf B): ${contract.partyB}",
                                    fontSize = 12.sp,
                                    color = SleekLimeGreenPrimary,
                                    fontWeight = FontWeight.Bold
                                )

                                if (partyBSigBitmap != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(50.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color.White)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            bitmap = partyBSigBitmap,
                                            contentDescription = "Taraf B İmza Görseli",
                                            modifier = Modifier.fillMaxHeight()
                                        )
                                    }
                                }

                                if (contract.partyBSignatureTimestamp != null) {
                                    val partyBSignDate = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("tr")).format(Date(contract.partyBSignatureTimestamp))
                                    Text(
                                        text = "Alıcı İmza Tarihi: $partyBSignDate (Web Portalı İle Onaylandı)",
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
                                        text = "SHA-256 Mühür: ${contract.signatureHash}",
                                        fontSize = 10.5.sp,
                                        fontFamily = FontFamily.Monospace,
                                        color = SleekLimeGreenPrimary
                                    )
                                }
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

            // Bottom Action Card for PDF Download & Share
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (hasUserSigned) SleekLimeGreenPrimary.copy(alpha = 0.5f) else SleekDarkCardBorder, RoundedCornerShape(20.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureAsPdf,
                                contentDescription = null,
                                tint = if (hasUserSigned) SleekLimeGreenPrimary else SleekTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "E-İmzalı PDF Belgeniz",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = SleekTextWhite
                                )
                                Text(
                                    text = if (hasUserSigned) "Sözleşmeyi cihazınıza indirin veya paylaşın." else "İmzanızı attıktan sonra PDF indirilebilir.",
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
                                onClick = {
                                    if (hasUserSigned) {
                                        onDownloadPdf()
                                    } else {
                                        Toast.makeText(context, "Lütfen önce kendi imzanızı atın.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = hasUserSigned,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = SleekLimeGreenPrimary,
                                    contentColor = SleekLimeGreenOnPrimary,
                                    disabledContainerColor = SleekDarkCardBorder,
                                    disabledContentColor = SleekTextMuted
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
                                Text("PDF İndir", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    if (hasUserSigned) {
                                        onExportAndSharePdf("")
                                    } else {
                                        Toast.makeText(context, "Lütfen önce kendi imzanızı atın.", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = hasUserSigned,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (hasUserSigned) SleekLimeGreenPrimary else SleekDarkCardBorder),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(46.dp)
                                    .testTag("share_pdf_bottom_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = null,
                                    tint = if (hasUserSigned) SleekLimeGreenPrimary else SleekTextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PDF Paylaş", color = if (hasUserSigned) SleekTextWhite else SleekTextMuted, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Remote Counterparty E-Signature Module at Very Bottom
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SleekDarkSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (hasUserSigned) SleekLimeGreenPrimary.copy(alpha = 0.3f) else SleekDarkCardBorder, RoundedCornerShape(20.dp))
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
                                    tint = if (hasUserSigned) SleekLimeGreenPrimary else SleekTextMuted,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Uzaktan E-İmza Bağlantısı (Alıcı)",
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
                                        text = "Alıcı İmzalandı",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SleekLimeGreenPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!hasUserSigned) {
                            // Locked State Notice
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F172A))
                                    .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(12.dp))
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "🔒 Kendi imzanızı attıktan sonra Alıcı / Müşteri için uzaktan imza bağlantısı ve QR kodu otomatik olarak aktifleşecektir.",
                                    fontSize = 12.sp,
                                    color = SleekTextMuted,
                                    lineHeight = 17.sp
                                )
                            }
                        } else {
                            // Unlocked Active State
                            Text(
                                text = "${contract.partyB} kullanıcısının uygulamayı yüklemeden web ortamında sözleşmeyi inceleyip imzalaması için aşağıdaki bağlantıyı paylaşın:",
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
                                        contentDescription = "Domain Düzenle",
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
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Signing Link", remoteSigningLink))
                                        Toast.makeText(context, "Uzaktan imza bağlantısı kopyalandı!", Toast.LENGTH_SHORT).show()
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
                                    Text("Linki Kopyala", fontSize = 11.sp, color = SleekTextWhite)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val sendIntent: Intent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, "Lütfen '${contract.title}' sözleşmesini inceleyip imzalamak için bağlantıyı açın:\n$remoteSigningLink")
                                            type = "text/plain"
                                        }
                                        val shareIntent = Intent.createChooser(sendIntent, "Uzaktan İmza Bağlantısını Paylaş")
                                        context.startActivity(shareIntent)
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
                                    Text("Linki Paylaş", fontSize = 11.sp, color = SleekTextWhite)
                                }

                                OutlinedButton(
                                    onClick = { showQrDialog = true },
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
                                    Text("QR Göster", fontSize = 11.sp, color = SleekTextWhite)
                                }
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
                    text = "Uzaktan İmza QR Kodu",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SleekTextWhite
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${contract.partyB} kullanıcısının telefon kamerasından bu QR kodu okutarak sözleşme imzalama sayfasına erişmesini sağlayın.",
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
                        text = "5070 Sayılı Kanuna Uygundur",
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
                    Text("Kapat")
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
                    text = "Web Hosting Domain Ayarla",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = SleekTextWhite
                )
            },
            text = {
                Column {
                    Text(
                        text = "Vercel üretim alan adınızı (örn: https://contractguard-5511.vercel.app) giriniz.\n\nNot: '-git-main-' veya preview linkleri Vercel koruması nedeniyle giriş ekranına yönlendirebilir.",
                        fontSize = 12.sp,
                        color = SleekTextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempDomainInput,
                        onValueChange = { tempDomainInput = it },
                        placeholder = { Text("https://sizin-projeniz.vercel.app", fontSize = 12.sp, color = SleekTextMuted) },
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
                    Text("Kaydet")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDomainDialog = false }) {
                    Text("İptal", color = SleekTextMuted)
                }
            },
            containerColor = SleekDarkSurface
        )
    }

    // Delete Confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Sözleşme Silinsin mi?", fontWeight = FontWeight.Bold, color = SleekTextWhite) },
            text = { Text("Bu işlem sözleşmeyi cihazınızdan kalıcı olarak silecektir.", fontSize = 13.sp, color = SleekTextMuted) },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirm = false
                        onDeleteContract()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SleekRedAlert)
                ) {
                    Text("Kalıcı Olarak Sil")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("İptal", color = SleekTextMuted)
                }
            },
            containerColor = SleekDarkSurface
        )
    }
}
