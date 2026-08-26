package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class ProductTemplate(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val unit: String = "",
    val defaultPrice: Double = 0.0,
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
