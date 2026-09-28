package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.BillingQueue
import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.CartLine
import com.kiranaflow.core.model.PaymentMode
import javax.inject.Inject

class CommitBillUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedPaise: Long = 0L
    ): BillingQueue.CommitResult {
        val result = repository.commitBill(cartLines, paymentMode, tenderedPaise)
        if (result is BillingQueue.CommitResult.Success) {
            repository.clearDraftCart()
        }
        return result
    }

    suspend operator fun invoke(
        cartLines: List<CartLine>,
        paymentMode: PaymentMode = PaymentMode.CASH,
        tenderedAmount: Double = 0.0
    ): BillingQueue.CommitResult {
        val tenderedPaise = (tenderedAmount * 100.0 + 0.5).toLong()
        return invoke(cartLines, paymentMode, tenderedPaise)
    }
}
