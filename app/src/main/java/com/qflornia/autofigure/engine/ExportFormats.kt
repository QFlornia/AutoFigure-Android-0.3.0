package com.qflornia.autofigure.engine

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

object ExportFormats {
    fun svgToDrawio(svg: String, title: String = "AutoFigure diagram"): String {
        // Preserve SVG as an editable image cell in mxGraph. Individual SVG primitives
        // remain editable in SVG-aware editors; draw.io stores the SVG payload in one cell.
        val escaped = svg.replace("&", "&amp;").replace("\"", "&quot;")
            .replace("<", "&lt;").replace(">", "&gt;")
        val id = UUID.randomUUID().toString().take(8)
        return """<mxfile host="app.diagrams.net" modified="2026-11-03T00:00:00.000Z" agent="AutoFigure Android" version="24.7.17"><diagram id="$id" name="${xml(title)}"><mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" guides="1" tooltips="1" connect="1" arrows="1" fold="1" page="1" pageScale="1" pageWidth="1169" pageHeight="827"><root><mxCell id="0"/><mxCell id="1" parent="0"/><mxCell id="2" value="$escaped" style="shape=image;image=data:image/svg+xml,$escaped;verticalAlign=middle;aspect=fixed;" vertex="1" parent="1"><mxGeometry x="80" y="80" width="1000" height="650" as="geometry"/></mxCell></root></mxGraphModel></diagram></mxfile>"""
    }
    fun xml(s: String) = s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;")
}
