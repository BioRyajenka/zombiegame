package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.graphics.TextGraphics
import ru.sushencev.zombiegame.*
import ru.sushencev.zombiegame.MyColor.*
import ru.sushencev.zombiegame.views.SiteType.*

private val returnToGameLogViewCommand = ControlCommand('q', "return") {
    it.windows.find { it is MapView }!!.hide()
    it.activeWindow = it.windows.find { it is GameLogView }!!
}


enum class SiteType(val char: Char, val color: MyColor = WHITE) {
    NOTHING(' '), GRASS(',', GREEN), ROAD('?'),
    HOUSE('h'), BIG_HOUSE('H'), CHURCH('c'), SCHOOL('S'),
    HOSPITAL('H', RED),
    ELECTRONIC_STORE('e', BLUE), GUN_STORE('g', RED), FOOD_STORE('f', GREEN), RESTAURANT('R', GREEN),
    GAS_STATION('G', GRAY), PARK('p', GRAY), BANK('B', GRAY)
}

class Site(val type: SiteType)

abstract class MapGenerator

class SimpleMapGenerator private constructor(private val decorationProbabilityWeights: Map<SiteType, Int>,
                                             private val probabilityWeights: Map<SiteType, Int>) : MapGenerator() {
    fun generate(width: Int, height: Int): GameMap {
        val beneficialSitesRate = 1.0 / 4 // approx every n-th

        val field = Array(height) {
            val row = (0 until width).map { Site(decorationProbabilityWeights.roulette()) }.toMutableList()
            if (dice() < beneficialSitesRate) {
                row[randInt(row.size)] = Site(probabilityWeights.roulette())
            }
            row.toTypedArray()
        }

        return GameMap(field)
    }

    companion object {
        fun getInstance(): SimpleMapGenerator {
            val decorationProbabilityWeights = listOf(
                    NOTHING to 70,
                    GRASS to 1
            ).toMap()

            val probabilityWeights = listOf(
                    HOUSE to 7,
                    BIG_HOUSE to 2,
                    CHURCH to 1,
                    SCHOOL to 1,
                    HOSPITAL to 1,
                    ELECTRONIC_STORE to 1,
                    GUN_STORE to 1,
                    FOOD_STORE to 1,
                    RESTAURANT to 1,
                    GAS_STATION to 1,
                    PARK to 1,
                    BANK to 1
            ).toMap()
            return SimpleMapGenerator(decorationProbabilityWeights, probabilityWeights)
        }
    }
}

class GameMap(val field: Array<Array<Site>>)

class MapView(private val map: GameMap) : GUIWithCommands(returnToGameLogViewCommand) {
    override fun draw(tg: TextGraphics) {
        tg.clearScreen()
        map.field.forEachIndexed { i, row ->
            val rowString = row.joinToString("") { colorize(it.type.char, it.type.color) }
            tg.putCSIStyledString(0, i, rowString)
        }
        super.draw(tg)
    }
}