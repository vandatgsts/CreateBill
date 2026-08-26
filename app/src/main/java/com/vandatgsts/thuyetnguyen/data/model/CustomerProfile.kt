package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class CustomerProfile(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val taxCode: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
