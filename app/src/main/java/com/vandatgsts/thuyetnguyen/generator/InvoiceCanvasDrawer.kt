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
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import kotlin.math.max

object InvoiceCanvasDrawer {

    fun getCanvasWidth(type: InvoiceType): Int = when (type) {
        InvoiceType.DELIVERY_DEBT -> 1200
        InvoiceType.QUOTATION_A4 -> 1000
    }

    fun getCanvasHeight(type: InvoiceType, invoice: InvoiceDocument): Int = when (type) {
        InvoiceType.DELIVERY_DEBT -> {
            val baseHeader = 100
            val rowHeight = 44
            val rows = max(invoice.items.size, 3)
            val footers = 100
            max(700, baseHeader + (rows * rowHeight) + footers + 100)
        }
        InvoiceType.QUOTATION_A4 -> {
            var estimatedHeight = 150 // base title + top/bottom padding
            if (invoice.showCompanyInfo) {
                estimatedHeight += 35
                if (invoice.showCompanyAddress && invoice.companyAddress.isNotBlank()) estimatedHeight += 40
                if (invoice.showCompanyTaxCode && invoice.companyTaxCode.isNotBlank()) estimatedHeight += 35
            }
            if (invoice.showCustomerInfo) {
                if (invoice.customer.name.isNotBlank()) estimatedHeight += 35
                if (invoice.showCustomerAddress && invoice.customer.address.isNotBlank()) estimatedHeight += 40
            }
            val rows = max(invoice.items.size, 1)
            estimatedHeight += 55 + (rows * 120) + 160 // Table header + rows + totals
            if (invoice.showNotes && invoice.notes.isNotBlank()) estimatedHeight += 60
            if (invoice.showWarranty && invoice.warranty.isNotBlank()) estimatedHeight += 40
            if (invoice.showPaymentTerms && invoice.paymentTerms.isNotBlank()) estimatedHeight += 60
            if (invoice.showBankInfo && (invoice.bankAccountNumber.isNotBlank() || invoice.bankName.isNotBlank() || invoice.bankAccountHolder.isNotBlank())) estimatedHeight += 130
            if (invoice.isPaid) estimatedHeight += 60
            max(1414, estimatedHeight + 80)
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
        STT, PRODUCT_NAME, UNIT, QUANTITY, UNIT_PRICE, LINE_TOTAL
    }

    private data class QuotationColDef(
        val type: QuotationColType,
        val title: String,
        var width: Float
    )

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
        // Cột: STT(45), Ngày(90), Sản phẩm(200), SL(60), Đơn giá(100), Địa chỉ(165), Người nhận(100), SĐT(110), Đã Thu(110), Thành Tiền(140)
        val colWidths = floatArrayOf(45f, 90f, 210f, 65f, 105f, 165f, 105f, 115f, 105f, 115f)
        val colHeaders = arrayOf(
            "STT", "Ngày", "Sản phẩm", "Số lượng", "Đơn giá",
            "Địa chỉ", "Người nhận", "Số điện thoại", "Đã Thu", "Thành Tiền"
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
        val rowHeight = 48f
        paint.textSize = 14f
        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)

        val items = invoice.items
        for (i in items.indices) {
            val item = items[i]
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

            // Cột 8: Đã Thu (Right)
            canvas.drawRect(colX, rowTop, colX + colWidths[8], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(item.paidAmount), colX + colWidths[8] - 8f, rowTop + rowHeight / 2f + 5f, paint)
            colX += colWidths[8]

            // Cột 9: Thành Tiền (Right)
            canvas.drawRect(colX, rowTop, colX + colWidths[9], rowBottom, strokePaint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(item.lineTotalM1), colX + colWidths[9] - 8f, rowTop + rowHeight / 2f + 5f, paint)

            currentY += rowHeight
        }

        // Footer 1: Nợ cũ
        val footer1Top = currentY
        val footer1Bottom = footer1Top + 38f
        val mergedColsWidth = tableWidth - colWidths[9]

        canvas.drawRect(marginLeft, footer1Top, marginLeft + mergedColsWidth, footer1Bottom, strokePaint)
        canvas.drawRect(marginLeft + mergedColsWidth, footer1Top, marginRight, footer1Bottom, strokePaint)

        paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText("Nợ cũ", marginLeft + mergedColsWidth / 2f, footer1Top + 24f, paint)

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
        canvas.drawText("Thành tiền", marginLeft + mergedColsWidth / 2f, footer2Top + 24f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(FormatHelper.formatMoney(invoice.totalAmount), marginRight - 8f, footer2Top + 24f, paint)

        currentY += 60f

        // Bảng phụ chi tiết các đợt Chuyển khoản / Trừ nợ cũ (Khớp mẫu img_4.png)
        if (invoice.debtPayments.isNotEmpty() || (invoice.initialOldDebt > 0 && invoice.initialOldDebt != invoice.effectiveOldDebt)) {
            val subTableLeft = marginLeft
            val subCol1Width = 190f
            val subCol2Width = 160f
            val subRowHeight = 34f
            var subY = currentY

            val baseDebt = if (invoice.initialOldDebt > 0) invoice.initialOldDebt else invoice.oldDebt

            // Dòng 1: Nợ cũ
            canvas.drawRect(subTableLeft, subY, subTableLeft + subCol1Width, subY + subRowHeight, strokePaint)
            canvas.drawRect(subTableLeft + subCol1Width, subY, subTableLeft + subCol1Width + subCol2Width, subY + subRowHeight, strokePaint)
            paint.color = Color.BLACK
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Nợ cũ", subTableLeft + subCol1Width / 2f, subY + 22f, paint)
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

            // Dòng cuối: Nợ cũ còn lại
            canvas.drawRect(subTableLeft, subY, subTableLeft + subCol1Width, subY + subRowHeight, strokePaint)
            canvas.drawRect(subTableLeft + subCol1Width, subY, subTableLeft + subCol1Width + subCol2Width, subY + subRowHeight, strokePaint)
            paint.color = Color.parseColor("#CC0000") // Màu đỏ như trong ảnh mẫu img_4
            paint.typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("Nợ cũ còn lại", subTableLeft + subCol1Width / 2f, subY + 22f, paint)
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(FormatHelper.formatMoney(invoice.effectiveOldDebt), subTableLeft + subCol1Width + subCol2Width - 10f, subY + 22f, paint)
        }

        // Đóng dấu ĐÃ THANH TOÁN nổi bật nếu hóa đơn đã được thu tiền
        if (invoice.isPaid) {
            val dateLabel = if (invoice.paidDate.isNotBlank()) "NGÀY: ${invoice.paidDate}" else "NGÀY: ${FormatHelper.formatDate(invoice.updatedAt)}"
            drawPaidStamp(canvas, marginRight - 160f, footer2Top + 19f, dateLabel, -7f)
        }
    }

    private fun drawQuotationA4Template(canvas: Canvas, invoice: InvoiceDocument) {
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
        val colDefs = mutableListOf<QuotationColDef>()
        colDefs.add(QuotationColDef(QuotationColType.STT, "STT", 55f))
        val prodColDef = QuotationColDef(QuotationColType.PRODUCT_NAME, "NỘI DUNG DỊCH VỤ", 0f)
        colDefs.add(prodColDef)

        if (invoice.showUnitCol) {
            colDefs.add(QuotationColDef(QuotationColType.UNIT, "ĐVT", 95f))
        }
        if (invoice.showQuantityCol) {
            colDefs.add(QuotationColDef(QuotationColType.QUANTITY, "SỐ LƯỢNG", 115f))
        }
        if (invoice.showUnitPriceCol) {
            colDefs.add(QuotationColDef(QuotationColType.UNIT_PRICE, "ĐƠN GIÁ", 140f))
        }
        val lineTotalColWidth = 140f
        colDefs.add(QuotationColDef(QuotationColType.LINE_TOTAL, "THÀNH TIỀN", lineTotalColWidth))

        // Dãn rộng cột Nội dung dịch vụ để vừa khít tổng chiều rộng bảng 900f
        val otherColsTotalWidth = colDefs.filter { it.type != QuotationColType.PRODUCT_NAME }.sumOf { it.width.toDouble() }.toFloat()
        prodColDef.width = tableWidth - otherColsTotalWidth

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
            // Tính toán chiều cao dòng linh hoạt nếu nội dung dịch vụ nhiều dòng
            val prodColWidth = prodColDef.width
            val wrappedLines = getWrappedLines(item.productName, prodColWidth - 20f, paint)
            val neededHeight = (maxOf(1, wrappedLines.size) * (17f * 1.4f)) + 50f
            val rowHeight = max(120f, neededHeight)

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
            canvas.drawText(invoice.paymentTerms, canvasWidth / 2f, currentY, paint)
            currentY += 45f
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
        val words = text.split(" ")
        var line = ""
        var y = startY
        val lineHeight = textSize * 1.5f

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (paint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, x, y, paint)
                line = word
                y += lineHeight
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, x, y, paint)
            y += lineHeight
        }
        return y - lineHeight + (lineHeight * 0.2f)
    }

    private fun getWrappedLines(text: String, maxWidth: Float, paint: Paint): List<String> {
        if (text.isBlank()) return emptyList()
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = ""
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > maxWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = word
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
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

        val words = value.split(" ")
        var line = ""
        var y = startY

        for (word in words) {
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (valuePaint.measureText(testLine) > maxWidth) {
                canvas.drawText(line, valueX, y, valuePaint)
                line = word
                y += lineHeight
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) {
            canvas.drawText(line, valueX, y, valuePaint)
        }
        return y + lineHeight + 6f
    }
}

