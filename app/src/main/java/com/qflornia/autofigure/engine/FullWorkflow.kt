package com.qflornia.autofigure.engine
import org.json.JSONObject
data class PipelineResult(val svg:String,val score:Double,val rounds:Int,val log:List<String>,val warnings:List<String>)
class FullWorkflow(private val api:ProviderRouter=ProviderRouter()) {
 fun generate(p:ModelProfile,request:String,topic:String,iterations:Int,threshold:Double):PipelineResult {
  val log=mutableListOf<String>()
  var svg=SvgTools.extract(api.chat(p,"Generate valid publication-quality scientific SVG.",ScientificPrompts.generate(topic,request)))
  log+="初稿生成完成"
  var score=0.0; var rounds=0
  for(i in 0 until iterations.coerceIn(1,5)) {
   var report=SvgTools.check(svg)
   if(!report.valid) { svg=SvgTools.extract(api.chat(p,"Repair SVG syntax and remove unsafe constructs. Return SVG only.",ScientificPrompts.revise(request,svg,report.errors.joinToString()))); report=SvgTools.check(svg) }
   val raw=api.chat(p,"You are a strict scientific figure reviewer. Return JSON only.",ScientificPrompts.evaluate(request,svg))
   val j=Regex("\\\\{[\\\\s\\\\S]*}").find(raw)?.value?.let{runCatching{JSONObject(it)}.getOrNull()}
   score=(j?.optDouble("score",0.0)?:0.0).coerceIn(0.0,10.0); rounds=i+1
   val feedback=j?.optString("revision_instructions").orEmpty()
   val issues=j?.optJSONArray("issues")?.let{a->(0 until a.length()).map{a.optString(it)}}.orEmpty()
   log+="第 ${i+1} 轮评价：${"%.1f".format(score)}/10；${issues.take(3).joinToString("；")}"
   if(score>=threshold || feedback.isBlank() || i==iterations-1) break
   svg=SvgTools.extract(api.chat(p,"Revise the scientific SVG. Return SVG only.",ScientificPrompts.revise(request,svg,feedback)))
   log+="已根据评价意见修订"
  }
  val final=SvgTools.check(svg)
  return PipelineResult(svg,score,rounds,log,final.errors+final.warnings)
 }
 fun enhancementPrompt(p:ModelProfile,svg:String,style:String,topic:String)=api.chat(p,"Convert vector figure into a precise image-generation prompt.",ScientificPrompts.enhance(svg,style,topic))
}
