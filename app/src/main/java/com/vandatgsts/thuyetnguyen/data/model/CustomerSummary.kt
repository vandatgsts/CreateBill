package com.vandatgsts.thuyetnguyen.data.model

data class CustomerSummary(
    val customer: CustomerProfile,
    val invoiceCount: Int = 0,
    val totalAmount: Double = 0.0,
    val totalPaid: Double = 0.0,
    val totalDebt: Double = 0.0,
    val invoices: List<InvoiceDocument> = emptyList()
)
