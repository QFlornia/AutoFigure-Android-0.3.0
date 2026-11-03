package com.qflornia.autofigure.engine
import java.util.UUID
data class SvgReport(val valid:Boolean,val errors:List<String>,val warnings:List<String>)
object SvgTools {
 fun extract(raw:String):String {
  val text=Regex("```(?:svg|xml)?\\s*([\\s\\S]*?)```",RegexOption.IGNORE_CASE).find(raw)?.groupValues?.get(1)?:raw
  val a=text.indexOf("<svg",true); val b=text.lastIndexOf("</svg>",true)
  require(a>=0 && b>a){"模型未返回完整 SVG"}; return text.substring(a,b+6).trim()
 }
 fun check(s:String):SvgReport {
  val e=mutableListOf<String>(); val w=mutableListOf<String>()
  if(!s.contains("<svg",true))e+="缺少 SVG 根元素"
  if(!s.contains("</svg>",true))e+="SVG 未闭合"
  listOf("<script","javascript:","onload=","onerror=","foreignObject").forEach{if(s.contains(it,true))e+="检测到危险内容：$it"}
  if(!s.contains("viewBox"))w+="建议设置 viewBox"
  if(!Regex("<text\\\\b").containsMatchIn(s))w+="未检测到文本标签，请检查标注"
  return SvgReport(e.isEmpty(),e,w)
 }
 fun toDrawio(svg:String):String {
  val b=android.util.Base64.encodeToString(svg.toByteArray(),android.util.Base64.NO_WRAP)
  return """<mxfile host="app.diagrams.net" agent="AutoFigure Android"><diagram id="${UUID.randomUUID()}" name="Scientific Figure"><mxGraphModel dx="1200" dy="800" grid="1" gridSize="10" page="1" pageWidth="1333" pageHeight="750"><root><mxCell id="0"/><mxCell id="1" parent="0"/><mxCell id="2" value="SVG artwork" style="shape=image;image=data:image/svg+xml;base64,$b;aspect=fixed;" vertex="1" parent="1"><mxGeometry x="0" y="0" width="1333" height="750" as="geometry"/></mxCell></root></mxGraphModel></diagram></mxfile>"""
 }
}
