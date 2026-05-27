package com.amindev.muziktube

import okhttp3.OkHttpClient
import okhttp3.Request as OkRequest
import okhttp3.RequestBody
import org.schabi.newpipe.extractor.downloader.Downloader
import org.schabi.newpipe.extractor.downloader.Request
import org.schabi.newpipe.extractor.downloader.Response
import org.schabi.newpipe.extractor.exceptions.ReCaptchaException
import java.util.concurrent.TimeUnit

class YtDownloader private constructor() : Downloader() {

    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    companion object {
        @Volatile private var instance: YtDownloader? = null
        fun getInstance() = instance ?: synchronized(this) {
            instance ?: YtDownloader().also { instance = it }
        }
    }

    override fun execute(request: Request): Response {
        val requestBuilder = OkRequest.Builder().url(request.url())

        request.headers().forEach { (key, values) ->
            values.forEach { requestBuilder.addHeader(key, it) }
        }
        requestBuilder.addHeader(
            "User-Agent",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36"
        )

        val body = request.dataToSend()?.let { RequestBody.create(null, it) }
        val okRequest = when (request.httpMethod()) {
            "POST" -> requestBuilder.post(body!!).build()
            else   -> requestBuilder.get().build()
        }

        val response = client.newCall(okRequest).execute()
        if (response.code == 429) throw ReCaptchaException("Rate limited", request.url())

        return Response(
            response.code,
            response.message,
            response.headers.toMultimap(),
            response.body?.string() ?: "",
            request.url()
        )
    }
}
