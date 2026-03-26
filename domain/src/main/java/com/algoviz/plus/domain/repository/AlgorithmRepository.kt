package com.algoviz.plus.domain.repository

import com.algoviz.plus.domain.model.Algorithm
import com.algoviz.plus.domain.model.AlgorithmCategory
import com.algoviz.plus.domain.model.AlgorithmStep
import kotlinx.coroutines.flow.Flow

interface AlgorithmRepository {
    fun getAllAlgorithms(): Flow<List<Algorithm>>
    fun getAlgorithmsByCategory(category: AlgorithmCategory): Flow<List<Algorithm>>
    fun getAlgorithmById(id: String): Algorithm?
    suspend fun generateSteps(
        algorithmId: String,
        initialArray: List<Int>,
        extraInput: Map<String, String> = emptyMap()
    ): List<AlgorithmStep>
}
