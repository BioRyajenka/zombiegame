package ru.sushencev.zombiegame

import com.googlecode.lanterna.*
import com.googlecode.lanterna.graphics.TextGraphics
import ru.sushencev.zombiegame.MyColor.*

const val ESC = 0x1B.toChar()

@Suppress("ClassName")
sealed class MyColor(r: Int, g: Int, b: Int) : TextColor by TextColor.Indexed.fromRGB(r, g, b)!! {
    object RED : MyColor(255, 4, 4)
    object GREEN : MyColor(97, 162, 71)
    object BLUE : MyColor(28, 43, 232)
    object ORANGE : MyColor(237, 125, 38)
    object GRAY : MyColor(125, 125, 125)
    object WHITE : MyColor(255, 255, 255)
    object BLACK : MyColor(0, 0, 0)
    object BRIGHT_BLUE : MyColor(66, 111, 191)

    object DEFAULT_COLOR : MyColor(170, 170, 170)
}

fun colorize(s: String, color: TextColor = DEFAULT_COLOR, bgColor: TextColor = BLACK, bold: Boolean = false): String {
    require(s.none(TerminalTextUtils::isControlCharacter))
    val fgColorSequence = String(color.foregroundSGRSequence)
    val bgColorSequence = String(bgColor.backgroundSGRSequence)
    val stylesSequence = if (bold) "1" else ""
    return "$ESC[$fgColorSequence;$bgColorSequence;${stylesSequence}m$s$ESC[39m"
}

fun colorize(c: Char, color: TextColor = DEFAULT_COLOR, bgColor: TextColor = BLACK): String {
    return colorize(c.toString(), color, bgColor)
}

data class TerminalSizeAndPosition(val i: Int, val j: Int, val width: Int, val height: Int)

fun TextGraphics.drawBorder(topLeft: TerminalPosition, size: TerminalSize) {
    val horChar = Symbols.SINGLE_LINE_HORIZONTAL
    val verChar = Symbols.SINGLE_LINE_VERTICAL
    val tlChar = Symbols.SINGLE_LINE_TOP_LEFT_CORNER
    val trChar = Symbols.SINGLE_LINE_TOP_RIGHT_CORNER
    val blChar = Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER
    val brChar = Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER

    drawLine(topLeft.withRelativeColumn(1), topLeft.withRelativeColumn(size.columns - 2), horChar)
    drawLine(topLeft.withRelative(1, size.rows - 1),
            topLeft.withRelative(size.columns - 2, size.rows - 1), horChar)
    drawLine(topLeft.withRelativeRow(1), topLeft.withRelativeRow(size.rows - 2), verChar)
    drawLine(topLeft.withRelative(size.columns - 1, 1),
            topLeft.withRelative(size.columns - 1, size.rows - 2), verChar)
    setCharacter(topLeft, tlChar)
    setCharacter(topLeft.withRelativeRow(size.rows - 1), blChar)
    setCharacter(topLeft.withRelativeColumn(size.columns - 1), trChar)
    setCharacter(topLeft.withRelative(size.columns - 1, size.rows - 1), brChar)
}