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

    fun generatePdfFile(context: Context, rawContract: ContractEntity): File? {
        return try {
            val contract = rawContract.copy(
                title = rawContract.title.toEnglishAscii(),
                partyA = rawContract.partyA.toEnglishAscii(),
                partyB = rawContract.partyB.toEnglishAscii(),
                generatedDraftText = rawContract.generatedDraftText.toEnglishAscii()
            )
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
            canvas.drawText("CONTRACTGUARD OFFICIAL E-SIGNED CONTRACT RECORD", 42f, y + 18f, bannerTitlePaint)

            val dateStr = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US).format(Date(contract.createdAt))
            val metaPaint = Paint().apply {
                isAntiAlias = true
                textSize = 7.5f
                color = accentBlue
            }
            canvas.drawText("Digital Security Seal • End-to-End Encrypted • Created: $dateStr", 42f, y + 32f, metaPaint)

            y += 54f

            // 2. Document Title & Type
            canvas.drawText(contract.title.uppercase(Locale.US), 30f, y, titlePaint)
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
            canvas.drawText("ELECTRONIC SIGNATURES & ASSENT RECORD", 30f, y, subHeaderPaint)
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

            canvas.drawText("PARTY A (ISSUER)", leftBoxX + 12f, y + 16f, labelMutedPaint)
            canvas.drawText(contract.partyA, leftBoxX + 12f, y + 28f, labelBoldPaint)

            if (!contract.signatureBase64.isNull_or_empty()) {
                val cleanSigA = contract.signatureBase64?.substringAfter("base64,") ?: ""
                val imageBytes = Base64.decode(cleanSigA, Base64.DEFAULT)
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
                    canvas.drawText("✓ Digitally Signed", leftBoxX + 12f, y + 112f, statusPaint)
                    if (contract.signatureTimestamp != null) {
                        val sDate = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US).format(Date(contract.signatureTimestamp))
                        canvas.drawText("Date: $sDate", leftBoxX + 120f, y + 112f, labelMutedPaint)
                    }
                }
            } else {
                val pendingPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8.5f
                    color = Color.parseColor("#D97706")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC)
                }
                canvas.drawText("[Party A Signature Pending]", leftBoxX + 12f, y + 70f, pendingPaint)
            }

            // Party B Text Details
            canvas.drawText("PARTY B (RECIPIENT / SIGNATORY)", rightBoxX + 12f, y + 16f, labelMutedPaint)
            canvas.drawText(contract.partyB, rightBoxX + 12f, y + 28f, labelBoldPaint)

            if (!contract.partyBSignatureBase64.isNullOrEmpty()) {
                val cleanSigB = contract.partyBSignatureBase64?.substringAfter("base64,") ?: ""
                val imageBytes = Base64.decode(cleanSigB, Base64.DEFAULT)
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
                    canvas.drawText("✓ Remotely Signed", rightBoxX + 12f, y + 112f, statusPaint)
                    if (contract.partyBSignatureTimestamp != null) {
                        val sDate = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.US).format(Date(contract.partyBSignatureTimestamp))
                        canvas.drawText("Date: $sDate", rightBoxX + 120f, y + 112f, labelMutedPaint)
                    }
                }
            } else {
                // Render empty dashed signature box rectangle for Party B
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
                canvas.drawText("Reserved for Recipient Signature", rightBoxX + 22f, y + 62f, boxHintPaint)
                canvas.drawText("(Will be populated via remote link)", rightBoxX + 18f, y + 74f, labelMutedPaint)

                val pendingPaint = Paint().apply {
                    isAntiAlias = true
                    textSize = 8f
                    color = Color.parseColor("#D97706")
                    typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                }
                canvas.drawText("⏳ Recipient Signature Pending", rightBoxX + 12f, y + 112f, pendingPaint)
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
            canvas.drawText("Verification Seal Hash (SHA-256): $hash", 38f, y + 17f, hashPaint)

            document.finishPage(page)

            // Save PDF to App Cache directory
            val pdfDir = File(context.cacheDir, "shared_pdfs")
            if (!pdfDir.exists()) pdfDir.mkdirs()

            val pdfFile = File(pdfDir, "Contract_${contract.type}_${contract.id}.pdf")
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

    fun String.toEnglishAscii(): String {
        return this
            .replace("ç", "c").replace("Ç", "C")
            .replace("ğ", "g").replace("Ğ", "G")
            .replace("ı", "i").replace("İ", "I")
            .replace("ö", "o").replace("Ö", "O")
            .replace("ş", "s").replace("Ş", "S")
            .replace("ü", "u").replace("Ü", "U")
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()

    fun savePdfToPublicDownloads(context: Context, pdfFile: File, contractTitle: String): String {
        return try {
            val sanitizedTitle = contractTitle.replace(Regex("[^a-zA-Z0-9_]"), "_")
            val fileName = "Contract_${sanitizedTitle}_${System.currentTimeMillis()}.pdf"

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
            "Downloads/ContractGuard/$fileName"
        } catch (e: Exception) {
            e.printStackTrace()
            "Downloads folder"
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
                putExtra(Intent.EXTRA_SUBJECT, "ContractGuard: $contractTitle (E-Signed PDF)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Hello,\n\nPlease find attached the electronically signed, secure PDF copy of the contract '$contractTitle' executed via ContractGuard.\n\nBest regards."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(emailIntent, "Send Contract via Email")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not launch email client.", Toast.LENGTH_SHORT).show()
        }
    }

    fun sharePdfGeneral(context: Context, pdfFile: File, contractTitle: String) {
        try {
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_SUBJECT, "ContractGuard: $contractTitle (E-Signed PDF)")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the electronically signed contract document '$contractTitle' verified via ContractGuard."
                )
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share E-Signed PDF Document")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Could not launch share sheet.", Toast.LENGTH_SHORT).show()
        }
    }

    fun openAndDownloadPdf(context: Context, pdfFile: File, contractTitle: String) {
        try {
            val savedPathLocation = savePdfToPublicDownloads(context, pdfFile, contractTitle)
            Toast.makeText(context, "PDF saved to: $savedPathLocation", Toast.LENGTH_LONG).show()

            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(contentUri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(viewIntent, "$contractTitle - Open PDF")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            sharePdfGeneral(context, pdfFile, contractTitle)
        }
    }
}
