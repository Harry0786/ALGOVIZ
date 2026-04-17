package com.algoviz.plus.features.auth.data.mapper

import com.algoviz.plus.features.auth.domain.model.User
import io.github.jan.supabase.gotrue.user.UserInfo

object AuthMapper {
    fun mapSupabaseUserToDomain(supabaseUser: UserInfo): User {
        return User(
            id = supabaseUser.id,
            email = supabaseUser.email ?: "",
            isEmailVerified = supabaseUser.emailConfirmedAt != null || supabaseUser.confirmedAt != null
        )
    }
    
    fun mapSupabaseUserToDomainOrNull(supabaseUser: UserInfo?): User? {
        return supabaseUser?.let { mapSupabaseUserToDomain(it) }
    }
}
