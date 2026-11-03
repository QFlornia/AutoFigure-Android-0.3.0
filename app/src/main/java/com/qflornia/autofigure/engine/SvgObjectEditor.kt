package com.qflornia.autofigure.engine

data class SvgObjectInfo(val index: Int, val tag: String, val id: String, val fill: String, val preview: String)

/** Lightweight source-preserving editor for common SVG drawable elements.
 * It intentionally edits one element at a time and leaves unrelated SVG markup untouched.
 */
object SvgObjectEditor {
    private val element = Regex("<(rect|circle|ellipse|path|line|polyline|polygon|text)\\b([^>]*)>([\\s\\S]*?)</\\1\\s*>|<(rect|circle|ellipse|path|line|polyline|polygon)\\b([^>]*)/?>", RegexOption.IGNORE_CASE)
    private val attr = Regex("""([A-Za-z_:][A-Za-z0-9_.:-]*)\s*=\s*["']([^"']*)["']""")

    fun list(svg: String): List<SvgObjectInfo> = element.findAll(svg).mapIndexed { i, m ->
        val tag = m.groupValues[1].ifBlank { m.groupValues[4] }
        val attributes = m.groupValues[2].ifBlank { m.groupValues[5] }
        val values = attr.findAll(attributes).associate { it.groupValues[1] to it.groupValues[2] }
        SvgObjectInfo(i, tag, values["id"].orEmpty(), values["fill"] ?: "未设置", m.value.take(100))
    }.toList()

    fun setFill(svg: String, index: Int, color: String): String {
        require(Regex("#[0-9a-fA-F]{6}").matches(color) || color.matches(Regex("[a-zA-Z]{3,20}")) || color == "none") {
            "颜色请使用 #RRGGBB、颜色名称或 none"
        }
        var current = -1
        var changed = false
        val result = element.replace(svg) { m ->
            current++
            if (current != index) m.value else {
                changed = true
                val open = m.value.indexOf('>')
                if (open < 0) m.value else {
                    val start = m.value.substring(0, open)
                    val updated = if (Regex("""\bfill\s*=""").containsMatchIn(start)) {
                        Regex("""\bfill\s*=\s*["'][^"']*["']""").replaceFirst(start, "fill=\"$color\"")
                    } else start + " fill=\"$color\""
                    updated + m.value.substring(open)
                }
            }
        }
        require(changed) { "没有找到第 ${index + 1} 个可编辑 SVG 元素" }
        return result
    }

    fun setText(svg: String, index: Int, text: String): String {
        require(text.length <= 500) { "单个文字元素最多 500 个字符" }
        var current = -1
        var changed = false
        val result = element.replace(svg) { m ->
            current++
            if (current != index || m.groupValues[1].isBlank()) m.value else {
                changed = true
                val open = m.value.indexOf('>')
                val close = m.value.lastIndexOf("</")
                if (open < 0 || close <= open) m.value else
                    m.value.substring(0, open + 1) + escapeXml(text) + m.value.substring(close)
            }
        }
        require(changed) { "所选元素不是可编辑的文字元素" }
        return result
    }

    private fun escapeXml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
}