package com.vandatgsts.thuyetnguyen.generator

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface

import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceItem
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import kotlin.math.ceil
import kotlin.math.max

object InvoiceCanvasDrawer {

    private const val DELIVERY_NOTE_WIDTH = 200f
    private const val QUOTATION_NOTE_WIDTH = 160f

    fun getCanvasWidth(type: InvoiceType): Int = when (type) {
        InvoiceType.DELIVERY_DEBT -> 1400
        InvoiceType.QUOTATION_A4 -> 1000
    }

    fun getCanvasHeight(type: InvoiceType, invoice: InvoiceDocument): Int = when (type) {
        InvoiceType.DELIVERY_DEBT -> {
            val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            }
            val rowsHeight = invoice.items.sumOf { getDeliveryRowHeight(it, paint).toDouble() }
            val hasDebtTable = invoice.debtPayments.isNotEmpty() ||
                (invoice.initialOldDebt != 0.0 && invoice.initialOldDebt != invoice.effectiveOldDebt)
            val debtTableHeight = if (hasDebtTable) (invoice.debtPayments.size + 2) * 34 else 0
            max(700, ceil(129 + rowsHeight + 98 + debtTableHeight + 80).toInt())
        }
        InvoiceType.QUOTATION_A4 -> {
            // Canvas rỗng không cấp phát bitmap. Chạy cùng bố cục với lần vẽ thật
            // để đo cả địa chỉ, ghi chú, VAT, ngân hàng và con dấu.
            val contentBottom = drawQuotationA4Template(Canvas(), invoice)
            max(1414, ceil(contentBottom + 80f).toInt())
        }
    }

    fun drawInvoice(canvas: Canvas, invoice: InvoiceDocument) {
        canvas.drawColor(Color.WHITE)
        when (invoice.type) {
            InvoiceType.DELIVERY_DEBT -> drawDeliveryDebtTemplate(canvas, invoice)
            InvoiceType.QUOTATION_A4 -> drawQuotationA4Template(canvas, invoice)
        }
    }

    private enum class QuotationColType {
        STT, PRODUCT_NAME, UNIT, QUANTITY, UNIT_PRICE, NOTE, LINE_TOTAL
    }

    private data class QuotationColDef(
        val type: QuotationColType,
        val title: String,
        var width: Float
    )

    private fun createQuotationColumns(invoice: InvoiceDocument, tableWidth: Float): List<QuotationColDef> {
        val columns = mutableListOf<QuotationColDef>()
        columns.add(QuotationColDef(QuotationColType.STT, "STT", 55f))
        val productColumn = QuotationColDef(QuotationColType.PRODUCT_NAME, "NỘI DUNG DỊCH VỤ", 0f)
        columns.add(productColumn)
        if (invoice.showUnitCol) columns.add(QuotationColDef(QuotationColType.UNIT, "ĐVT", 95f))
        if (invoice.showQuantityCol) columns.add(QuotationColDef(QuotationColType.QUANTITY, "SỐ LƯỢNG", 115f))
        if (invoice.showUnitPriceCol) columns.add(QuotationColDef(QuotationColType.UNIT_PRICE, "ĐƠN GIÁ", 140f))
        columns.add(QuotationColDef(QuotationColType.NOTE, "GHI CHÚ", QUOTATION_NOTE_WIDTH))
        columns.add(QuotationColDef(QuotationColType.LINE_TOTAL, "THÀNH TIỀN", 140f))
        val fixedWidth = columns.filter { it.type != QuotationColType.PRODUCT_NAME }
            .sumOf { it.width.toDouble() }.toFloat()
        productColumn.width = tableWidth - fixedWidth
        return columns
    }

    private fun getDeliveryRowHeight(item: InvoiceItem, paint: Paint): Float {
        val noteLines = getWrappedLines(item.note, DELIVERY_NOTE_WIDTH - 16f, paint)
        return max(48f, noteLines.size * (14f * 1.4f) + 16f)
    }

    private fun getQuotationRowHeight(item: InvoiceItem, columns: List<QuotationColDef>, paint: Paint): Float {
        val productWidth = columns.first { it.type == QuotationColType.PRODUCT_NAME }.width
        val productLines = getWrappedLines(item.productName, productWidth - 20f, paint)
        val noteLines = getWrappedLines(item.note, QUOTATION_NOTE_WIDTH - 20f, paint)
        val lineCount = maxOf(1, productLines.size, noteLines.size)
        return max(120f, lineCount * (17f * 1.4f) + 50f)
    }

    private fun drawDeliveryDebtTemplate(canvas: Canvas, invoice: InvoiceDocument) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = Color.BLACK
        }

        val canvasWidth = getCanvasWidth(InvoiceType.DELIVERY_DEBT)
        val marginLeft = 40f
        val marginRight = canvasWidth - 40f
        val tableWidth = marginRight - marginLeft
        var currentY = 50f

        // 1. Tiêu đề Cửa hàng
        paint.color = Color.BLACK
        paint.textSize = 28f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val titleText = if (invoice.storeOrCompanyName.isNotBlank()) invoice.storeOrCompanyName else "Cửa Hàng Kiều Phát"
        canvas.drawText(titleText, canvasWidth / 2f, currentY, paint)

        currentY += 35f

        // 2. Định nghĩa các cột
        // Giữ chiều rộng các cột cũ và dành thêm 200px cho Ghi chú trước các cột tiền.
        val colWidths = floatArrayOf(45f, 90f, 210f, 65f, 105f, 165f, 105f, 115f, DELIVERY_NOTE_WIDTH, 105f, 115f)
        val colHeaders = arrayOf(
            "STT", "Ngày", "Sản phẩm", "Số lượng", "Đơn giá",
            "Địa chỉ", "Người nhận", "Số điện thoại", "Ghi chú", "Đã Thu", "Thành Tiền"
        )

        val headerHeight = 44f
        val tableTop = currentY

        // Vẽ Header Table
        var colX = marginLeft
        paint.textSize = 15f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        for (i in colHeaders.indices) {
            val w = colWidths[i]
            canvas.drawRect(colX, tableTop, colX + w, tableTop + headerHeight, strokePaint)
            val headerTitle = colHeaders[i]
            canvas.drawText(headerTitle, colX + w / 2f, tableTop + headerHeight / 2f + 5f, paint)
            colX += w
        }

        currentY += headerHeight

        // Vẽ các dòng dữ liệu (Item Rows)
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

        val items = invoice.items
        for (i in items.indices) {
            val item = items[i]
            val rowHeight = getDeliveryRowHeight(item, paint)
            val rowTop = currentY
            val rowBottom = rowTop + rowHeight
            colX = marginLeft

            // Cột 0: STT
            canvas.drawRect(colX, rowTop, colX + colWidths[0], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("${item.stt}", colX + colWidths[0] / 2f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[0]

            // Cột 1: Ngày
            canvas.drawRect(colX, rowTop, colX + colWidths[1], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(item.date, colX + colWidths[1] / 2f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[1]

            // Cột 2: Sản phẩm (Left)
            canvas.drawRect(colX, rowTop, colX + colWidths[2], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(truncateText(item.productName, colWidths[2] - 16f, paint), colX + 8f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[2]

            // Cột 3: Số lượng (Center)
            canvas.drawRect(colX, rowTop, colX + colWidths[3], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(FormatHelper.formatQuantity(item.quantity), colX + colWidths[3] / 2f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[3]

            // Cột 4: Đơn giá (Right)
            canvas.drawRect(colX, rowTop, colX + colWidths[4], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(item.unitPrice), colX + colWidths[4] - 8f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[4]

            // Cột 5: Địa chỉ (Left)
            canvas.drawRect(colX, rowTop, colX + colWidths[5], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(truncateText(item.address, colWidths[5] - 16f, paint), colX + 8f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[5]

            // Cột 6: Người nhận (Left/Center)
            canvas.drawRect(colX, rowTop, colX + colWidths[6], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(truncateText(item.receiver, colWidths[6] - 10f, paint), colX + colWidths[6] / 2f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[6]

            // Cột 7: Số điện thoại (Center)
            canvas.drawRect(colX, rowTop, colX + colWidths[7], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText(item.phone, colX + colWidths[7] / 2f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[7]

            // Cột 8: Ghi chú (tự xuống dòng, không cắt nội dung)
            canvas.drawRect(colX, rowTop, colX + colWidths[8], rowBottom, strokePaint)
            drawVerticallyCenteredWrappedText(
                canvas = canvas,
                lines = getWrappedLines(item.note, colWidths[8] - 16f, paint),
                centerX = colX + colWidths[8] / 2f,
                rowTop = rowTop,
                rowHeight = rowHeight,
                textSize = 14f,
                paint = paint
            )
            colX += colWidths[8]

            // Cột 9: Đã Thu (Right)
            canvas.drawRect(colX, rowTop, colX + colWidths[9], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(item.paidAmount), colX + colWidths[9] - 8f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[9]

            // Cột 10: Thành Tiền (Right)
            canvas.drawRect(colX, rowTop, colX + colWidths[10], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(item.lineTotalM1), colX + colWidths[10] - 8f, rowTop + rowHeight / 2f + 5f, paint)

            currentY += rowHeight
        }

        // Footer 1: Nợ cũ
        val footer1Top = currentY
        val footer1Bottom = footer1Top + 38f
        val mergedColsWidth = tableWidth - colWidths[10]

        canvas.drawRect(marginLeft, footer1Top, marginLeft + mergedColsWidth, footer1Bottom, strokePaint)
        canvas.drawRect(marginLeft + mergedColsWidth, footer1Top, marginRight, footer1Bottom, strokePaint)

        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val oldDebtLabel = if (invoice.effectiveOldDebt < 0) "Tiền thừa kỳ trước" else "Nợ cũ"
        canvas.drawText(oldDebtLabel, marginLeft + mergedColsWidth / 2f, footer1Top + 24f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(FormatHelper.formatMoney(invoice.effectiveOldDebt), marginRight - 8f, footer1Top + 24f, paint)

        currentY += 38f

        // Footer 2: Thành tiền tổng cộng
        val footer2Top = currentY
        val footer2Bottom = footer2Top + 38f
        canvas.drawRect(marginLeft, footer2Top, marginLeft + mergedColsWidth, footer2Bottom, strokePaint)
        canvas.drawRect(marginLeft + mergedColsWidth, footer2Top, marginRight, footer2Bottom, strokePaint)

        paint.color = Color.parseColor("#008000") // Màu xanh lá như trong ảnh mẫu
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val totalLabel = if (invoice.totalAmount < 0) "Khách trả dư" else "Thành tiền"
        canvas.drawText(totalLabel, marginLeft + mergedColsWidth / 2f, footer2Top + 24f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(FormatHelper.formatMoney(invoice.totalAmount), marginRight - 8f, footer2Top + 24f, paint)

        currentY += 60f

        // Bảng phụ chi tiết các đợt Chuyển khoản / Trừ nợ cũ (Khớp mẫu img_4.png)
        if (invoice.debtPayments.isNotEmpty() || (invoice.initialOldDebt != 0.0 && invoice.initialOldDebt != invoice.effectiveOldDebt)) {
            val subTableLeft = marginLeft
            val subCol1Width = 190f
            val subCol2Width = 160f
            val subRowHeight = 34f
            var subY = currentY

            val baseDebt = if (invoice.initialOldDebt != 0.0) invoice.initialOldDebt else invoice.oldDebt

            // Dòng 1: Nợ cũ
            canvas.drawRect(subTableLeft, subY, subTableLeft + subCol1Width, subY + subRowHeight, strokePaint)
            canvas.drawRect(subTableLeft + subCol1Width, subY, subTableLeft + subCol1Width + subCol2Width, subY + subRowHeight, strokePaint)
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val subBaseLabel = if (baseDebt < 0) "Tiền thừa" else "Nợ cũ"
            canvas.drawText(subBaseLabel, subTableLeft + subCol1Width / 2f, subY + 22f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(baseDebt), subTableLeft + subCol1Width + subCol2Width - 10f, subY + 22f, paint)
            subY += subRowHeight

            // Các đợt chuyển khoản
            invoice.debtPayments.forEach { payment ->
                canvas.drawRect(subTableLeft, subY, subTableLeft + subCol1Width, subY + subRowHeight, strokePaint)
                canvas.drawRect(subTableLeft + subCol1Width, subY, subTableLeft + subCol1Width + subCol2Width, subY + subRowHeight, strokePaint)
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                paint.textAlign = Paint.Align.CENTER
                val title = if (payment.date.isNotBlank()) "${payment.title} (${payment.date})" else payment.title
                canvas.drawText(title, subTableLeft + subCol1Width / 2f, subY + 22f, paint)
                paint.textAlign = Paint.Align.RIGHT
                canvas.drawText(FormatHelper.formatMoney(payment.amount), subTableLeft + subCol1Width + subCol2Width - 10f, subY + 22f, paint)
                subY += subRowHeight
            }

            // Dòng cuối: Nợ cũ còn lại / Khách trả dư
            canvas.drawRect(subTableLeft, subY, subTableLeft + subCol1Width, subY + subRowHeight, strokePaint)
            canvas.drawRect(subTableLeft + subCol1Width, subY, subTableLeft + subCol1Width + subCol2Width, subY + subRowHeight, strokePaint)
            paint.color = if (invoice.effectiveOldDebt < 0) Color.parseColor("#008000") else Color.parseColor("#CC0000")
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val remainingLabel = if (invoice.effectiveOldDebt < 0) "Khách trả dư" else "Nợ cũ còn lại"
            canvas.drawText(remainingLabel, subTableLeft + subCol1Width / 2f, subY + 22f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(invoice.effectiveOldDebt), subTableLeft + subCol1Width + subCol2Width - 10f, subY + 22f, paint)
        }

        // Đóng dấu ĐÃ THANH TOÁN nổi bật nếu hóa đơn đã được thu tiền hoặc thanh toán dư
        if (invoice.isFullyPaid) {
            val dateLabel = if (invoice.paidDate.isNotBlank()) "NGÀY: ${invoice.paidDate}" else "NGÀY: ${FormatHelper.formatDate(invoice.updatedAt)}"
            drawPaidStamp(canvas, marginRight - 160f, footer2Top + 19f, dateLabel, -7f)
        }
    }

    private fun drawQuotationA4Template(canvas: Canvas, invoice: InvoiceDocument): Float {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f
            color = Color.BLACK
        }

        val canvasWidth = getCanvasWidth(InvoiceType.QUOTATION_A4)
        val marginLeft = 50f
        val marginRight = canvasWidth - 50f
        val tableWidth = marginRight - marginLeft
        var currentY = 70f

        // 1. Tiêu đề lớn BẢNG BÁO GIÁ
        paint.color = Color.BLACK
        paint.textSize = 34f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val mainTitle = if (invoice.title.isNotBlank()) invoice.title else "BẢNG BÁO GIÁ"
        canvas.drawText(mainTitle, canvasWidth / 2f, currentY, paint)

        currentY += 50f

        // 2. Thông tin Công ty & Khách hàng
        val labelX = marginLeft
        val colonX = marginLeft + 195f
        val valueX = marginLeft + 210f
        val maxValueWidth = marginRight - valueX

        paint.textSize = 17f

        // 2.1. Khối Thông tin Bên Bán / Công ty
        var drewCompanyInfo = false
        if (invoice.showCompanyInfo) {
            // Tên công ty
            if (invoice.storeOrCompanyName.isNotBlank()) {
                currentY = drawInfoRow(
                    canvas = canvas,
                    label = "Tên công ty",
                    value = invoice.storeOrCompanyName,
                    labelX = labelX,
                    colonX = colonX,
                    valueX = valueX,
                    startY = currentY,
                    maxWidth = maxValueWidth,
                    paint = paint,
                    isBoldValue = true
                )
                drewCompanyInfo = true
            }

            // Địa chỉ công ty
            if (invoice.showCompanyAddress && invoice.companyAddress.isNotBlank()) {
                currentY = drawInfoRow(
                    canvas = canvas,
                    label = "Địa chỉ công ty",
                    value = invoice.companyAddress,
                    labelX = labelX,
                    colonX = colonX,
                    valueX = valueX,
                    startY = currentY,
                    maxWidth = maxValueWidth,
                    paint = paint,
                    isBoldValue = false
                )
                drewCompanyInfo = true
            }

            // Mã số thuế
            if (invoice.showCompanyTaxCode && invoice.companyTaxCode.isNotBlank()) {
                currentY = drawInfoRow(
                    canvas = canvas,
                    label = "Mã số thuế",
                    value = invoice.companyTaxCode,
                    labelX = labelX,
                    colonX = colonX,
                    valueX = valueX,
                    startY = currentY,
                    maxWidth = maxValueWidth,
                    paint = paint,
                    isBoldValue = false
                )
                drewCompanyInfo = true
            }
        }

        if (drewCompanyInfo) {
            currentY += 8f
        }

        // 2.2. Khối Thông tin Khách Hàng
        var drewCustomerInfo = false
        if (invoice.showCustomerInfo) {
            // Khách hàng
            if (invoice.customer.name.isNotBlank()) {
                currentY = drawInfoRow(
                    canvas = canvas,
                    label = "Khách hàng",
                    value = invoice.customer.name,
                    labelX = labelX,
                    colonX = colonX,
                    valueX = valueX,
                    startY = currentY,
                    maxWidth = maxValueWidth,
                    paint = paint,
                    isBoldValue = true
                )
                drewCustomerInfo = true
            }

            // Địa chỉ khách hàng
            if (invoice.showCustomerAddress && invoice.customer.address.isNotBlank()) {
                currentY = drawInfoRow(
                    canvas = canvas,
                    label = "Địa chỉ khách hàng",
                    value = invoice.customer.address,
                    labelX = labelX,
                    colonX = colonX,
                    valueX = valueX,
                    startY = currentY,
                    maxWidth = maxValueWidth,
                    paint = paint,
                    isBoldValue = false
                )
                drewCustomerInfo = true
            }
        }

        if (drewCustomerInfo || drewCompanyInfo) {
            currentY += 15f
        }

        // 3. BẢNG DỊCH VỤ / SẢN PHẨM (Tự động thích ứng các cột hiển thị)
        val colDefs = createQuotationColumns(invoice, tableWidth)
        val prodColDef = colDefs.first { it.type == QuotationColType.PRODUCT_NAME }
        val lineTotalColWidth = colDefs.first { it.type == QuotationColType.LINE_TOTAL }.width

        val headerHeight = 55f
        val tableTop = currentY

        var colX = marginLeft
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER

        for (col in colDefs) {
            val w = col.width
            canvas.drawRect(colX, tableTop, colX + w, tableTop + headerHeight, strokePaint)
            canvas.drawText(col.title, colX + w / 2f, tableTop + headerHeight / 2f + 6f, paint)
            colX += w
        }

        currentY += headerHeight

        // Dữ liệu dòng (Item rows)
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

        val items = invoice.items
        for (i in items.indices) {
            val item = items[i]
            // Dùng cùng phép đo với chiều cao trang để ghi chú dài không bị cắt ở đáy ảnh/PDF.
            val prodColWidth = prodColDef.width
            val wrappedLines = getWrappedLines(item.productName, prodColWidth - 20f, paint)
            val noteLines = getWrappedLines(item.note, QUOTATION_NOTE_WIDTH - 20f, paint)
            val rowHeight = getQuotationRowHeight(item, colDefs, paint)

            val rowTop = currentY
            val rowBottom = rowTop + rowHeight
            colX = marginLeft

            for (col in colDefs) {
                val w = col.width
                canvas.drawRect(colX, rowTop, colX + w, rowBottom, strokePaint)

                when (col.type) {
                    QuotationColType.STT -> {
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText("${item.stt}", colX + w / 2f, rowTop + rowHeight / 2f + 6f, paint)
                    }
                    QuotationColType.PRODUCT_NAME -> {
                        drawVerticallyCenteredWrappedText(
                            canvas = canvas,
                            lines = wrappedLines,
                            centerX = colX + w / 2f,
                            rowTop = rowTop,
                            rowHeight = rowHeight,
                            textSize = 17f,
                            paint = paint
                        )
                    }
                    QuotationColType.UNIT -> {
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText(item.unit, colX + w / 2f, rowTop + rowHeight / 2f + 6f, paint)
                    }
                    QuotationColType.QUANTITY -> {
                        paint.textAlign = Paint.Align.CENTER
                        canvas.drawText(FormatHelper.formatQuantity(item.quantity), colX + w / 2f, rowTop + rowHeight / 2f + 6f, paint)
                    }
                    QuotationColType.UNIT_PRICE -> {
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(FormatHelper.formatMoney(item.unitPrice), colX + w - 12f, rowTop + rowHeight / 2f + 6f, paint)
                    }
                    QuotationColType.NOTE -> {
                        drawVerticallyCenteredWrappedText(
                            canvas = canvas,
                            lines = noteLines,
                            centerX = colX + w / 2f,
                            rowTop = rowTop,
                            rowHeight = rowHeight,
                            textSize = 17f,
                            paint = paint
                        )
                    }
                    QuotationColType.LINE_TOTAL -> {
                        paint.textAlign = Paint.Align.RIGHT
                        canvas.drawText(FormatHelper.formatMoney(item.lineTotalM2), colX + w - 12f, rowTop + rowHeight / 2f + 6f, paint)
                    }
                }
                colX += w
            }

            currentY += rowHeight
        }

        // Row TỔNG CỘNG & VAT
        val subTotal = invoice.items.sumOf { it.lineTotalM2 }
        val vatAmount = invoice.vatAmount
        val finalTotal = invoice.totalAmount
        val mergedColsWidth = tableWidth - lineTotalColWidth

        if (invoice.vatRate > 0.0) {
            val subRowHeight = 44f

            // 1. Tiền hàng
            val subTop = currentY
            val subBottom = subTop + subRowHeight
            canvas.drawRect(marginLeft, subTop, marginLeft + mergedColsWidth, subBottom, strokePaint)
            canvas.drawRect(marginLeft + mergedColsWidth, subTop, marginRight, subBottom, strokePaint)
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText("Cộng tiền hàng: ", marginLeft + mergedColsWidth - 12f, subTop + subRowHeight / 2f + 6f, paint)
            canvas.drawText(FormatHelper.formatMoney(subTotal), marginRight - 12f, subTop + subRowHeight / 2f + 6f, paint)
            currentY += subRowHeight

            // 2. Thuế GTGT
            val vatTop = currentY
            val vatBottom = vatTop + subRowHeight
            canvas.drawRect(marginLeft, vatTop, marginLeft + mergedColsWidth, vatBottom, strokePaint)
            canvas.drawRect(marginLeft + mergedColsWidth, vatTop, marginRight, vatBottom, strokePaint)
            val vatLabel = if (invoice.vatRate == invoice.vatRate.toLong().toDouble()) {
                "Thuế GTGT (${invoice.vatRate.toInt()}%): "
            } else {
                "Thuế GTGT (${invoice.vatRate}%): "
            }
            canvas.drawText(vatLabel, marginLeft + mergedColsWidth - 12f, vatTop + subRowHeight / 2f + 6f, paint)
            canvas.drawText(FormatHelper.formatMoney(vatAmount), marginRight - 12f, vatTop + subRowHeight / 2f + 6f, paint)
            currentY += subRowHeight

            // 3. TỔNG CỘNG (ĐÃ BAO GỒM VAT)
            val totalRowHeight = 55f
            val totalTop = currentY
            val totalBottom = totalTop + totalRowHeight
            canvas.drawRect(marginLeft, totalTop, marginLeft + mergedColsWidth, totalBottom, strokePaint)
            canvas.drawRect(marginLeft + mergedColsWidth, totalTop, marginRight, totalBottom, strokePaint)
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("TỔNG CỘNG (ĐÃ BAO GỒM VAT)", marginLeft + mergedColsWidth / 2f, totalTop + totalRowHeight / 2f + 6f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(finalTotal), marginRight - 12f, totalTop + totalRowHeight / 2f + 6f, paint)
            currentY += totalRowHeight + 20f
        } else {
            val totalRowHeight = 65f
            val totalTop = currentY
            val totalBottom = totalTop + totalRowHeight

            canvas.drawRect(marginLeft, totalTop, marginLeft + mergedColsWidth, totalBottom, strokePaint)
            canvas.drawRect(marginLeft + mergedColsWidth, totalTop, marginRight, totalBottom, strokePaint)

            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val totalLabel = if (invoice.isVatIncluded) "TỔNG CỘNG (ĐÃ BAO GỒM VAT)" else "TỔNG CỘNG"
            canvas.drawText(totalLabel, marginLeft + mergedColsWidth / 2f, totalTop + totalRowHeight / 2f + 6f, paint)

            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(finalTotal), marginRight - 12f, totalTop + totalRowHeight / 2f + 6f, paint)

            currentY += totalRowHeight + 25f
        }

        // 4. Ghi chú
        if (invoice.showNotes && invoice.notes.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 16f
            currentY = drawWrappedText(canvas, invoice.notes, marginLeft, currentY, tableWidth, 16f, paint)
            currentY += 16f
        }

        // 4.1. Bảo hành (Ở dưới ghi chú)
        if (invoice.showWarranty && invoice.warranty.isNotBlank()) {
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            paint.textAlign = Paint.Align.LEFT
            paint.textSize = 16f
            val warrantyLine = if (invoice.warranty.startsWith("Bảo hành", ignoreCase = true)) invoice.warranty else "Bảo hành: ${invoice.warranty}"
            currentY = drawWrappedText(canvas, warrantyLine, marginLeft, currentY, tableWidth, 16f, paint)
            currentY += 16f
        }

        val dividerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3f
            color = Color.BLACK
        }

        // 5. Điều khoản thanh toán
        if (invoice.showPaymentTerms && invoice.paymentTerms.isNotBlank()) {
            currentY += 14f
            canvas.drawLine(marginLeft, currentY, marginRight, currentY, dividerPaint)
            currentY += 40f

            paint.textSize = 19f
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            val paymentLines = getWrappedLines(invoice.paymentTerms, tableWidth, paint)
            val paymentLineHeight = 19f * 1.5f
            for (i in paymentLines.indices) {
                canvas.drawText(paymentLines[i], canvasWidth / 2f, currentY + i * paymentLineHeight, paint)
            }
            currentY += (paymentLines.size - 1).coerceAtLeast(0) * paymentLineHeight + 45f
        }

        // 6. Thông tin Tài Khoản Ngân Hàng
        val hasBankDetails = invoice.bankAccountNumber.isNotBlank() || invoice.bankName.isNotBlank() || invoice.bankAccountHolder.isNotBlank()
        if (invoice.showBankInfo && hasBankDetails) {
            val bankLabelX = marginLeft + 30f
            val bankColonX = marginLeft + 230f
            val bankValueX = marginLeft + 245f
            val bankRowHeight = 35f

            paint.textSize = 18f
            paint.textAlign = Paint.Align.LEFT

            if (invoice.bankAccountNumber.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                canvas.drawText("Số Tài Khoản", bankLabelX, currentY, paint)
                canvas.drawText(":", bankColonX, currentY, paint)
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                canvas.drawText(invoice.bankAccountNumber, bankValueX, currentY, paint)
                currentY += bankRowHeight
            }

            if (invoice.bankName.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                canvas.drawText("Ngân hàng", bankLabelX, currentY, paint)
                canvas.drawText(":", bankColonX, currentY, paint)
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
                canvas.drawText(invoice.bankName, bankValueX, currentY, paint)
                currentY += bankRowHeight
            }

            if (invoice.bankAccountHolder.isNotBlank()) {
                paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
                canvas.drawText("Tên chủ tài khoản", bankLabelX, currentY, paint)
                canvas.drawText(":", bankColonX, currentY, paint)
                canvas.drawText(invoice.bankAccountHolder, bankValueX, currentY, paint)
                currentY += 35f
            }

            canvas.drawLine(marginLeft, currentY, marginRight, currentY, dividerPaint)
        }

        // Đóng dấu ĐÃ THANH TOÁN nếu báo giá đã được thanh toán
        if (invoice.isPaid) {
            val dateLabel = if (invoice.paidDate.isNotBlank()) "NGÀY: ${invoice.paidDate}" else "NGÀY: ${FormatHelper.formatDate(invoice.updatedAt)}"
            drawPaidStamp(canvas, marginRight - 160f, currentY + 45f, dateLabel, -10f)
        }
        // Bao gồm phần nhô xuống của con dấu đã xoay, trước lề cuối trang.
        return currentY + if (invoice.isPaid) 110f else 0f
    }

    private fun drawPaidStamp(
        canvas: Canvas,
        centerX: Float,
        centerY: Float,
        dateText: String,
        rotationDegrees: Float = -8f
    ) {
        canvas.save()
        canvas.translate(centerX, centerY)
        canvas.rotate(rotationDegrees)

        val stampWidth = 260f
        val stampHeight = 76f
        val rect = RectF(-stampWidth / 2f, -stampHeight / 2f, stampWidth / 2f, stampHeight / 2f)
        val innerRect = RectF(-stampWidth / 2f + 5f, -stampHeight / 2f + 5f, stampWidth / 2f - 5f, stampHeight / 2f - 5f)

        val stampColor = Color.parseColor("#D93025") // Đỏ con dấu đậm nổi bật

        // Outer border
        val outerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 3.5f
            color = stampColor
        }
        canvas.drawRoundRect(rect, 8f, 8f, outerPaint)

        // Inner border
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
            color = stampColor
        }
        canvas.drawRoundRect(innerRect, 6f, 6f, innerPaint)

        // Stamp Text
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = stampColor
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
            textSize = 21f
        }
        canvas.drawText("★ ĐÃ THANH TOÁN ★", 0f, if (dateText.isNotBlank()) -6f else 7f, textPaint)

        if (dateText.isNotBlank()) {
            val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = stampColor
                typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
                textSize = 12f
            }
            canvas.drawText(dateText, 0f, 18f, datePaint)
        }

        canvas.restore()
    }


    private fun truncateText(text: String, maxWidth: Float, paint: Paint): String {
        if (paint.measureText(text) <= maxWidth) return text
        var truncated = text
        while (truncated.isNotEmpty() && paint.measureText("$truncated...") > maxWidth) {
            truncated = truncated.dropLast(1)
        }
        return "$truncated..."
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        startY: Float,
        maxWidth: Float,
        textSize: Float,
        paint: Paint
    ): Float {
        val lines = getWrappedLines(text, maxWidth, paint)
        if (lines.isEmpty()) return startY
        var y = startY
        val lineHeight = textSize * 1.5f
        for (line in lines) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y - lineHeight + (lineHeight * 0.2f)
    }

    private fun getWrappedLines(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return emptyList()
        val lines = mutableListOf<String>()
        for (paragraph in text.replace("\r\n", "\n").replace('\r', '\n').split('\n')) {
            var remaining = paragraph.trim()
            if (remaining.isEmpty()) {
                lines.add("")
                continue
            }
            while (remaining.isNotEmpty()) {
                val count = paint.breakText(remaining, true, maxWidth.coerceAtLeast(1f), null)
                    .coerceAtLeast(1)
                if (count >= remaining.length) {
                    lines.add(remaining)
                    break
                }
                val wordBoundary = remaining.lastIndexOf(' ', count)
                val splitAt = if (wordBoundary > 0) wordBoundary else count
                lines.add(remaining.substring(0, splitAt).trimEnd())
                remaining = remaining.substring(splitAt).trimStart()
            }
        }
        return lines
    }

    private fun drawVerticallyCenteredWrappedText(
        canvas: Canvas,
        lines: List<String>,
        centerX: Float,
        rowTop: Float,
        rowHeight: Float,
        textSize: Float,
        paint: Paint
    ) {
        if (lines.isEmpty()) return

        val lineHeight = textSize * 1.4f
        val numLines = lines.size
        // Tính toán baseline của dòng đầu tiên sao cho tâm của khối text nằm đúng chính giữa dòng (rowTop + rowHeight / 2 + 6f)
        val startY = (rowTop + rowHeight / 2f + 6f) - ((numLines - 1) * lineHeight / 2f)

        paint.textAlign = Paint.Align.CENTER
        for (i in lines.indices) {
            val lineY = startY + (i * lineHeight)
            canvas.drawText(lines[i], centerX, lineY, paint)
        }
    }

    private fun drawInfoRow(
        canvas: Canvas,
        label: String,
        value: String,
        labelX: Float,
        colonX: Float,
        valueX: Float,
        startY: Float,
        maxWidth: Float,
        paint: Paint,
        isBoldValue: Boolean = false,
        lineHeight: Float = 28f
    ): Float {
        val labelPaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val valuePaint = Paint(paint).apply {
            typeface = Typeface.create(Typeface.SERIF, if (isBoldValue) Typeface.BOLD else Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }

        canvas.drawText(label, labelX, startY, labelPaint)
        canvas.drawText(":", colonX, startY, labelPaint)

        val lines = getWrappedLines(value, maxWidth, valuePaint)
        var y = startY
        for (i in lines.indices) {
            canvas.drawText(lines[i], valueX, startY + i * lineHeight, valuePaint)
        }
        y += (lines.size - 1).coerceAtLeast(0) * lineHeight
        return y + lineHeight + 6f
    }
}

