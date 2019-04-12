package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import ru.sushencev.zombiegame.GUI
import ru.sushencev.zombiegame.Game

typealias ControlCommand = Pair<Char, GUI>

class GameLogView(private val commands: List<ControlCommand>) : GUI() {
    override fun onKeyEvent(key: KeyStroke, game: Game) {
        when {
            key.character == 'q' -> {
                game.terminate()
                return
            }
        }
    }

    override fun draw(tg: TextGraphics) {
        tg.drawLine(0, 1, tg.size.columns - 1, 1, Symbols.DOUBLE_LINE_HORIZONTAL)
        tg.putString(10, 0, "food: 15")
    }
}