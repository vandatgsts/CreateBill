package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class InvoiceDocument(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val type: InvoiceType = InvoiceType.QUOTATION_A4,
    val storeOrCompanyName: String = "",
    val companyAddress: String = "",
    val companyTaxCode: String = "",
    val companyPhone: String = "",
    val customer: CustomerInfo = CustomerInfo(),
    val items: List<InvoiceItem> = emptyList(),
    val oldDebt: Double = 0.0,
    val initialOldDebt: Double = 0.0,
    val debtPayments: List<DebtPayment> = emptyList(),
    val isPaid: Boolean = false,
    val paidDate: String = "",
    val notes: String = "",
    val warranty: String = "",
    val isVatIncluded: Boolean = true,
    val vatRate: Double = 0.0,
    val paymentTerms: String = "",
    val bankAccountNumber: String = "",
    val bankName: String = "",
    val bankAccountHolder: String = "",
    val showCompanyInfo: Boolean = true,
    val showCompanyAddress: Boolean = true,
    val showCompanyTaxCode: Boolean = true,
    val showCustomerInfo: Boolean = true,
    val showCustomerAddress: Boolean = true,
    val showUnitCol: Boolean = true,
    val showQuantityCol: Boolean = true,
    val showUnitPriceCol: Boolean = true,
    val showNotes: Boolean = true,
    val showWarranty: Boolean = true,
    val showPaymentTerms: Boolean = true,
    val showBankInfo: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDebtPayments: Double
        get() = debtPayments.sumOf { it.amount }

    val effectiveOldDebt: Double
        get() {
            val baseOldDebt = if (initialOldDebt != 0.0) initialOldDebt else oldDebt
            return baseOldDebt - totalDebtPayments
        }

    val vatAmount: Double
        get() = if (type == InvoiceType.QUOTATION_A4 && vatRate > 0.0) {
            items.sumOf { it.lineTotalM2 } * (vatRate / 100.0)
        } else 0.0

    // Tổng thành tiền hóa đơn nguyên vẹn (Không bị ép về 0 đ khi đã thanh toán)
    val totalAmount: Double
        get() = when (type) {
            InvoiceType.DELIVERY_DEBT -> items.sumOf { it.lineTotalM1 } + effectiveOldDebt
            InvoiceType.QUOTATION_A4 -> items.sumOf { it.lineTotalM2 } + vatAmount
        }

    val totalSubTotal: Double
        get() = items.sumOf { it.quantity * it.unitPrice }

    val totalPaid: Double
        get() = items.sumOf { it.paidAmount } + totalDebtPayments

    // Dư nợ thực tế cần thu / Số tiền trả thừa mang sang kỳ sau:
    // - Nếu totalAmount < 0: Khách trả thừa -> giữ nguyên số âm (dư có) để tự động mang sang kỳ tiếp theo khấu trừ
    // - Nếu totalAmount >= 0 và isPaid = true: Đã thu tiền xong -> Dư nợ = 0 đ
    // - Nếu totalAmount > 0 và isPaid = false: Dư nợ = totalAmount
    val remainingDebt: Double
        get() = if (totalAmount < 0.0) totalAmount else if (isPaid) 0.0 else totalAmount

    val isFullyPaid: Boolean
        get() = isPaid || totalAmount <= 0.0
}
