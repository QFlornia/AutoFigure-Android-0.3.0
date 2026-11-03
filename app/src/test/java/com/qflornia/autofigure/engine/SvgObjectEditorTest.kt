package com.qflornia.autofigure.engine

import org.junit.Assert.*
import org.junit.Test

class SvgObjectEditorTest {
    @Test fun listsBasicElements() {
        val items = SvgObjectEditor.list("""<svg><rect id="box" fill="#ffffff" /><text id="label">hello</text></svg>""")
        assertEquals(2, items.size)
        assertEquals("rect", items[0].tag)
        assertEquals("box", items[0].id)
    }

    @Test fun changesOnlySelectedFill() {
        val svg = """<svg><rect id="a" fill="#ffffff"/><circle id="b" fill="#000000"/></svg>"""
        val edited = SvgObjectEditor.setFill(svg, 1, "#ff0000")
        assertTrue(edited.contains("""id="a" fill="#ffffff""""))
        assertTrue(edited.contains("""id="b" fill="#ff0000""""))
    }
}
