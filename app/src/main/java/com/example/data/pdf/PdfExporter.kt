package com.example.data.pdf

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ContractEntity
import com.example.data.security.CryptoVault
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfExporter {

    fun generatePdfFile(context: Context, contract: ContractEntity): File? {
        return try {
            val document = PdfDocument()
            // Standard A4 Size: 595 x 842 points
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            // Color Palette
            val primaryBlue = Color.parseColor("#0F172A")
            val accentBlue = Color.parseColor("#0284C7")
            val borderGray = Color.parseColor("#CBD5E1")
            val textDark = Color.parseColor("#1E293B")
            val textMuted = Color.parseColor("#64748B")
            val greenSuccess = Color.parseColor("#059669")
            val bgCard = Color.parseColor("#F8FAFC")

            // Paints
            val textPaint = Paint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = textDark
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            val titlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 14f
                color = primaryBlue
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val subHeaderPaint = Paint().apply {
                isAntiAlias = true
                textSize = 10f
                color = accentBlue
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }

            val linePaint = Paint().apply {
                isAntiAlias = true
                color = borderGray
                strokeWidth = 1f
            }

            var y = 30f

            // 1. Top Header Banner Card
            val headerBox = RectF(30f, y, 565f, y + 42f)
            val headerBgPaint = Paint().apply {
                isAntiAlias = true
                color = Color.parseColor("#F0F9FF")
            }
            canvas.drawRoundRect(headerBox, 8f, 8f, headerBgPaint)

            val headerStrokePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = Color.parseColor("#BAE6FD")
                strokeWidth = 1f
            }
            canvas.drawRoundRect(headerBox, 8f, 8f, headerStrokePaint)

            val bannerTitlePaint = Paint().apply {
                isAntiAlias = true
                textSize = 12f
                color = primaryBlue
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("CONTRACTGUARD E-İMZALI RESMİ SÖZLEŞME BELGESİ", 42f, y + 18f, bannerTitlePaint)

            val dateStr = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(contract.createdAt))
            val metaPaint = Paint().apply {
                isAntiAlias = true
                textSize = 7.5f
                color = accentBlue
            }
            canvas.drawText("Dijital Güvenlik Mührü • Uçtan Uca Şifreli • Oluşturulma: $dateStr", 42f, y + 32f, metaPaint)

            y += 54f

            // 2. Document Title & Type
            canvas.drawText(contract.title.uppercase(Locale("tr")), 30f, y, titlePaint)
            y += 14f

            val typePaint = Paint().apply {
                isAntiAlias = true
                textSize = 8.5f
                color = textMuted
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            canvas.drawText("Contract ID: #${contract.id} • Status: ${contract.status}", 30f, y, typePaint)
            y += 10f

            canvas.drawLine(30f, y, 565f, y, linePaint)
            y += 14f

            // 3. Contract Draft Text Body (Scaled strictly for single-page layout)
            val draftLines = contract.generatedDraftText.split("\n")
            val maxTextWidth = 535f
            val maxBodyY = 560f // Leave plenty of room (280pt) for signatures and security seal

            for (line in draftLines) {
                if (line.startsWith("=") || line.startsWith("-")) continue

                if (line.startsWith("1.") || line.startsWith("2.") || line.startsWith("3.") ||
                    line.startsWith("4.") || line.startsWith("5.") || line.startsWith("6.") || line.startsWith("7.")) {
                    y += 4f
                    if (y > maxBodyY) break
                    canvas.drawText(line, 30f, y, subHeaderPaint)
                    y += 12f
                } else {
                    val words = line.split(" ")
                    var currentLineText = ""

                    for (word in words) {
                        val testLine = if (currentLineText.isEmpty()) word else "$currentLineText $word"
                        if (textPaint.measureText(testLine) <= maxTextWidth) {
                            currentLineText = testLine
                        } else {
                            if (y > maxBodyY) break
                            canvas.drawText(currentLineText, 30f, y, textPaint)
                            y += 11f
                            currentLineText = word
                        }
                    }
                    if (currentLineText.isNotEmpty() && y <= maxBodyY) {
                        canvas.drawText(currentLineText, 30f, y, textPaint)
                        y += 11f
                    }
                }
                if (y > maxBodyY) break
            }

            // Lock y position for Signature Block at 580f to guarantee single-page layout
            y = 580f

            canvas.drawLine(30f, y, 565f, y, linePaint)
            y += 14f

            // 4. E-Signature Header
            canvas.drawText("E-İMZA VE TARAFLARIN ONAYI / ELECTRONIC SIGNATURE & ASSENT", 30f, y, subHeaderPaint)
            y += 14f

            // 5. Dual Signature Boxes (Side-by-Side)
            val boxWidth = 255f
            val boxHeight = 140f
            val leftBoxX = 30f
            val rightBoxX = 310f

            val boxBgPaint = Paint().apply {
                isAntiAlias = true
                color = bgCard
            }
            val boxBorderPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                color = borderGray
                strokeWidth = 1f
            }

            // Party A Box (Left)
            val leftBoxRect = RectF(leftBoxX, y, leftBoxX + boxWidth, y + boxHeight)
            canvas.drawRoundRect(leftBoxRect, 8f, 8f, boxBgPaint)
            canvas.drawRoundRect(leftBoxRect, 8f, 8f, boxBorderPaint)

            // Party B Box (Right)
            val rightBoxRect = RectF(rightBoxX, y, rightBoxX + boxWidth, y + boxHeight)
            canvas.drawRoundRect(rightBoxRect, 8f, 8f, boxBgPaint)
            canvas.drawRoundRect(rightBoxRect, 8f, 8f, boxBorderPaint)

            // Party A Text Details
            val labelBoldPaint = Paint().apply {
                isAntiAlias = true
                textSize = 9f
                color = primaryBlue
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            }
            val labelMutedPaint = Paint().apply {
                isAntiAlias = true
                textSize = 7.5f
                color = textMuted
            }

            canvas.drawText("TARAF A (DÜZENLEYEN)", leftBoxX + 12f, y + 16f, labelMutedPaint)
            canvas.drawText(contract.partyA, leftBoxX + 12f, y + 28f, labelBoldPaint)

            if (!contract.signatureBase64.isNull_or_empty()) {
                val imageBytes = Base64.decode(contract.signatureBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 180, 60, true)
                    canvas.drawBitmap(scaledBitmap, leftBoxX + 12f, y + 36f, null)

                    val statusPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 8f
                        color = greenSuccess
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText("✓ Dijital İmzalandı", leftBoxX + 12f, y + 112f, statusPaint)
                    if (contract.signatureTimestamp != null) {
                        val sDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(contract.signatureTimestamp))
                        canvas.drawText("Tarih: $sDate", leftBoxX + 120f, y + 112f, labelMutedPaint)
                    }
                }
            } else {
                val pendingPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = Color.parseColor("#D97706")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                canvas.drawText("[Taraf A İmzası Bekleniyor]", leftBoxX + 12f, y + 70f, pendingPaint)
            }

            // Party B Text Details
            canvas.drawText("TARAF B (ALICI / UZAKTAN İMZA)", rightBoxX + 12f, y + 16f, labelMutedPaint)
            canvas.drawText(contract.partyB, rightBoxX + 12f, y + 28f, labelBoldPaint)

            if (!contract.partyBSignatureBase64.isNullOrEmpty()) {
                val imageBytes = Base64.decode(contract.partyBSignatureBase64, Base64.NO_WRAP)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 180, 60, true)
                    canvas.drawBitmap(scaledBitmap, rightBoxX + 12f, y + 36f, null)

                    val statusPaint = Paint().apply {
                        isAntiAlias = true
                        textSize = 8f
                        color = greenSuccess
                        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                    }
                    canvas.drawText("✓ Uzaktan İmzalandı", rightBoxX + 12f, y + 112f, statusPaint)
                    if (contract.partyBSignatureTimestamp != null) {
                        val sDate = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("tr")).format(Date(contract.partyBSignatureTimestamp))
                        canvas.drawText("Tarih: $sDate", rightBoxX + 120f, y + 112f, labelMutedPaint)
                    }
                }
            } else {
                // Render empty dashed signature box rectangle for Alıcı / Müşteri
                val emptyBoxPaint = Paint().apply {
                    isAntiAlias = true
                    style = Paint.Style.STROKE
                    color = Color.parseColor("#94A3B8")
                    strokeWidth = 0.8f
                    pathEffect = DashPathEffect(floatArrayOf(4f, 4f), 0f)
                }
                val emptyBoxRect = RectF(rightBoxX + 12f, y + 36f, rightBoxX + boxWidth - 12f, y + 96f)
                canvas.drawRoundRect(emptyBoxRect, 4f, 4f, emptyBoxPaint)

                val boxHintPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8f
                    color = Color.parseColor("#64748B")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                canvas.drawText("Alıcı / Müşteri İmzası İçin Ayrılmıştır", rightBoxX + 22f, y + 62f, boxHintPaint)
                canvas.drawText("(Uzaktan imza bağlantısı ile doldurulacaktır)", rightBoxX + 18f, y + 74f, labelMutedPaint)

                val pendingPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8f
                    color = Color.parseColor("#D97706")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("⏳ Alıcı İmzası Bekleniyor", rightBoxX + 12f, y + 112f, pendingPaint)
            }

            y += boxHeight + 16f

            // 6. SHA-256 Digital Security Verification Seal at Bottom
            val hash = contract.signatureHash ?: CryptoVault.generateESignatureHash(
                contract.title, contract.partyA, contract.partyB, contract.signatureBase64 ?: "", contract.createdAt
            )
            val hashBox = RectF(30f, y, 565f, y + 28f)
            val hashBoxBg = Paint().apply { color = Color.parseColor("#F1F5F9") }
            canvas.drawRoundRect(hashBox, 6f, 6f, hashBoxBg)
            canvas.drawRoundRect(hashBox, 6f, 6f, boxBorderPaint)

            val hashPaint = Paint().apply {
                isAntiAlias = true
                textSize = 7f
                color = Color.parseColor("#334155")
                typeface = Typeface.MONOSPACE
            }
            canvas.drawText("Güvenlik Doğrulama Kodu (SHA-256 Hash): $hash", 38f, y + 17f, hashPaint)

            document.finishPage(page)

            // Save PDF to App Cache directory
            val pdfDir = File(context.cacheDir, "shared_pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, "Sözlesme_${contract.type}_${contract.id}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            document.writeTo(outputStream)
            document.close()
            outputStream.close()

            pdfFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    fun savePdfToPublicDownloads(context: Context, pdfFile: File, contractTitle: String): String {
        return try {
            val sanitizedTitle = contractTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val fileName = "Sozlesme_${sanitizedTitle}_${System.currentTimeMillis()}.pdf"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS + "/ContractGuard")
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    resolver.openOutputStream(uri)?.use { outStream ->
                        FileInputStream(pdfFile).use { inStream ->
                            inStream.copyTo(outStream)
                        }
                    }
                }
            } else {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val subDir = File(downloadsDir, "ContractGuard")
                if (!subDir.exists()) subDir.mkdirs()
                val destFile = File(subDir, fileName)
                FileInputStream(pdfFile).use { inStream ->
                    FileOutputStream(destFile).use { outStream ->
                        inStream.copyTo(outStream)
                    }
                }
            }
            "İndirilenler/ContractGuard/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            "İndirilenler klasörü"
        }
    }

    fun sharePdfViaEmail(context: Context, pdfFile: File, contractTitle: String, recipientEmail: String = "") {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val emailIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                if (recipientEmail.isNotEmpty()) {
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(recipientEmail))
                }
                putExtra(Intent.EXTRA_SUBJECT, "ContractGuard: $contractTitle (E-İmzalı PDF)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Merhaba,\n\nContractGuard e-imza sistemi üzerinden onaylanan '$contractTitle' başlıklı sözleşmenizin e-imzalı güvenli PDF dosyası ekte bilgilerinize sunulmıştır.\n\nİyi çalışmalar."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(emailIntent, "Sözleşmeyi E-Posta ile Gönder")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "E-posta istemcisi başlatılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdfGeneral(context: Context, pdfFile: File, contractTitle: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "ContractGuard: $contractTitle (E-İmzalı PDF)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "ContractGuard üzerinden e-imza ile onaylanmış '$contractTitle' sözleşme belgesi ekte yer almaktadır."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "E-İmzalı PDF Belgesini Paylaş")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Paylaşım başlatılamadı.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAndDownloadPdf(context: Context, pdfFile: File, contractTitle: String) {
        try {
            val savedPathLocation = savePdfToPublicDownloads(context, pdfFile, contractTitle)
            Toast.makeText(context, "PDF kaydedildi: $savedPathLocation", Toast.LENGTH_LONG).show()

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "$contractTitle - PDF Aç / İncele")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            sharePdfGeneral(context, pdfFile, contractTitle)
        }
    }
}
