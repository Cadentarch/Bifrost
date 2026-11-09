package tk.zwander.common.util

import io.ktor.client.*
import io.ktor.client.engine.ProxyBuilder
import io.ktor.client.engine.cio.*
import io.ktor.client.engine.http
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.http.URLProtocol
import java.net.URI
import java.security.cert.X509Certificate
import java.util.Base64
import javax.net.ssl.X509TrustManager

actual fun createPlatformHttpClient(): HttpClient {
    val proxyUrl = System.getenv("HTTPS_PROXY")
        ?: System.getenv("https_proxy")
        ?: System.getenv("HTTP_PROXY")
        ?: System.getenv("http_proxy")
        ?: System.getenv("ALL_PROXY")
        ?: System.getenv("all_proxy")

    val normalizedProxy = proxyUrl?.takeIf { it.isNotBlank() }?.let {
        if (it.contains("://")) it else "http://$it"
    }

    val proxyUserInfo: String? = normalizedProxy?.let {
        try {
            URI(it).userInfo
        } catch (_: Exception) {
            null
        }
    }

    return HttpClient(CIO) {
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

            if (normalizedProxy != null) {
                proxy = ProxyBuilder.http(normalizedProxy)
            } else {
                val host = System.getProperty("https.proxyHost")
                    ?: System.getProperty("http.proxyHost")
                val port = (System.getProperty("https.proxyPort")
                    ?: System.getProperty("http.proxyPort"))
                    ?.toIntOrNull() ?: 8080
                if (!host.isNullOrBlank()) {
                    proxy = ProxyBuilder.http("http://$host:$port")
                }
            }
        }

        if (proxyUserInfo != null) {
            val encodedAuth = Base64.getEncoder().encodeToString(proxyUserInfo.toByteArray())
            install(createClientPlugin("ProxyAuth") {
                onRequest { request, _ ->
                    if (request.url.protocol == URLProtocol.HTTP) {
                        request.headers.append("Proxy-Authorization", "Basic $encodedAuth")
                    }
                }
            })
        }
    }
}
