package com.vandatgsts.thuyetnguyen.data.model

import java.util.UUID

data class DebtPayment(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val amount: Double = 0.0,
    val date: String = ""
)
