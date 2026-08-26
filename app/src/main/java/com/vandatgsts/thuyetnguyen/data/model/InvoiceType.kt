package com.vandatgsts.thuyetnguyen.data.model

enum class InvoiceType(val displayName: String, val description: String) {
    DELIVERY_DEBT(
        displayName = "Mẫu 1: Giao hàng & Công nợ (Khổ ngang)",
        description = "Bảng theo dõi giao nhận hàng, quản lý đã thu, nợ cũ và thành tiền (dạng bảng ngang)."
    ),
    QUOTATION_A4(
        displayName = "Mẫu 2: Bảng Báo Giá Chuẩn A4 (Khổ dọc)",
        description = "Bảng báo giá dịch vụ / sản phẩm chuẩn A4 dọc, có thông tin công ty, MST và thông tin thanh toán."
    )
}
