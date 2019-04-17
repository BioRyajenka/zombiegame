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
    private var visible: Boolean = true

    private var sizeAndPositionTranslation: (TerminalSize) -> TerminalSizeAndPosition = {
        TerminalSizeAndPosition(0, 0, it.columns, it.rows)
    }

    fun restrict(sizeAndPositionTranslation: (TerminalSize) -> TerminalSizeAndPosition) {
        this.sizeAndPositionTranslation = sizeAndPositionTranslation
    }

    protected abstract fun doDraw(tg: TextGraphics)

    fun draw(tg: TextGraphics) {
        if (!visible) return

        val (i, j, width, height) = sizeAndPositionTranslation(tg.size)

        tg.newTextGraphics(TerminalPosition(j, i), TerminalSize(width, height)).also {
            it.fill(' ')
            doDraw(it)
        }
    }

    fun hide() {
        visible = false
    }

    fun show() {
        visible = true
    }
}

data class ControlCommand(val key: Char, val name: String, val runnable: (Game) -> Unit)

abstract class GUIWithCommands(private vararg val commands: ControlCommand) : GUI() {
    override fun onKeyEvent(key: KeyStroke, game: Game) {
        commands.find { it.key == key.character }?.runnable?.invoke(game)
    }

    override fun doDraw(tg: TextGraphics) {
        val width = tg.size.columns
        val height = tg.size.rows

        tg.drawLine(0, height - 2, width - 1, height - 2, Symbols.DOUBLE_LINE_HORIZONTAL)
        tg.drawLine(0, height - 1, width - 1, height - 1, ' ')
        val totalCommandsLength = commands.sumBy { it.name.length }
        var curPos = 0
        commands.forEach {
            val commandWidth = it.name.length / totalCommandsLength * (width - commands.size + 1)
//            val str = it.name.substring(0, max(min(it.name.length, commandWidth), it.name.length - 4))
            val str = it.name
            tg.putCSIStyledString(curPos, height - 1, " ${colorize(it.key, BLUE)} $str ")
            curPos += str.length + 4
            if (it !== commands.last()) {
                tg.putString(curPos, height - 1, Symbols.BOLD_SINGLE_LINE_VERTICAL.toString())
                curPos++
            }
        }
    }
}

abstract class Pane : GUI() {
    override fun doDraw(tg: TextGraphics) {
        // TODO: getWordWrappedText
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) = throw NotImplementedError()
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

class Scrollable constructor() : GUI() {
    constructor(items: List<ScrollableItem>) : this() {
        listView.setItems(items)
    }

    private val listView = ListView<ScrollableItem> { it.first }.also {
        it.restrict {
            val width = LISTITEM_WIDTH
            val height = it.rows
            val i = 0
            val j = it.columns - width
            TerminalSizeAndPosition(i, j, width, height)
        }
    }

    val setItems = listView::setItems

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        listView.onKeyEvent(key, game)
    }

    override fun doDraw(tg: TextGraphics) {
        tg.newTextGraphics(TerminalPosition.TOP_LEFT_CORNER, TerminalSize(LISTITEM_WIDTH, tg.size.rows)).also {
            listView.selectedItem.second.draw(it)
        }
        listView.draw(tg)
    }
}