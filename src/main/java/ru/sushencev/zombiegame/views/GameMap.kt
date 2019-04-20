package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.Symbols
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.input.KeyType
import ru.sushencev.zombiegame.*
import ru.sushencev.zombiegame.MyColor.*
import ru.sushencev.zombiegame.views.SiteType.*

enum class SiteType(val char: Char, val color: TextColor = WHITE, val decorative: Boolean = false) {
    HOR_BORDER(Symbols.SINGLE_LINE_HORIZONTAL, TextColor.ANSI.WHITE, decorative = true),
    VER_BORDER(Symbols.SINGLE_LINE_VERTICAL, TextColor.ANSI.WHITE, decorative = true),
    TL_BORDER(Symbols.SINGLE_LINE_TOP_LEFT_CORNER, TextColor.ANSI.WHITE, decorative = true),
    TR_BORDER(Symbols.SINGLE_LINE_TOP_RIGHT_CORNER, TextColor.ANSI.WHITE, decorative = true),
    BL_BORDER(Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER, TextColor.ANSI.WHITE, decorative = true),
    BR_BORDER(Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER, TextColor.ANSI.WHITE, decorative = true),

    NOTHING(' ', decorative = true), GRASS(',', GREEN, decorative = true), ROAD('?', decorative = true),
    HOUSE('h'), BIG_HOUSE('H'), CHURCH('c'), SCHOOL('S'),
    HOSPITAL('H', RED),
    ELECTRONIC_STORE('e', BLUE), GUN_STORE('g', RED), FOOD_STORE('f', GREEN), RESTAURANT('R', GREEN),
    GAS_STATION('G', GRAY), PARK('p', GRAY), BANK('B', GRAY)
}

class Site(val type: SiteType, val i: Int, val j: Int)

abstract class MapGenerator {
    protected abstract fun doGenerate(width: Int, height: Int): GameMap

    fun generate(width: Int, height: Int): GameMap {
        require(width > 0 && height > 0)
        val map = generateSequence { doGenerate(width, height) }.find {
            it.any { row -> row.any { !it.type.decorative } }
        }!!
        val upperBorder = listOf(Site(TL_BORDER, -1, -1)) +
                (0 until map.width).map { Site(HOR_BORDER, -1, it) } +
                Site(TR_BORDER, -1, map.width)
        val bottomBorder = listOf(Site(BL_BORDER, -1, -1)) +
                (0 until map.width).map { Site(HOR_BORDER, -1, it) } +
                Site(BR_BORDER, -1, map.width)
        val newMap = map.mapIndexed { i, row ->
            listOf(Site(VER_BORDER, i, -1)) + row + Site(VER_BORDER, i, map.width)
        }
        return GameMap(listOf(upperBorder) + newMap + listOf(bottomBorder))
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

class GameMap(field: List<List<Site>>) : List<List<Site>> by field {
    val height: Int get() = this.size
    val width: Int get() = this.first().size
}

class MapView(private val map: GameMap) : GUIWithCommands() {
    init {
        setCommands(closeActiveWindowCommand)
    }

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
        val (ni, nj) = center.i + di to center.j + dj
        val siteType = if (ni in 0 until map.height && nj in 0 until map.width) {
            map[ni][nj].type
        } else NOTHING
        center = Site(siteType, ni, nj)
    }

    override fun doDraw(tg: TextGraphics) {
        super.doDraw(tg)
        val width = tg.size.columns
        val height = tg.size.rows - 2
        val rows = map.cautiousSubList(
                center.i - height / 2,
                center.i + height / 2 + height % 2) { emptyList() }
        rows.forEachIndexed { i, row ->
            val columns = row.cautiousSubList(
                    center.j - width / 2,
                    center.j + width / 2 + width % 2) { Site(NOTHING, i, it) }
            val rowString = columns.joinToString("") { colorize(it.type.char, it.type.color) }
            tg.putCSIStyledString(0, i, rowString)
        }
    }
}