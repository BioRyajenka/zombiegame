package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import ru.sushencev.zombiegame.*
import ru.sushencev.zombiegame.MyColor.LIGHT_GRAY

class GameLogView(vararg commands: ControlCommand) : GUI(), CommandsControllable {
    override val commands: List<ControlCommand> = commands.toList()

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        when {
            key.character == 'q' -> {
                game.terminate()
                return
            }
        }
    }

    override fun doDraw(tg: TextGraphics) {
        tg.withColor(LIGHT_GRAY) {
            it.drawLine(0, 1, it.size.columns - 1, 1, Symbols.SINGLE_LINE_HORIZONTAL)
        }
        tg.putString(10, 0, "food: 15")

        // TODO: ScreenBuffer.scrollLines
    }
}