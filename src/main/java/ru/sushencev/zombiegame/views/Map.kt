package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import ru.sushencev.zombiegame.GUI
import ru.sushencev.zombiegame.Game

abstract class MapGenerator

class Map(mapGenerator: MapGenerator) : GUI() {
    override fun onKeyEvent(key: KeyStroke, game: Game) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun draw(tg: TextGraphics) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}