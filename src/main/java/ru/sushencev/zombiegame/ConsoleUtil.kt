package ru.sushencev.zombiegame

import com.googlecode.lanterna.TextColor
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

fun colorize(s: String, color: TextColor = DEFAULT_COLOR, bgColor: TextColor = BLACK): String {
    val fgColorSequence = String(color.foregroundSGRSequence)
    val bgColorSequence = String(bgColor.backgroundSGRSequence)
    return "$ESC[$fgColorSequence;${bgColorSequence}m$s$ESC[39m"
}

fun colorize(c: Char, color: TextColor = DEFAULT_COLOR, bgColor: TextColor = BLACK): String {
    return colorize(c.toString(), color, bgColor)
}

data class TerminalSizeAndPosition(val i: Int, val j: Int, val width: Int, val height: Int)