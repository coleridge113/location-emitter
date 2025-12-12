package com.luna.location_emitter.presentation.aws

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ResponseTypeValues
import net.openid.appauth.TokenRequest
import net.openid.appauth.TokenResponse
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class CognitoAuthHelper(
    private val context: Context,
    private val issuerUrl: String = "https://cognito-idp.ap-northeast-1.amazonaws.com/ap-northeast-1_qOJ3Um6p4",
    private val clientId: String = "5gqq1r5bhkfb6oj9r875bgvbkh",
    private val redirectUri: String = "com.metromart.locationemitter:/oauth2redirect" // must match Cognito app client
) {

    /**
     * Fetch the OIDC configuration from the issuer (suspendable).
     */
    suspend fun fetchServiceConfig(): AuthorizationServiceConfiguration =
        suspendCancellableCoroutine { cont ->
            AuthorizationServiceConfiguration.fetchFromIssuer(
                issuerUrl.toUri()
            ) { serviceConfig, ex ->
                if (ex != null) {
                    cont.resumeWithException(ex)
                    return@fetchFromIssuer
                }
                if (serviceConfig == null) {
                    cont.resumeWithException(IllegalStateException("Service configuration is null"))
                    return@fetchFromIssuer
                }
                cont.resume(serviceConfig)
            }
        }

    /**
     * Build an AuthorizationRequest from a fetched service configuration.
     * Uses ResponseTypeValues.CODE (PKCE).
     */
    fun buildAuthRequest(serviceConfig: AuthorizationServiceConfiguration): AuthorizationRequest {
        return AuthorizationRequest.Builder(
            serviceConfig,
            clientId,
            ResponseTypeValues.CODE,
            redirectUri.toUri()
        )
            .setScope("openid email phone")
            .build()
    }

    /**
     * Create the Intent to start the authorization flow. Launch this with ActivityResultLauncher.
     */
    fun getAuthIntent(authRequest: AuthorizationRequest): Intent {
        val authService = AuthorizationService(context)
        return authService.getAuthorizationRequestIntent(authRequest)
    }

    /**
     * Exchange a TokenRequest for tokens (suspendable).
     * Use the TokenRequest you obtain from the AuthorizationResponse in your Activity result handling.
     */
    suspend fun performTokenRequest(tokenRequest: TokenRequest): TokenResponse =
        suspendCancellableCoroutine { cont ->
            val authService = AuthorizationService(context)
            authService.performTokenRequest(tokenRequest) { response, ex ->
                if (ex != null) {
                    cont.resumeWithException(ex)
                    return@performTokenRequest
                }
                if (response == null) {
                    cont.resumeWithException(IllegalStateException("Token response is null"))
                    return@performTokenRequest
                }
                cont.resume(response)
            }
            cont.invokeOnCancellation {
                // best-effort cleanup
                authService.dispose()
            }
        }
}
