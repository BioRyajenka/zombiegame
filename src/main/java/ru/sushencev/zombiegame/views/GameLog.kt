package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import ru.sushencev.zombiegame.ControlCommand
import ru.sushencev.zombiegame.GUIWithCommands
import ru.sushencev.zombiegame.Game

class GameLogView(vararg commands: ControlCommand) : GUIWithCommands(*commands) {
    override fun onKeyEvent(key: KeyStroke, game: Game) {
        super.onKeyEvent(key, game)
        when {
            key.character == 'q' -> {
                game.terminate()
                return
            }
        }
    }

    override fun draw(tg: TextGraphics) {
        super.draw(tg)
        tg.drawLine(0, 1, tg.size.columns - 1, 1, Symbols.DOUBLE_LINE_HORIZONTAL)
        tg.putString(10, 0, "food: 15")
    }
}