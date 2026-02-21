package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.repository.AlgorithmRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllAlgorithmsUseCase @Inject constructor(
    private val repository: AlgorithmRepository
) {
    operator fun invoke(): Flow<List<Algorithm>> {
        return repository.getAllAlgorithms()
    }
}
