package ru.sushencev.zombiegame

import java.util.*
import kotlin.math.max
import kotlin.math.min

private val random = Random()

fun randInt(fromInclusive: Int, toExclusive: Int): Int {
    return fromInclusive + random.nextInt(toExclusive - fromInclusive)
}

fun randInt(toExclusive: Int) = randInt(0, toExclusive)

fun dice(): Double = random.nextDouble()

fun <T> Map<T, Int>.roulette(): T {
    var pill = randInt(0, values.sum())
    this.forEach { (k, v) ->
        if (pill < v) return k
        pill -= v
    }
    error("unreachable code")
}

fun <T> List<T>.cautiousSubList(fromInclusive: Int, toExclusive: Int, fill: (Int) -> T): List<T> {
    require(fromInclusive <= toExclusive)
    val antecedentList = (fromInclusive until 0).map(fill)
    val subsequentList = (size until toExclusive).map(fill)
    val middleList = if (fromInclusive >= size || toExclusive < 0) {
        emptyList()
    } else {
        subList(max(0, fromInclusive), min(size, toExclusive))
    }
    return antecedentList + middleList + subsequentList
}