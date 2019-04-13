package ru.sushencev.zombiegame

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics

const val ESC = 0x1B.toChar()

sealed class MyColor(r: Int, g: Int, b: Int) : TextColor by TextColor.Indexed.fromRGB(r, g, b)!! {
    object RED : MyColor(255, 4, 4)
    object GREEN : MyColor(97, 162, 71)
    object BLUE : MyColor(28, 43, 232)
    object ORANGE : MyColor(237, 125, 38)
    object GRAY : MyColor(125, 125, 125)
    object WHITE : MyColor(255, 255, 255)
    object BRIGHT_BLUE : MyColor(66, 111, 191)
//    val indexedColor = TextColor.Indexed.fromRGB(r, g, b)!!
}

fun colorize(s: String, color: TextColor): String {
    return "$ESC[${String(color.foregroundSGRSequence)}m$s$ESC[39m"
}

fun colorize(c: Char, color: TextColor) = colorize(c.toString(), color)

fun TextGraphics.clearScreen() {
    fillRectangle(TerminalPosition.TOP_LEFT_CORNER, size, ' ')
}