package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class StorePartner(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val contactPerson: String = "",
    val defaultType: InvoiceType = InvoiceType.DELIVERY_DEBT,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
