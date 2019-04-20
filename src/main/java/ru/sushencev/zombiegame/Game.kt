package ru.sushencev.zombiegame

import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ru.sushencev.zombiegame.views.GameLogView
import ru.sushencev.zombiegame.views.ManagePeopleView
import ru.sushencev.zombiegame.views.MapView
import ru.sushencev.zombiegame.views.SimpleMapGenerator
import java.util.*

class Game(private val commandsView: CommandsView, val colony: Colony) : AutoCloseable {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal().also {
        it.enterPrivateMode()
        it.setCursorVisible(false)
    }

    private val screen: TerminalScreen = TerminalScreen(terminal).also {
        it.cursorPosition = null
    }

    private val windowsStack = LinkedList<GUI>()
    private val commandsStack = LinkedList<List<ControlCommand>>()

    fun openActiveWindow(window: GUI) {
        windowsStack.add(window)
        if (window is CommandsControllable) commandsStack.add(window.commands)
        commandsView.commands = commandsStack.last
    }

    fun closeActiveWindow() {
        val closedWindow = windowsStack.removeLast()
        if (closedWindow is CommandsControllable) commandsStack.removeLast()
        commandsView.commands = commandsStack.last
    }

    fun loop() {
        screen.startScreen()

        while (true) {
            screen.doResizeIfNecessary()
            val tg = screen.newTextGraphics()

            val input: KeyStroke? = screen.pollInput()
            if (input != null) {
                windowsStack.last().onKeyEvent(input, this)
                commandsView.onKeyEvent(input, this)
            }
            if (needTerminate || input?.keyType == KeyType.EOF) break

            tg.fillRectangle(TerminalPosition.TOP_LEFT_CORNER, terminal.terminalSize, ' ')
            commandsView.draw(tg)
            tg.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, tg.size.withRelativeRows(-2)).also {
                windowsStack.forEach { window -> window.draw(it) }
            }

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

    val commandsView = CommandsView()

    val gameLogView = GameLogView(
            ControlCommand('m', "mission") {},
            ControlCommand('p', "manage people") {
                managePeopleView.setPeople(colony.dwellers)
                it.openActiveWindow(managePeopleView)
            },
            ControlCommand('f', "manage facilities") {},
            ControlCommand('M', "map") {
                mapView.center(colony.baseSite)
                it.openActiveWindow(mapView)
            }
    )

    Game(commandsView, colony).also { it.openActiveWindow(gameLogView) }.use(Game::loop)
}
