package ru.sushencev.zombiegame

import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal
import ru.sushencev.zombiegame.views.GameLogView

enum class ResourceType { FOOD, MATERIALS }

class Colony(val resources: MutableMap<ResourceType, Float>) {
}

class Game(var activeWindow: KeyAware, val windows: List<GUI>) : AutoCloseable {
    private val terminal: Terminal = DefaultTerminalFactory().createTerminal().also {
        it.enterPrivateMode()
        it.setCursorVisible(false)
    }

    private val screen: TerminalScreen = TerminalScreen(terminal)

    private val textGraphics = screen.newTextGraphics()
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
    val gameLogView = GameLogView(emptyList())
    Game(gameLogView, listOf(gameLogView)).use(Game::loop)
}
