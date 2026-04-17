package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.User
import io.github.jan.supabase.gotrue.user.UserInfo

object AuthMapper {
    fun mapFirebaseUserToDomain(firebaseUser: UserInfo): User {
        return User(
            id = firebaseUser.id,
            email = firebaseUser.email ?: "",
            isEmailVerified = firebaseUser.emailConfirmedAt != null || firebaseUser.confirmedAt != null
        )
    }
    
    fun mapFirebaseUserToDomainOrNull(firebaseUser: UserInfo?): User? {
        return firebaseUser?.let { mapFirebaseUserToDomain(it) }
    }
}
