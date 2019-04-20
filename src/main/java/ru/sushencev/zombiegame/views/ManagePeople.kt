package ru.sushencev.zombiegame.views

import com.googlecode.lanterna.graphics.TextGraphics
import com.googlecode.lanterna.input.KeyStroke
import ru.sushencev.zombiegame.*

class PersonView(private val person: Person) : Pane() {

    override fun doDraw(tg: TextGraphics) {
        super.doDraw(tg)

        tg.putCSIStyledString(1, 1, colorize(person.name, bold = true))
        tg.putCSIStyledString(1, 2, "Профессия: ${person.profession?.desc}")
        tg.putCSIStyledString(1, 3, "Пол: ${person.gender.desc}")
        tg.putCSIStyledString(1, 4, "Прошлое: ${person.past}")

        tg.putCSIStyledString(1, 6, "Состояние: ${person.mood.desc}")

        tg.putCSIStyledString(1, 8, "Особенности: ${person.trait.desc}")
        tg.putCSIStyledString(1, 9, "Отношения: ${person.relationShips.getDescription()}")
        //TerminalTextUtils.getWordWrappedText()
    }
}

private const val MP_VIEW_HORIZONTAL_MARGIN = 5
private const val MP_VIEW_VERTICAL_MARGIN = 2

class ManagePeopleView : GUIWithCommands() {
    private val scrollable = Scrollable()

    init {
        scrollable.restrict {
            TerminalSizeAndPosition(MP_VIEW_VERTICAL_MARGIN, MP_VIEW_HORIZONTAL_MARGIN,
                    it.columns - 2 * MP_VIEW_HORIZONTAL_MARGIN, it.rows - 2 * MP_VIEW_VERTICAL_MARGIN)
        }
        setCommands(closeActiveWindowCommand)
    }

    fun setPeople(people: List<Person>) {
        scrollable.setItems(people.map { "${it.name}${it.profession?.let { " (${it.desc})" } ?: ""}" to PersonView(it) })
    }

    override fun onKeyEvent(key: KeyStroke, game: Game) {
        super.onKeyEvent(key, game)
        scrollable.onKeyEvent(key, game)
    }

    override fun doDraw(tg: TextGraphics) {
        super.doDraw(tg)
        scrollable.draw(tg)
    }
}