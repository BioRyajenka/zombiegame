package ru.sushencev.zombiegame

import java.util.*

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