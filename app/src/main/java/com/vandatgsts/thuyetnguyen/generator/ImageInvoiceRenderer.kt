package com.vandatgsts.thuyetnguyen.generator

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object ImageInvoiceRenderer {

    suspend fun renderBitmap(invoice: InvoiceDocument): Bitmap = withContext(Dispatchers.Default) {
        val width = InvoiceCanvasDrawer.getCanvasWidth(invoice.type)
        val height = InvoiceCanvasDrawer.getCanvasHeight(invoice.type, invoice)

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        InvoiceCanvasDrawer.drawInvoice(canvas, invoice)
        bitmap
    }

    suspend fun renderPng(context: Context, invoice: InvoiceDocument): File = withContext(Dispatchers.IO) {
        val bitmap = renderBitmap(invoice)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "HoaDon_${invoice.type.name}_${System.currentTimeMillis()}.png"
        val outputFile = File(exportDir, fileName)

        FileOutputStream(outputFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        outputFile
    }
}
