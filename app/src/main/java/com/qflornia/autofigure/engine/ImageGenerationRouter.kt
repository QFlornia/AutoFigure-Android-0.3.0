package com.qflornia.autofigure.engine

import android.util.Base64
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

/** Raster image generation adapter. Supports Gemini native image output and
 * OpenAI-compatible /images/generations endpoints (including compatible gateways).
 */
class ImageGenerationRouter {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(240, TimeUnit.SECONDS)
        .callTimeout(300, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    fun generate(profile: ModelProfile, prompt: String, aspectRatio: String = "16:9"): ByteArray {
        require(profile.apiKey.isNotBlank()) { "请先填写 API Key" }
        require(profile.model.isNotBlank()) { "请填写图像生成模型名称" }
        require(prompt.isNotBlank()) { "请填写图像生成描述" }
        return if (profile.provider.equals("gemini", true)) {
            generateGemini(profile, prompt, aspectRatio)
        } else {
            generateOpenAiCompatible(profile, prompt)
        }
    }

    private fun generateGemini(p: ModelProfile, prompt: String, aspectRatio: String): ByteArray {
        val root = p.baseUrl.trim().trimEnd('/').ifBlank { "https://generativelanguage.googleapis.com/v1beta" }
        val endpoint = "$root/models/${p.model}:generateContent"
        val body = JSONObject()
            .put("contents", org.json.JSONArray().put(JSONObject().put("role", "user")
                .put("parts", org.json.JSONArray().put(JSONObject().put("text", prompt)))))
            .put("generationConfig", JSONObject()
                .put("responseModalities", org.json.JSONArray().put("IMAGE").put("TEXT"))
                .put("imageConfig", JSONObject().put("aspectRatio", aspectRatio)))
        val request = Request.Builder().url(endpoint)
            .header("x-goog-api-key", p.apiKey)
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonType)).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("Gemini 图片生成 HTTP ${response.code}: ${raw.take(1200)}")
            val candidates = JSONObject(raw).optJSONArray("candidates") ?: error("Gemini 未返回候选结果")
            val parts = candidates.optJSONObject(0)?.optJSONObject("content")?.optJSONArray("parts")
                ?: error("Gemini 未返回图片内容")
            for (i in 0 until parts.length()) {
                val inline = parts.optJSONObject(i)?.optJSONObject("inlineData")
                    ?: parts.optJSONObject(i)?.optJSONObject("inline_data")
                val encoded = inline?.optString("data").orEmpty()
                if (encoded.isNotBlank()) return Base64.decode(encoded, Base64.DEFAULT)
            }
            error("模型没有返回图片。请确认模型支持图像生成，并检查 API 权限。")
        }
    }

    private fun generateOpenAiCompatible(p: ModelProfile, prompt: String): ByteArray {
        require(p.baseUrl.isNotBlank()) { "请填写 API Base URL" }
        val root = p.baseUrl.trim().trimEnd('/')
        val endpoint = if (root.endsWith("/images/generations")) root else "$root/images/generations"
        val body = JSONObject().put("model", p.model).put("prompt", prompt)
            .put("size", "1536x1024").put("n", 1).put("response_format", "b64_json")
        val request = Request.Builder().url(endpoint)
            .header("Authorization", "Bearer ${p.apiKey}")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody(jsonType)).build()
        client.newCall(request).execute().use { response ->
            val raw = response.body?.string().orEmpty()
            if (!response.isSuccessful) error("${p.provider} 图片生成 HTTP ${response.code}: ${raw.take(1200)}")
            val data = JSONObject(raw).optJSONArray("data")?.optJSONObject(0)
                ?: error("图片 API 未返回 data")
            val b64 = data.optString("b64_json")
            if (b64.isNotBlank()) return Base64.decode(b64, Base64.DEFAULT)
            error("图片 API 未返回 b64_json。当前适配器要求兼容 OpenAI Images API 的 base64 响应。")
        }
    }
}
