package com.vandatgsts.thuyetnguyen.generator

import android.content.ClipData
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.FileProvider
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object InvoiceExportManager {

    suspend fun sharePdf(context: Context, invoice: InvoiceDocument) {
        try {
            val file = PdfInvoiceRenderer.renderPdf(context, invoice)
            val uri = getUriForFile(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, invoice.title.ifBlank { "Hóa Đơn" })
                clipData = ClipData.newRawUri("PDF Invoice", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            grantUriPermissions(context, intent, uri)

            val chooser = Intent.createChooser(intent, "Chia sẻ File PDF hóa đơn (Zalo, In, Email)").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri("PDF Invoice", uri)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi khi chia sẻ PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun shareImage(context: Context, invoice: InvoiceDocument) {
        try {
            val file = ImageInvoiceRenderer.renderPng(context, invoice)
            val uri = getUriForFile(context, file)
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, invoice.title.ifBlank { "Hóa Đơn" })
                clipData = ClipData.newRawUri("Image Invoice", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            grantUriPermissions(context, intent, uri)

            val chooser = Intent.createChooser(intent, "Chia sẻ Ảnh hóa đơn (Zalo, Tin nhắn, Lưu trữ)").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri("Image Invoice", uri)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Lỗi khi chia sẻ Ảnh: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun viewPdf(context: Context, invoice: InvoiceDocument) {
        try {
            val file = PdfInvoiceRenderer.renderPdf(context, invoice)
            val uri = getUriForFile(context, file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                clipData = ClipData.newRawUri("PDF Invoice", uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            grantUriPermissions(context, intent, uri)

            val chooser = Intent.createChooser(intent, "Mở file PDF").apply {
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                clipData = ClipData.newRawUri("PDF Invoice", uri)
            }
            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Không thể mở file PDF: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun saveImageToGallery(context: Context, invoice: InvoiceDocument): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = ImageInvoiceRenderer.renderPng(context, invoice)
            val fileName = "HoaDon_${System.currentTimeMillis()}.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                    put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/TaoHoaDon")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return@withContext false

                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(file).use { input ->
                        input.copyTo(out)
                    }
                }

                values.clear()
                values.put(MediaStore.Images.Media.IS_PENDING, 0)
                resolver.update(uri, values, null, null)
            } else {
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val appDir = File(picturesDir, "TaoHoaDon").apply { mkdirs() }
                val targetFile = File(appDir, fileName)
                FileInputStream(file).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        input.copyTo(output)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun grantUriPermissions(context: Context, intent: Intent, uri: Uri) {
        try {
            val resInfoList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.queryIntentActivities(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(
                    packageName,
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getUriForFile(context: Context, file: File): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }
}
