package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.FlowPurchasePlanItem
import javax.inject.Inject

class GetTomorrowFlowPlanUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(businessDate: String? = null): List<FlowPurchasePlanItem> =
        if (businessDate != null) repository.getFlowPurchasePlan(businessDate)
        else repository.getFlowPurchasePlan()
}
