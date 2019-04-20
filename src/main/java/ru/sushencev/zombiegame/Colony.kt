package ru.sushencev.zombiegame

import ru.sushencev.zombiegame.views.DwellingStatus
import ru.sushencev.zombiegame.views.GameMap
import ru.sushencev.zombiegame.views.Site
import kotlin.math.min


enum class Trait(val desc: String) {
    // тут херня. смысла нет, т.к. t возвращает строку
    ENVIOUS("завистливый"),
    INDECISIVE("нерешительный"),
    CAUTIOUS("осторожный"),
    HYPOCHONDRIAC("ипохондрик"),
    UNBALANCED("неуравновешенный"),
    SLEEPY("сонливый"),
    GLUTTON("обжора")
}

enum class Past {
    POLICEMAN, DENTIST
}

// TODO: ProfessionType, Повар-новичок etc.
enum class Profession(val desc: String) {
    SENTRY("часовой"),
    COOK("повар")
}

enum class Mood(val desc: String, val lowerBound: Int) {
    EXCITED("воодушевленное", 90),
    ALL_RIGHT("нормальное", 60),
    DEPRESSED("в депрессии", 40),
    IN_DESPERATION("в отчаянии", 20)
}

class Relationships {
    fun getDescription(): String {
        // "Встречается с <...>"
        return ""
    }
}

enum class Gender(val desc: String) { MALE("мужской"), FEMALE("женский") }

// TODO: nickname, like "Джим [Булава] Мёрдок"
class Person(
        val gender: Gender,
        val name: String,
        val surname: String,
        val trait: Trait,
        val past: Past?,

        var moodValue: Int,
        val relationShips: Relationships,
        var profession: Profession? = null) {
    val mood: Mood get() = Mood.values().sortedBy { it.lowerBound }.last { it.lowerBound < moodValue }
}

enum class ResourceType { FOOD, MATERIALS, AMMO, RADIO }

class Colony(val resources: MutableMap<ResourceType, Double>, val dwellers: MutableList<Person>, var baseSite: Site) {
    init {
        baseSite.dwellingStatus = DwellingStatus.INHABITED
    }

    fun resettle(newBaseSite: Site) {
        baseSite.dwellingStatus = DwellingStatus.ABANDONED
        baseSite = newBaseSite
        baseSite.dwellingStatus = DwellingStatus.INHABITED
    }

    companion object {
        fun createDefaultColony(map: GameMap): Colony {
            val resources = mutableMapOf(
                    ResourceType.RADIO to 4.0,
                    ResourceType.FOOD to 36.0,
                    ResourceType.AMMO to 1.0,
                    ResourceType.MATERIALS to 1.0
            )

            val dwellers = mutableListOf(
                    Person(Gender.MALE, "Джим", "Волтон", Trait.ENVIOUS, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Карлос", "Гардэл", Trait.INDECISIVE, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Майк", "Шинода", Trait.CAUTIOUS, null, randInt(20, 80), Relationships()),
                    Person(Gender.FEMALE, "Клер", "Флауэрс", Trait.HYPOCHONDRIAC, null, randInt(20, 80), Relationships(), Profession.COOK),
                    Person(Gender.FEMALE, "Стефани", "Питерсон", Trait.UNBALANCED, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Джон", "Лобб", Trait.SLEEPY, null, randInt(20, 80), Relationships())
            )

            val baseSite = map.reversed().map { row ->
                row.filterNot { it.type.decorative }.let { if (it.isEmpty()) null else it[randInt(it.size)] }
            }.let { rows: List<Site?> ->
                rows.drop(min(10, map.size)).find { it != null } ?: rows.find { it != null }
                ?: error("There is no dwellable sites on this map")
            }

            return Colony(resources, dwellers, baseSite)
        }
    }
}