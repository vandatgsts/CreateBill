package com.vandatgsts.thuyetnguyen.generator

import android.content.Context
import android.graphics.pdf.PdfDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object PdfInvoiceRenderer {

    suspend fun renderPdf(context: Context, invoice: InvoiceDocument): File = withContext(Dispatchers.IO) {
        val width = InvoiceCanvasDrawer.getCanvasWidth(invoice.type)
        val height = InvoiceCanvasDrawer.getCanvasHeight(invoice.type, invoice)

        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(width, height, 1).create()
        val page = document.startPage(pageInfo)

        InvoiceCanvasDrawer.drawInvoice(page.canvas, invoice)
        document.finishPage(page)

        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "HoaDon_${invoice.type.name}_${System.currentTimeMillis()}.pdf"
        val outputFile = File(exportDir, fileName)

        FileOutputStream(outputFile).use { outputStream ->
            document.writeTo(outputStream)
        }
        document.close()

        outputFile
    }
}
