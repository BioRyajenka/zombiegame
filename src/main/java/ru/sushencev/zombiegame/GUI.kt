package ru.sushencev.zombiegame

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import kotlin.math.max
import kotlin.math.min

interface KeyAware {
    fun onKeyEvent(key: KeyStroke, game: Game)
}

abstract class GUI : KeyAware {
    abstract fun draw(tg: TextGraphics)
}

abstract class Pane(val content: String) : GUI() {
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
        selectedItem = when {
            key.keyType == KeyType.ArrowUp -> items[max(0, items.indexOf(selectedItem) - 1)]
            key.keyType == KeyType.ArrowDown -> items[min(items.size - 1, items.indexOf(selectedItem) + 1)]
            else -> selectedItem
        }
    }

    override fun draw(tg: TextGraphics) {
        selectedItem.second.draw(tg)
    }
}