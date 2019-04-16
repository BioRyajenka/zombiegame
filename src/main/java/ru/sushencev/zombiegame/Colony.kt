package ru.sushencev.zombiegame

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

enum class Profession(val desc: String) {
    SENTRY("часовой"),
    COOK("повар")
}

enum class Mood(val desc: String) {
    EXCITED("Воодушевлен"),
    IN_DESPERATION("В отчаянии"),
    DEPRESSED("В депрессии"),
    ALL_RIGHT("В порядке")
}

class Relationships {
    fun getDescription(): String? {
        // "Встречается с <...>"
        return null
    }
}

enum class Gender { MALE, FEMALE }

class Person(
        val gender: Gender,
        val name: String,
        val trait: Trait,
        val past: Past?,

        var moodValue: Int,
        val relationShips: Relationships,
        var profession: Profession? = null)

enum class ResourceType { FOOD, MATERIALS, AMMO, RADIO }

class Colony(val resources: MutableMap<ResourceType, Double>, val dwellers: MutableList<Person>, val site: Site) {
    companion object {
        fun createDefaultColony(map: GameMap): Colony {
            val resources = mutableMapOf(
                    ResourceType.RADIO to 4.0,
                    ResourceType.FOOD to 36.0,
                    ResourceType.AMMO to 1.0,
                    ResourceType.MATERIALS to 1.0
            )

            val dwellers = mutableListOf(
                    Person(Gender.MALE, "Джим", Trait.ENVIOUS, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Карлос", Trait.INDECISIVE, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Майк", Trait.CAUTIOUS, null, randInt(20, 80), Relationships()),
                    Person(Gender.FEMALE, "Клер", Trait.HYPOCHONDRIAC, null, randInt(20, 80), Relationships()),
                    Person(Gender.FEMALE, "Стефани", Trait.UNBALANCED, null, randInt(20, 80), Relationships()),
                    Person(Gender.MALE, "Джон", Trait.SLEEPY, null, randInt(20, 80), Relationships())
            )

            val site = map.reversed().map { row ->
                row.filterNot { it.type.decorative }.let { if (it.isEmpty()) null else it[randInt(it.size)] }
            }.let { rows: List<Site?> ->
                rows.drop(min(10, map.size)).find { it != null } ?: rows.find { it != null }
                ?: error("There is no dwellable sites on this map")
            }

            return Colony(resources, dwellers, site)
        }
    }
}