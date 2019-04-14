package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import ru.sushencev.zombiegame.*
import ru.sushencev.zombiegame.MyColor.*
import ru.sushencev.zombiegame.views.SiteType.*
import kotlin.math.max
import kotlin.math.min

private val returnToGameLogViewCommand = ControlCommand('q', "return") {
    it.windows.find { it is MapView }!!.hide()
    it.activeWindow = it.windows.find { it is GameLogView }!!
}


enum class SiteType(val char: Char, val color: MyColor = WHITE, val decorative: Boolean = false) {
    NOTHING(' ', decorative = true), GRASS(',', GREEN, decorative = true), ROAD('?', decorative = true),
    HOUSE('h'), BIG_HOUSE('H'), CHURCH('c'), SCHOOL('S'),
    HOSPITAL('H', RED),
    ELECTRONIC_STORE('e', BLUE), GUN_STORE('g', RED), FOOD_STORE('f', GREEN), RESTAURANT('R', GREEN),
    GAS_STATION('G', GRAY), PARK('p', GRAY), BANK('B', GRAY)
}

// TODO: add MapPoint class with mutable i j
class Site(val type: SiteType, val i: Int, val j: Int)

abstract class MapGenerator {
    protected abstract fun doGenerate(width: Int, height: Int): GameMap

    fun generate(width: Int, height: Int): GameMap {
        require(width > 0 && height > 0)
        return generateSequence { doGenerate(width, height) }.find {
            it.field.any { row -> row.any { !it.type.decorative } }
        }!!
    }
}

class SimpleMapGenerator private constructor(private val decorationProbabilityWeights: Map<SiteType, Int>,
                                             private val probabilityWeights: Map<SiteType, Int>) : MapGenerator() {
    override fun doGenerate(width: Int, height: Int): GameMap {
        val beneficialSitesRate = 1.0 / 4 // approx every n-th

        val field = (0 until height).map { i ->
            val row = (0 until width).map { j ->
                Site(decorationProbabilityWeights.roulette(), i, j)
            }.toMutableList()
            if (dice() < beneficialSitesRate) {
                val j = randInt(row.size)
                row[j] = Site(probabilityWeights.roulette(), i, j)
            }
            row
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

class GameMap(val field: List<List<Site>>)

// TODO: add map border
class MapView(private val map: GameMap) : GUIWithCommands(returnToGameLogViewCommand) {
    lateinit var center: Site

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        super.onKeyEvent(key, game)
        val (di, dj) = when (key.keyType) {
            KeyType.ArrowUp -> -1 to 0
            KeyType.ArrowDown -> 1 to 0
            KeyType.ArrowLeft -> 0 to -1
            KeyType.ArrowRight -> 0 to 1
            else -> 0 to 0
        }
        center = Site(NOTHING, center.i + di, center.j + dj)
    }

    override fun draw(tg: TextGraphics) {
        tg.clearScreen()
        val width = tg.size.columns
        val height = tg.size.rows - 2
        val rows = map.field.cautiousSubList(
                center.i - height / 2,
                center.i + height / 2 + height % 2) { emptyList() }
        rows.forEachIndexed { i, row ->
            val columns = row.cautiousSubList(
                    center.j - width / 2,
                    center.j + width / 2 + width % 2) { Site(NOTHING, i, it) }
            val rowString = columns.joinToString("") { colorize(it.type.char, it.type.color) }
            tg.putCSIStyledString(0, i, rowString)
        }
        super.draw(tg)
    }
}