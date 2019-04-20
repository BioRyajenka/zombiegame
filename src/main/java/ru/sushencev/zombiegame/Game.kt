package ru.sushencev.zombiegame

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ru.sushencev.zombiegame.views.*
import java.util.*

class Game(baseWindow: GUI, val colony: Colony) : AutoCloseable {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal().also {
        it.enterPrivateMode()
        it.setCursorVisible(false)
    }

    private val screen: TerminalScreen = TerminalScreen(terminal).also {
        it.cursorPosition = null
    }

    private val windows = LinkedList<GUI>().also { it.add(baseWindow) }

    fun openActiveWindow(window: GUI) {
        windows.add(window)
    }

    fun closeActiveWindow() {
        windows.removeLast()
    }

    fun loop() {
        screen.startScreen()

        while (true) {
            screen.doResizeIfNecessary()
            val textGraphics = screen.newTextGraphics()

            val input: KeyStroke? = screen.pollInput()
            if (input != null) windows.last().onKeyEvent(input, this)
            if (needTerminate || input?.keyType == KeyType.EOF) break

            textGraphics.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, terminal.terminalSize, ' ')
            windows.forEach { it.draw(textGraphics) }

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
    // default is 80x24
    val map = SimpleMapGenerator.getInstance().generate(80, 70)
    val mapView = MapView(map)

    val colony = Colony.createDefaultColony(map)
    val managePeopleView = ManagePeopleView()

    val gameLogView = GameLogView(
            ControlCommand('m', "mission") {},
            ControlCommand('p', "manage people") {
                managePeopleView.setPeople(colony.dwellers)
                it.openActiveWindow(managePeopleView)
            },
            ControlCommand('f', "manage facilities") {},
            ControlCommand('M', "map") {
                mapView.center = colony.site
                it.openActiveWindow(mapView)
            }
    )

    Game(gameLogView, colony).use(Game::loop)
}
