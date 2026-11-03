package com.qflornia.autofigure.engine

import org.json.JSONObject

data class AiConfig(val baseUrl: String, val apiKey: String, val model: String, val provider: String = "custom")
data class FigureResult(val svg: String, val score: Double, val iterations: Int, val log: List<String>)

/** Native AutoFigure generation -> critic -> refinement workflow. */
class FigureEngine {
    private val router = ProviderRouter()

    suspend fun generate(
        config: AiConfig,
        instruction: String,
        topic: String,
        maxIterations: Int,
        threshold: Double,
        referenceImage: ByteArray? = null
    ): FigureResult {
        require(config.apiKey.isNotBlank()) { "请先填写 API Key" }
        require(config.model.isNotBlank()) { "请填写模型名称" }
        if (!config.provider.equals("gemini", true)) require(config.baseUrl.isNotBlank()) { "请填写 API Base URL" }
        val logs = mutableListOf<String>()
        val system = """You are AutoFigure, an expert scientific-figure designer. Create publication-quality, editable SVG diagrams.
Return ONLY a complete standalone SVG document. No markdown fences, explanations, raster images, or external resources.
Use a clear visual hierarchy, precise alignment, readable labels, consistent stroke widths, restrained scientific palette, and a viewBox. Use native SVG shapes, paths, text, markers and groups. Preserve scientific meaning; never invent mechanisms or data. Topic: $topic."""
        var svg = call(config, system, "Create a scientifically accurate diagram for this request:\n$instruction\n\nSVG only.", referenceImage)
        svg = extractSvg(svg)
        validateSvg(svg)
        logs += "初稿生成完成；SVG 结构检查通过（${config.provider}）"
        var score = 0.0
        var iteration = 0
        while (iteration < maxIterations) {
            iteration++
            val critique = call(config,
                "You are a strict scientific illustration reviewer. Evaluate scientific fidelity, completeness, layout, typography, contrast, and SVG validity. Return JSON only: {\"score\": number 0-10, \"issues\": [string], \"revision\": string}.",
                "Review this SVG against the original request. Score honestly. Request:\n$instruction\nSVG:\n$svg")
            val parsed = runCatching { JSONObject(extractJson(critique)) }.getOrNull()
            score = parsed?.optDouble("score", 0.0) ?: 0.0
            val issues = parsed?.optJSONArray("issues")?.let { arr ->
                (0 until arr.length()).joinToString { arr.optString(it) }
            } ?: critique.take(1200)
            logs += "第 $iteration 轮评价：${"%.1f".format(score)}/10；$issues"
            if (score >= threshold) break
            val revision = parsed?.optString("revision", "")?.takeIf { it.isNotBlank() } ?: issues
            val refined = call(config, system,
                "Improve the SVG using this reviewer feedback. Keep all scientifically correct content, fix the listed problems, and return the complete SVG only.\nFeedback: $revision\nOriginal request: $instruction\nCurrent SVG:\n$svg")
            val candidate = extractSvg(refined)
            validateSvg(candidate)
            svg = candidate
            logs += "第 $iteration 轮修改完成，SVG 结构检查通过"
        }
        return FigureResult(svg, score, iteration, logs)
    }

    suspend fun buildEnhancementPrompt(config: AiConfig, svg: String, artStyle: String, topic: String): String =
        call(config,
            "You are an expert scientific-figure art director. Analyze vector diagram source and write a precise image-generation prompt. Preserve all scientific content, labels, relationships and layout.",
            "Convert this $topic SVG into a detailed image-generation prompt in the style: $artStyle. Describe composition, exact text to preserve, color palette, typography, visual materials and connections. Do not invent scientific content.\nSVG:\n$svg")

    private fun call(c: AiConfig, system: String, prompt: String, imageBytes: ByteArray? = null): String =
        router.chat(ModelProfile(c.baseUrl, c.apiKey, c.model, c.provider), system, prompt, imageBytes)

    private fun extractSvg(raw: String): String {
        val cleaned = raw.trim().removePrefix("```svg").removePrefix("```xml").removePrefix("```").removeSuffix("```").trim()
        val start = cleaned.indexOf("<svg", ignoreCase = true)
        val end = cleaned.lastIndexOf("</svg>", ignoreCase = true)
        require(start >= 0 && end > start) { "模型没有返回完整 SVG。请更换支持代码输出的模型或调整提示词。" }
        return cleaned.substring(start, end + 6)
    }

    private fun validateSvg(svg: String) {
        require(svg.contains("<svg", true) && svg.contains("</svg>", true)) { "SVG 根元素不完整" }
        require(!svg.contains("<script", true)) { "出于安全考虑，拒绝包含脚本的 SVG" }
        require(!svg.contains("javascript:", true)) { "SVG 含有不安全链接" }
    }

    private fun extractJson(raw: String): String {
        val a = raw.indexOf('{'); val b = raw.lastIndexOf('}')
        require(a >= 0 && b > a) { "评价模型未返回 JSON" }
        return raw.substring(a, b + 1)
    }
}
