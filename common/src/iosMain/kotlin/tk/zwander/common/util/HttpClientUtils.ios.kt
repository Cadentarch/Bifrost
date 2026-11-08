package tk.zwander.common.util

import com.linroid.ketch.sqlite.DriverFactory
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import io.ktor.client.plugins.HttpTimeout
import platform.Foundation.*

actual fun createPlatformHttpClient(): HttpClient = HttpClient(Darwin) {
    followRedirects = true
    expectSuccess = false
    install(HttpTimeout)
    engine {
        handleChallenge { _, _, challenge, completionHandler ->
            if (challenge.protectionSpace.authenticationMethod == NSURLAuthenticationMethodServerTrust) {
                val serverTrust = challenge.protectionSpace.serverTrust
                if (serverTrust != null) {
                    completionHandler(
                        NSURLSessionAuthChallengeUseCredential,
                        NSURLCredential.credentialForTrust(serverTrust),
                    )
                } else {
                    completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
                }
            } else {
                completionHandler(NSURLSessionAuthChallengePerformDefaultHandling, null)
            }
        }
    }
}

actual val ketchDb: DriverFactory
    get() = DriverFactory()