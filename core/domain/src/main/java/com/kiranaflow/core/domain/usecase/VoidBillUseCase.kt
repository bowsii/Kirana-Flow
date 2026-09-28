package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import javax.inject.Inject

class VoidBillUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(billId: String, reason: String = "Customer Void"): Boolean =
        repository.voidBill(billId, reason)
}
