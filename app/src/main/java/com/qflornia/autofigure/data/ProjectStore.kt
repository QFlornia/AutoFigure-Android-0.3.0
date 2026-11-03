package com.qflornia.autofigure.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProjectStore(context: Context) {
    private val root = File(context.filesDir, "projects").apply { mkdirs() }
    data class Project(val name: String, val created: Long, val instruction: String, val svg: String, val score: Double)
    fun save(instruction: String, svg: String, score: Double): File {
        val now = System.currentTimeMillis()
        val name = "Figure_" + SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(now))
        val file = File(root, "$name.json")
        file.writeText(JSONObject().put("name", name).put("created", now).put("instruction", instruction)
            .put("svg", svg).put("score", score).toString())
        return file
    }
    fun list(): List<Project> = root.listFiles { f -> f.extension == "json" }?.mapNotNull { f ->
        runCatching { JSONObject(f.readText()).let { Project(it.optString("name"), it.optLong("created"), it.optString("instruction"), it.optString("svg"), it.optDouble("score")) } }.getOrNull()
    }?.sortedByDescending { it.created } ?: emptyList()
}
