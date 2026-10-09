package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class InvoiceItem(
    val id: String = UUID.randomUUID().toString(),
    val stt: Int = 1,
    val date: String = "",
    val productName: String = "",
    val unit: String = "",
    val quantity: Double = 1.0,
    val unitPrice: Double = 0.0,
    val address: String = "",
    val receiver: String = "",
    val phone: String = "",
    val paidAmount: Double = 0.0,
    val note: String = ""
) {
    /**
     * Thành tiền cho Mẫu 1 (Giao hàng & Công nợ): (Số lượng * Đơn giá) - Đã thu
     */
    val lineTotalM1: Double
        get() = (quantity * unitPrice) - paidAmount

    /**
     * Thành tiền cho Mẫu 2 (Báo giá A4): Số lượng * Đơn giá
     */
    val lineTotalM2: Double
        get() = quantity * unitPrice
}
