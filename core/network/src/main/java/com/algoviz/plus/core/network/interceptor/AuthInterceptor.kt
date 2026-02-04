package com.algoviz.plus.core.network.interceptor

import com.algoviz.plus.core.common.constants.ApiConstants
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class AuthInterceptor @Inject constructor() : Interceptor {
    
    @Volatile
    private var token: String? = null
    
    fun setToken(newToken: String?) {
        token = newToken
    }
    
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        
        val requestBuilder = originalRequest.newBuilder()
            .header(ApiConstants.HEADER_CONTENT_TYPE, ApiConstants.CONTENT_TYPE_JSON)
            .header(ApiConstants.HEADER_ACCEPT, ApiConstants.CONTENT_TYPE_JSON)
        
        token?.let {
            requestBuilder.header(ApiConstants.HEADER_AUTHORIZATION, "Bearer $it")
        }
        
        return chain.proceed(requestBuilder.build())
    }
}
