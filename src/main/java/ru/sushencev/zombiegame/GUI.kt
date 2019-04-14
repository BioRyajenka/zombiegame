package ru.sushencev.zombiegame

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import ru.sushencev.zombiegame.MyColor.BLUE
import kotlin.math.max
import kotlin.math.min

interface KeyAware {
    fun onKeyEvent(key: KeyStroke, game: Game)
}

abstract class GUI : KeyAware {
    var visible: Boolean = true

    abstract fun draw(tg: TextGraphics)

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
    override fun draw(tg: TextGraphics) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) = throw NotImplementedError()
}

typealias ScrollableItem = Pair<String, Pane>

abstract class Scrollable constructor(private var items: List<ScrollableItem>) : GUI() {
    constructor() : this(emptyList())

    private lateinit var selectedItem: ScrollableItem

    fun setItems(items: List<Pair<String, Pane>>) {
        require(items.isNotEmpty())
        require(items.distinct().size == items.size) {
            "Items should be different due to specific selection process"
        }
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
    }
}