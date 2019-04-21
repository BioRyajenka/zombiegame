package ru.sushencev.zombiegame

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TerminalPosition
import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import ru.sushencev.zombiegame.MyColor.*
import kotlin.math.max
import kotlin.math.min

interface KeyAware {
    fun onKeyEvent(key: KeyStroke, game: Game)
}

abstract class GUI : KeyAware {
    private var sizeAndPositionTranslation: (TerminalSize) -> TerminalSizeAndPosition = {
        TerminalSizeAndPosition(0, 0, it.columns, it.rows)
    }

    fun restrict(sizeAndPositionTranslation: (TerminalSize) -> TerminalSizeAndPosition) {
        this.sizeAndPositionTranslation = sizeAndPositionTranslation
    }

    protected abstract fun doDraw(tg: TextGraphics)

    fun draw(tg: TextGraphics) {
        val (i, j, width, height) = sizeAndPositionTranslation(tg.size)

        tg.newTextGraphics(TerminalPosition(j, i), TerminalSize(width, height)).also {
            it.fill(' ')
            doDraw(it)
        }
    }
}

data class ControlCommand(val key: Char, val name: String, val runnable: (Game) -> Unit)

val closeActiveWindowCommand = ControlCommand('q', "return") {
    it.closeActiveWindow()
}

interface CommandsControllable {
    val commands: List<ControlCommand>
}

class CommandsView constructor(vararg commands: ControlCommand) : GUI(), CommandsControllable {
    override var commands: List<ControlCommand> = commands.toList()

    init {
        restrict {
            TerminalSizeAndPosition(it.rows - 2, 0, it.columns, 2)
        }
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        commands.find { it.key == key.character }?.runnable?.invoke(game)
    }

    override fun doDraw(tg: TextGraphics) {
        tg.withColor(LIGHT_GRAY) {
            tg.drawLine(0, 0, tg.size.columns - 1, 0, Symbols.DOUBLE_LINE_HORIZONTAL)
        }
        val totalCommandsLength = commands.sumBy { it.name.length }
        var curPos = 0
        commands.forEach {
            val commandWidth = it.name.length / totalCommandsLength * (tg.size.columns - commands.size + 1)
//            val str = it.name.substring(0, max(min(it.name.length, commandWidth), it.name.length - 4))
            val str = it.name
            tg.putCSIStyledString(curPos, 1, " ${colorize(it.key, BLUE)} $str ")
            curPos += str.length + 4
            if (it !== commands.last()) {
                tg.putString(curPos, 1, Symbols.BOLD_SINGLE_LINE_VERTICAL.toString())
                curPos++
            }
        }
    }
}

abstract class Pane : GUI() {
    override fun onKeyEvent(key: KeyStroke, game: Game) = throw NotImplementedError()

    override fun doDraw(tg: TextGraphics) {
        tg.drawBorder(TerminalPosition.TOP_LEFT_CORNER, tg.size)
    }
}

open class ListView<T : Any>(private val itemToString: (T) -> String) : GUI() {
    private lateinit var items: List<T>

    private var selectedItemIndex: Int = 0
    val selectedItem: T get() = items[selectedItemIndex]

    constructor(items: List<T>, itemToString: (T) -> String) : this(itemToString) {
        setItems(items)
    }

    fun setItems(items: List<T>) {
        this.items = items
        selectedItemIndex = 0
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        selectedItemIndex = when (key.keyType) {
            KeyType.ArrowUp -> max(0, selectedItemIndex - 1)
            KeyType.ArrowDown -> min(items.size - 1, selectedItemIndex + 1)
            else -> return
        }
    }

    override fun doDraw(tg: TextGraphics) {
        items.forEachIndexed { i, item ->
            val s = itemToString(item).padEnd(tg.size.columns, ' ')
            val (fgColor, bgColor) = if (i == selectedItemIndex) {
                BLACK to WHITE
            } else {
                WHITE to BLACK
            }
            tg.putCSIStyledString(0, i, colorize(s, fgColor, bgColor))
        }
    }

}

typealias ScrollableItem = Pair<String, Pane>

private const val LISTITEM_WIDTH = 20

open class Scrollable constructor() : GUI() {
    constructor(items: List<ScrollableItem>) : this() {
        listView.setItems(items)
    }

    private val listView = ListView<ScrollableItem> { it.first }.also {
        it.restrict {
            val width = LISTITEM_WIDTH
            val height = it.rows
            val i = 1
            val j = it.columns - width
            TerminalSizeAndPosition(i, j, width, height)
        }
    }

    val setItems = listView::setItems

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        listView.onKeyEvent(key, game)
    }

    override fun doDraw(tg: TextGraphics) {
        val (width, height) = tg.size.columns to tg.size.rows
        tg.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, TerminalSize(width - LISTITEM_WIDTH, height)).also {
            listView.selectedItem.second.draw(it)
        }
        listView.draw(tg)
    }
}