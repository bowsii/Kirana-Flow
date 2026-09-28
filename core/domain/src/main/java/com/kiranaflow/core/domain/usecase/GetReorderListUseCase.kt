package com.kiranaflow.core.domain.usecase

import com.kiranaflow.core.data.KiranaRepository
import com.kiranaflow.core.model.CatalogItem
import javax.inject.Inject

class GetReorderListUseCase @Inject constructor(
    private val repository: KiranaRepository
) {
    suspend operator fun invoke(): List<CatalogItem> =
        repository.getStockReorderList()
}
