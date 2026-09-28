package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.DaySummary
import javax.inject.Inject

class CloseBusinessDayUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(businessDate: String? = null): DaySummary =
        if (businessDate != null) repository.closeBusinessDay(businessDate)
        else repository.closeBusinessDay()
}
