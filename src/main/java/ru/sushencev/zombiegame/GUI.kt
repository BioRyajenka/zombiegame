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

    abstract fun doDraw(tg: TextGraphics)

    open fun draw(tg: TextGraphics) {
        if (!visible) return
        tg.fill(' ')
        doDraw(tg)
    }

    fun hide() {
        visible = false
    }

    fun show() {
        visible = true
    }
}

fun GUI.restricted(iFunction: (TerminalSize) -> Int,
                   jFunction: (TerminalSize) -> Int,
                   widthFunction: (TerminalSize) -> Int,
                   heightFunction: (TerminalSize) -> Int): GUI {
    return object : GUI() {
        override fun onKeyEvent(key: KeyStroke, game: Game) = this@restricted.onKeyEvent(key, game)

        override fun doDraw(tg: TextGraphics) {
            val i = iFunction(tg.size)
            val j = jFunction(tg.size)
            val width = widthFunction(tg.size)
            val height = heightFunction(tg.size)

            val restrictedTG = tg.newTextGraphics(TerminalPosition(j, i), TerminalSize(height, width))

            this@restricted.draw(restrictedTG)
        }

    }
}

data class ControlCommand(val key: Char, val name: String, val runnable: (Game) -> Unit)

abstract class GUIWithCommands(private vararg val commands: ControlCommand) : GUI() {
    override fun onKeyEvent(key: KeyStroke, game: Game) {
        commands.find { it.key == key.character }?.runnable?.invoke(game)
    }

    override fun draw(tg: TextGraphics) {
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

class Pane(val content: String) : GUI() {
    override fun doDraw(tg: TextGraphics) {
        // TODO: getWordWrappedText
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) = throw NotImplementedError()
}

abstract class ListView<T : Any> constructor() : GUI() {
    private lateinit var items: List<T>
    private lateinit var itemToString: (T) -> String

    private var selectedItemIndex: Int = 0

    constructor(items: List<T>, itemToString: (T) -> String) : this() {
        setItems(items, itemToString)
    }

    fun setItems(items: List<T>, itemToString: (T) -> String) {
        this.items = items
        this.itemToString = itemToString
        selectedItemIndex = 0
        onItemChange(items.first())
    }

    abstract fun onItemChange(item: T)

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        selectedItemIndex = when (key.keyType) {
            KeyType.ArrowUp -> max(0, selectedItemIndex - 1)
            KeyType.ArrowDown -> min(items.size - 1, selectedItemIndex + 1)
            else -> return
        }
        onItemChange(items[selectedItemIndex])
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

abstract class Scrollable : GUI() {
    /*fun setItems(items: List<Pair<String, Pane>>) {
        require(items.isNotEmpty())
        require(items.distinct().size == items.size) {
            "Items should be different due to specific selection process"
        }
        this.items = items
        selectedItem = items.first()
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        selectedItem = when (key.keyType) {
            KeyType.ArrowUp -> items[max(0, items.indexOf(selectedItem) - 1)]
            KeyType.ArrowDown -> items[min(items.size - 1, items.indexOf(selectedItem) + 1)]
            else -> selectedItem
        }
    }

    override fun draw(tg: TextGraphics) {
        selectedItem.second.draw(tg)
    }*/
}