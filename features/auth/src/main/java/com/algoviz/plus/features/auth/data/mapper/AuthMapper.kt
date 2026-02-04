package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.User
import com.google.firebase.auth.FirebaseUser

object AuthMapper {
    fun mapFirebaseUserToDomain(firebaseUser: FirebaseUser): User {
        return User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            isEmailVerified = firebaseUser.isEmailVerified
        )
    }
    
    fun mapFirebaseUserToDomainOrNull(firebaseUser: FirebaseUser?): User? {
        return firebaseUser?.let { mapFirebaseUserToDomain(it) }
    }
}
