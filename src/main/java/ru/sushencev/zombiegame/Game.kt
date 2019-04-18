package ru.sushencev.zombiegame

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ru.sushencev.zombiegame.views.*

class Game(var activeWindow: KeyAware, val windows: List<GUI>, val colony: Colony) : AutoCloseable {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal().also {
        it.enterPrivateMode()
        it.setCursorVisible(false)
    }

    private val screen: TerminalScreen = TerminalScreen(terminal).also {
        it.cursorPosition = null
    }

    fun loop() {
        screen.startScreen()

        while (true) {
            screen.doResizeIfNecessary()
            val textGraphics = screen.newTextGraphics()

            val input: KeyStroke? = screen.pollInput()
            if (input != null) activeWindow.onKeyEvent(input, this)
            if (needTerminate || input?.keyType == KeyType.EOF) break

            textGraphics.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, terminal.terminalSize,  ' ')
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
    // 80x24 is default
    val map = SimpleMapGenerator.getInstance().generate(80, 70)
    val mapView = MapView(map).also(GUI::hide)

    val colony = Colony.createDefaultColony(map)
    val managePeopleView = ManagePeopleView().also(GUI::hide)

    val controlCommands = arrayOf(
            ControlCommand('m', "mission") {},
            ControlCommand('p', "manage people") {
                managePeopleView.setPeople(colony.dwellers)
                managePeopleView.show()
                it.activeWindow = managePeopleView
            },
            ControlCommand('f', "manage facilities") {},
            ControlCommand('M', "map") {
                mapView.center = colony.site
                mapView.show()
                it.activeWindow = mapView
            }
    )
    val gameLogView = GameLogView(*controlCommands)


    Game(gameLogView, listOf(gameLogView, mapView, managePeopleView), colony).use(Game::loop)
//    val pv = PersonView()
//    Game(pv, listOf(pv), colony).use(Game::loop)

//    val scrollable = Scrollable(items = listOf(
//            "abc" to Pane("abc"),
//            "def" to Pane("def")
//    ))
//    Game(scrollable, listOf(scrollable), colony).use(Game::loop)
}
