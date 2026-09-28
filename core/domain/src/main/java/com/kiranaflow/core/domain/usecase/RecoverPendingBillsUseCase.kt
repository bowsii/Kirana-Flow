package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import javax.inject.Inject

class RecoverPendingBillsUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke() {
        repository.replayPendingBills()
    }
}
