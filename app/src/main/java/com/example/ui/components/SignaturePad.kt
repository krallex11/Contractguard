package com.example.ui.components

import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint as AndroidPaint
import android.util.Base64
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SleekDarkCardBorder
import com.example.ui.theme.SleekDarkSurface
import com.example.ui.theme.SleekLimeGreenContainer
import com.example.ui.theme.SleekLimeGreenOnPrimary
import com.example.ui.theme.SleekLimeGreenPrimary
import com.example.ui.theme.SleekTextMuted
import com.example.ui.theme.SleekTextWhite
import java.io.ByteArrayOutputStream

@Composable
fun SignaturePad(
    modifier: Modifier = Modifier,
    onSignatureCaptured: (String) -> Unit
) {
    val paths = remember { mutableStateListOf<Path>() }
    var currentPath by remember { mutableStateOf<Path?>(null) }
    var isSigned by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SleekDarkSurface)
            .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Gesture,
                    contentDescription = null,
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "On-Device E-Signature Pad",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = SleekTextWhite
                    )
                )
            }
        }

        Text(
            text = "Draw your signature using finger or stylus in the box below.",
            fontSize = 12.sp,
            color = SleekTextMuted,
            modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
        )

        // Signature Canvas Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF0B0F19))
                .border(1.dp, SleekDarkCardBorder, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        down.consume()
                        val newPath = Path().apply { moveTo(down.position.x, down.position.y) }
                        currentPath = newPath
                        paths.add(newPath)
                        isSigned = true

                        do {
                            val event = awaitPointerEvent()
                            val change = event.changes.firstOrNull()
                            if (change != null && change.pressed) {
                                change.consume()
                                currentPath?.lineTo(change.position.x, change.position.y)
                                if (paths.isNotEmpty()) {
                                    val last = paths.removeAt(paths.size - 1)
                                    paths.add(last)
                                }
                            }
                        } while (event.changes.any { it.pressed })

                        currentPath = null
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                paths.forEach { path ->
                    drawPath(
                        path = path,
                        color = Color(0xFFA3E635),
                        style = Stroke(
                            width = 6f,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            if (paths.isEmpty()) {
                Text(
                    text = "Sign here...",
                    color = SleekTextMuted.copy(alpha = 0.5f),
                    fontSize = 14.sp,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = {
                    paths.clear()
                    isSigned = false
                    onSignatureCaptured("")
                },
                shape = RoundedCornerShape(12.dp),
                enabled = paths.isNotEmpty(),
                modifier = Modifier.testTag("clear_signature_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear",
                    tint = SleekLimeGreenPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear", fontSize = 13.sp, color = SleekTextWhite)
            }

            Button(
                onClick = {
                    if (paths.isNotEmpty()) {
                        val base64 = exportPathsToBase64(paths, 600, 300)
                        onSignatureCaptured(base64)
                    }
                },
                enabled = paths.isNotEmpty(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SleekLimeGreenPrimary,
                    contentColor = SleekLimeGreenOnPrimary
                ),
                modifier = Modifier.testTag("confirm_signature_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Signature",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Confirm E-Signature", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

private fun exportPathsToBase64(paths: List<Path>, targetWidth: Int = 600, targetHeight: Int = 300): String {
    if (paths.isEmpty()) return ""
    return try {
        // Calculate exact bounding rectangle of all drawn signature paths
        val totalBounds = android.graphics.RectF()
        val pathBounds = android.graphics.RectF()

        paths.forEachIndexed { index, composePath ->
            val androidPath = composePath.asAndroidPath()
            androidPath.computeBounds(pathBounds, true)
            if (index == 0) {
                totalBounds.set(pathBounds)
            } else {
                totalBounds.union(pathBounds)
            }
        }

        val pathWidth = if (totalBounds.width() <= 0f) 1f else totalBounds.width()
        val pathHeight = if (totalBounds.height() <= 0f) 1f else totalBounds.height()

        // Create target bitmap canvas
        val bitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
        val canvas = AndroidCanvas(bitmap)
        canvas.drawColor(AndroidColor.WHITE)

        val padding = 32f
        val availWidth = targetWidth - (padding * 2f)
        val availHeight = targetHeight - (padding * 2f)

        // Calculate scaling factor to fit bounds into bitmap
        val scaleX = availWidth / pathWidth
        val scaleY = availHeight / pathHeight
        val scale = minOf(scaleX, scaleY)

        // Calculate offset to center the signature bounding box in the bitmap
        val dx = (targetWidth - (pathWidth * scale)) / 2f - (totalBounds.left * scale)
        val dy = (targetHeight - (pathHeight * scale)) / 2f - (totalBounds.top * scale)

        val paint = AndroidPaint().apply {
            color = AndroidColor.parseColor("#0F172A") // Slate navy black stroke matching web
            style = AndroidPaint.Style.STROKE
            strokeWidth = (6f / scale).coerceIn(3f, 10f)
            strokeCap = AndroidPaint.Cap.ROUND
            strokeJoin = AndroidPaint.Join.ROUND
            isAntiAlias = true
        }

        canvas.save()
        canvas.translate(dx, dy)
        canvas.scale(scale, scale)

        paths.forEach { composePath ->
            canvas.drawPath(composePath.asAndroidPath(), paint)
        }

        canvas.restore()

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        "data:image/png;base64," + Base64.encodeToString(byteArray, Base64.NO_WRAP)
    } catch (e: Exception) {
        ""
    }
}

