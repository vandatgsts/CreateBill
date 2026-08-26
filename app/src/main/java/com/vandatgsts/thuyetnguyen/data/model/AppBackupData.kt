package com.vandatgsts.thuyetnguyen.data.model

data class AppBackupData(
    val backupVersion: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val exportedDate: String = "",
    val companyProfile: CompanyProfile = CompanyProfile(),
    val invoices: List<InvoiceDocument> = emptyList(),
    val products: List<ProductTemplate> = emptyList(),
    val storePartners: List<StorePartner> = emptyList(),
    val customers: List<CustomerProfile> = emptyList(),
    val fileHash: String = ""
)

enum class ImportMode {
    MERGE,       // Gộp dữ liệu (giữ dữ liệu hiện có và thêm mới)
    REPLACE_ALL  // Ghi đè toàn bộ (thay thế sạch dữ liệu máy)
}
