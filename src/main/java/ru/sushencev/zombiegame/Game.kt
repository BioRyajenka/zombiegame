package ru.sushencev.zombiegame

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ru.sushencev.zombiegame.views.GameLogView
import ru.sushencev.zombiegame.views.MapView
import ru.sushencev.zombiegame.views.SimpleMapGenerator

class Game(var activeWindow: KeyAware, val windows: List<GUI>, val colony: Colony) : AutoCloseable {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal().also {
        it.enterPrivateMode()
        it.setCursorVisible(false)
    }

    private val screen: TerminalScreen = TerminalScreen(terminal)

    private val textGraphics = screen.newTextGraphics()//.also { it.foregroundColor = MyColor.WHITE }
    private var initialCursorPosition = screen.cursorPosition

//    fun refreshInitialCursorPosition() {
//        initialCursorPosition = screen.cursorPosition
//    }

    fun loop() {
        screen.startScreen()

        while (true) {
            screen.cursorPosition = initialCursorPosition
            screen.doResizeIfNecessary()

            val input: KeyStroke? = screen.pollInput()
            if (input != null) activeWindow.onKeyEvent(input, this)
            if (needTerminate || input?.keyType == KeyType.EOF) break

            textGraphics.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, terminal.terminalSize,  ' ')
            windows.forEach { if (it.visible) it.draw(textGraphics) }

            screen.refresh()
            Thread.yield()
        }
    }

    private var needTerminate = false

    fun terminate() {
        needTerminate = true
    }

    override fun close() {
        terminal.close()
        screen.close()
    }
}

fun main(args: Array<String>) {
    val map = SimpleMapGenerator.getInstance().generate(80, 70)
    val mapView = MapView(map).also { it.hide() }

    val colony = Colony.createDefaultColony(map)

    val controlCommands = arrayOf(
            ControlCommand('m', "mission") {},
            ControlCommand('p', "manage people") {},
            ControlCommand('f', "manage facilities") {},
            ControlCommand('M', "map") {
                mapView.center = colony.site
                mapView.show()
                it.activeWindow = mapView
            }
    )
    val gameLogView = GameLogView(*controlCommands)

    Game(gameLogView, listOf(gameLogView, mapView), colony).use(Game::loop)
}
