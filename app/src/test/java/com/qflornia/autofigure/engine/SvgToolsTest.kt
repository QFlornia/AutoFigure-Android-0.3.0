package com.qflornia.autofigure.engine
import org.junit.Assert.*
import org.junit.Test
class SvgToolsTest {
 @Test fun extractsFencedSvg(){ val s=SvgTools.extract("```svg\n<svg xmlns=\"http://www.w3.org/2000/svg\"></svg>\n```"); assertTrue(s.startsWith("<svg")) }
 @Test fun rejectsScript(){ val r=SvgTools.check("<svg><script>alert(1)</script></svg>"); assertFalse(r.valid) }
 @Test fun acceptsBasicSvg(){ val r=SvgTools.check("<svg viewBox=\"0 0 10 10\"><text>x</text><rect/></svg>"); assertTrue(r.valid) }
}
