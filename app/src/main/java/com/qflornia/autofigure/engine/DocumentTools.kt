package com.qflornia.autofigure.engine

import android.content.Context
import android.net.Uri
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper

object DocumentTools {
    fun extractPdfText(context: Context, uri: Uri): String {
        PDFBoxResourceLoader.init(context)
        context.contentResolver.openInputStream(uri).use { input ->
            requireNotNull(input) { "无法打开 PDF 文件" }
            PDDocument.load(input).use { doc ->
                require(doc.numberOfPages > 0) { "PDF 没有页面" }
                return PDFTextStripper().getText(doc).trim().take(120_000)
            }
        }
    }
}
