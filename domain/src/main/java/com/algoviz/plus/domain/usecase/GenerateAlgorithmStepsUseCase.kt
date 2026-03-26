package com.algoviz.plus.domain.usecase

import com.algoviz.plus.domain.model.AlgorithmStep
import com.algoviz.plus.domain.repository.AlgorithmRepository
import javax.inject.Inject

class GenerateAlgorithmStepsUseCase @Inject constructor(
    private val repository: AlgorithmRepository
) {
    suspend operator fun invoke(
        algorithmId: String,
        initialArray: List<Int>,
        extraInput: Map<String, String> = emptyMap()
    ): List<AlgorithmStep> {
        return repository.generateSteps(algorithmId, initialArray, extraInput)
    }
}
