package com.qflornia.autofigure.engine

object ScientificPrompts {
 fun generate(topic:String, request:String, w:Int=1333, h:Int=750) = """
You are AutoFigure, a scientific illustration engine. Return ONLY one complete editable SVG, no Markdown.
Canvas ${w}x${h}; viewBox="0 0 $w $h"; white background.
Topic: $topic
Request/methodology:
$request
Requirements: preserve all entities, labels, causal relations, directionality, panels and supplied facts.
Do not invent mechanisms, results, citations or values; mark unspecified details. Use native SVG text,
paths, rects, circles, lines, polygons and groups. Align panels, use consistent typography and line widths,
avoid overlap/clipping, use explicit arrowheads and a compact legend. No external images or scripts.
""".trimIndent()
 fun evaluate(request:String,svg:String) = """
Review this scientific figure. Return JSON only:
{"score":0-10,"issues":["..."],"revision_instructions":"...","factual_risks":["..."]}
Judge scientific fidelity, completeness, legibility, layout, consistency and editability.
Penalize invented facts, missing relations, overlaps, clipped text.
REQUEST: $request
SVG: ${svg.take(45000)}
""".trimIndent()
 fun revise(request:String,svg:String,feedback:String) = """
Revise this SVG to address the feedback without changing correct scientific meaning.
Return ONLY a complete SVG document, no Markdown.
REQUEST: $request
FEEDBACK: $feedback
CURRENT SVG: ${svg.take(45000)}
""".trimIndent()
 fun extractMethodology(paper:String) = """
Extract the paper's proposed methodology verbatim, preserving headings and equations.
Exclude datasets, experimental setup, results, ablations and evaluation. Return Markdown only.
PAPER: ${paper.take(100000)}
""".trimIndent()
 fun enhance(svg:String,style:String,topic:String) = """
Write a detailed image-generation prompt that preserves every scientific label, relation, arrow and panel
in this $topic SVG. Style: $style. Do not add data or scientific claims. SVG: ${svg.take(30000)}
""".trimIndent()
}
