package com.alfredang.sgtutors

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class ApiException(message: String) : Exception(message)

/**
 * Thin client for the SG Tutors public API. Read-only endpoints plus
 * the enquiry POST — no auth, no admin surface.
 */
object ApiClient {
    private const val BASE = "https://sgtutors.tertiaryinfotech.com/api"
    private val client = OkHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val jsonMedia = "application/json".toMediaType()

    private suspend fun fetch(request: Request): String = withContext(Dispatchers.IO) {
        client.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw ApiException(serverMessage(body, response.code))
            }
            body
        }
    }

    private fun serverMessage(body: String, code: Int): String = runCatching {
        json.parseToJsonElement(body).jsonObject["error"]?.jsonPrimitive?.content
    }.getOrNull() ?: "Request failed (HTTP $code)"

    private fun url(path: String, query: Map<String, String?> = emptyMap()): HttpUrl {
        val builder = "$BASE/$path".toHttpUrl().newBuilder()
        query.forEach { (name, value) ->
            if (!value.isNullOrEmpty()) builder.addQueryParameter(name, value)
        }
        return builder.build()
    }

    suspend fun searchTutors(
        q: String?, subject: String?, level: String?,
        region: String?, gender: String?, page: Int,
    ): Paged<PublicTutor> {
        val body = fetch(
            Request.Builder().url(
                url(
                    "tutors",
                    mapOf(
                        "page" to page.toString(), "q" to q, "subject" to subject,
                        "level" to level, "region" to region, "gender" to gender,
                    ),
                )
            ).build()
        )
        return json.decodeFromString(body)
    }

    suspend fun featuredTutors(): List<PublicTutor> =
        json.decodeFromString(fetch(Request.Builder().url(url("tutors/featured")).build()))

    suspend fun tutor(id: String): PublicTutor =
        json.decodeFromString(fetch(Request.Builder().url(url("tutors/$id")).build()))

    suspend fun reviews(tutorId: String, page: Int = 1): Paged<Review> =
        json.decodeFromString(
            fetch(
                Request.Builder()
                    .url(url("tutors/$tutorId/reviews", mapOf("page" to page.toString())))
                    .build()
            )
        )

    suspend fun subjects(): List<Subject> =
        json.decodeFromString(fetch(Request.Builder().url(url("subjects")).build()))

    suspend fun levels(): List<Level> =
        json.decodeFromString(fetch(Request.Builder().url(url("levels")).build()))

    suspend fun sendEnquiry(tutorId: String, input: EnquiryInput) {
        fetch(
            Request.Builder()
                .url(url("tutors/$tutorId/enquiries"))
                .post(json.encodeToString(EnquiryInput.serializer(), input).toRequestBody(jsonMedia))
                .build()
        )
    }
}
