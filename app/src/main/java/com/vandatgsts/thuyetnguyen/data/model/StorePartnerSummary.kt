package com.vandatgsts.thuyetnguyen.data.model

data class StorePartnerSummary(
    val store: StorePartner,
    val invoiceCount: Int = 0,
    val totalAmount: Double = 0.0,
    val totalPaid: Double = 0.0,
    val currentDebt: Double = 0.0,
    val latestInvoice: InvoiceDocument? = null,
    val invoices: List<InvoiceDocument> = emptyList()
)
