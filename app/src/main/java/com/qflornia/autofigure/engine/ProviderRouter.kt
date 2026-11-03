package com.qflornia.autofigure.engine

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Provider-aware REST adapter. Gemini uses its native GenerateContent schema;
 * OpenAI, DeepSeek, OpenRouter and custom endpoints use Chat Completions.
 */
data class ModelProfile(
    val baseUrl: String,
    val apiKey: String,
    val model: String,
    val provider: String = "custom"
)

class ProviderRouter {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(240, TimeUnit.SECONDS)
        .callTimeout(300, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun chat(profile: ModelProfile, system: String, user: String, image: ByteArray? = null): String {
        require(profile.apiKey.isNotBlank()) { "请填写 API Key" }
        require(profile.model.isNotBlank()) { "请填写模型名称" }
        return if (profile.provider.equals("gemini", true)) {
            gemini(profile, system, user, image)
        } else {
            openAiCompatible(profile, system, user, image)
        }
    }

    private fun openAiCompatible(p: ModelProfile, system: String, user: String, image: ByteArray?): String {
        require(p.baseUrl.isNotBlank()) { "请填写 API Base URL" }
        val root = p.baseUrl.trim().trimEnd('/')
        val endpoint = if (root.endsWith("/chat/completions")) root else "$root/chat/completions"
        val content: Any = if (image == null) user else JSONArray()
            .put(JSONObject().put("type", "text").put("text", user))
            .put(JSONObject().put("type", "image_url").put("image_url", JSONObject().put(
                "url", "data:image/jpeg;base64," + Base64.encodeToString(image, Base64.NO_WRAP)
            )))
        val body = JSONObject()
            .put("model", p.model)
            .put("temperature", 0.25)
            .put("messages", JSONArray()
                .put(JSONObject().put("role", "system").put("content", system))
                .put(JSONObject().put("role", "user").put("content", content)))
        val request = Request.Builder().url(endpoint)
            .header("Authorization", "Bearer ${p.apiKey}")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonType)).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("${p.provider} API HTTP ${response.code}: ${raw.take(1200)}")
            val message = JSONObject(raw).getJSONArray("choices").getJSONObject(0).getJSONObject("message")
            val result = message.optString("content")
            require(result.isNotBlank()) { "模型返回内容为空" }
            return result
        }
    }

    private fun gemini(p: ModelProfile, system: String, user: String, image: ByteArray?): String {
        val root = p.baseUrl.trim().trimEnd('/').ifBlank { "https://generativelanguage.googleapis.com/v1beta" }
        val endpoint = "$root/models/${p.model}:generateContent"
        val parts = JSONArray().put(JSONObject().put("text", user))
        if (image != null) {
            parts.put(JSONObject().put("inline_data", JSONObject()
                .put("mime_type", "image/jpeg")
                .put("data", Base64.encodeToString(image, Base64.NO_WRAP))))
        }
        val body = JSONObject()
            .put("system_instruction", JSONObject().put("parts", JSONArray().put(JSONObject().put("text", system))))
            .put("contents", JSONArray().put(JSONObject().put("role", "user").put("parts", parts)))
            .put("generationConfig", JSONObject().put("maxOutputTokens", 8192))
        val request = Request.Builder().url(endpoint)
            .header("x-goog-api-key", p.apiKey)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonType)).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Gemini API HTTP ${response.code}: ${raw.take(1200)}")
            val candidates = JSONObject(raw).optJSONArray("candidates") ?: error("Gemini 未返回 candidates：${raw.take(600)}")
            val candidate = candidates.optJSONObject(0) ?: error("Gemini 没有可用候选回答")
            val content = candidate.optJSONObject("content") ?: error("Gemini 返回内容为空")
            val responseParts = content.optJSONArray("parts") ?: error("Gemini 返回内容为空")
            val result = buildString {
                for (i in 0 until responseParts.length()) {
                    append(responseParts.optJSONObject(i)?.optString("text").orEmpty())
                }
            }.trim()
            require(result.isNotBlank()) { "Gemini 返回内容为空（可能触发安全过滤或模型未输出文本）" }
            return result
        }
    }
}
