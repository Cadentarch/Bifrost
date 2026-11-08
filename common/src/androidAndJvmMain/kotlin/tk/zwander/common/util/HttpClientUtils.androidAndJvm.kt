package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.HttpTimeout
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

actual fun createPlatformHttpClient(): HttpClient = HttpClient(CIO) {
    followRedirects = true
    expectSuccess = false
    install(HttpTimeout)
    engine {
        https {
            trustManager = object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            }
        }
    }
}
