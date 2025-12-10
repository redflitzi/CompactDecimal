package io.github.redflitzi.compactdecimal

internal fun getPower10(exponent: Int) : Long { // only for between 0 and 15!!!
    var power: Long = 1
    if (exponent !in 0 .. 15) return 0 // solve somehow else, but generally shouldn't happen
    repeat (exponent) { power *= 10 }
    return power
}

// first: multiplicator, second: a hint for the direction
internal fun getDivisor(roundingMode: Decimal.RoundingMode, pos: Boolean) : Pair<Int, Int> {
    when (roundingMode) {
        Decimal.RoundingMode.UP -> return if (pos) Pair(10, -1) else Pair(-10, 1)
        Decimal.RoundingMode.DOWN -> return Pair(0, 0)
        Decimal.RoundingMode.CEILING -> return if (pos) Pair(10, -1) else Pair(0, 0)
        Decimal.RoundingMode.FLOOR -> return if (pos) Pair(0, 0) else Pair(-10, 1)
        Decimal.RoundingMode.HALF_UP -> return if (pos) Pair(5, -0) else Pair(-5, 0)
        Decimal.RoundingMode.HALF_DOWN -> return if (pos) Pair(5, -1) else Pair(-5, 1)
        Decimal.RoundingMode.HALF_EVEN -> return Pair(5, 0) // steht noch aus
        Decimal.RoundingMode.UNNECESSARY -> return Pair(0, 0)
    }
}

// desired precision can be below 0, but decimalplaces is between 0 and 15, independent of autoprcision
internal fun roundWithMode(mantissa: Long, decimals: Int, desiredprecision: Int, roundingmode: Decimal.RoundingMode): Pair<Long, Int> {
    if (desiredprecision >= decimals) return Pair(mantissa, decimals) // won't improve
    if (roundingmode == Decimal.RoundingMode.UNNECESSARY) throw ArithmeticException("Rounding is necessary")
    println("\nold: m:$mantissa, d:$decimals, p:$desiredprecision, r:$roundingmode")
    var lower_rounding_dist: Int
    var upper_rounding_dist: Int
    if (desiredprecision >= 0) {
        lower_rounding_dist = decimals - desiredprecision
        upper_rounding_dist = 0
     } else {
        lower_rounding_dist = decimals
        upper_rounding_dist = 0 - desiredprecision
     }
    val (mult, hint)  = getDivisor(roundingmode, mantissa >=0)

    val lower_divisor = getPower10(lower_rounding_dist)
    val lower_corrig = ((lower_divisor * mult)/10)+hint

    println("Lower divisor: $lower_divisor, mult:$mult, hint:$hint , corr: $lower_corrig")

    var newmantissa = ((mantissa+lower_corrig) / lower_divisor)
    var newdecimals = if (desiredprecision >= 0) desiredprecision; else 0;
    println("new: m:$newmantissa, d:$newdecimals")

    if (upper_rounding_dist > 0) {

        val upper_divisor = getPower10(upper_rounding_dist)
        val upper_corrig = ((upper_divisor * mult)/10)+hint

        println("Upper divisor: $upper_divisor, mult:$mult, hint:$hint , corr: $upper_corrig")

        newmantissa = ((newmantissa+upper_corrig) / upper_divisor) * upper_divisor
        // and newdecimals stays 0 because rounding is left to comma!
        println("new: m:$newmantissa, d:$newdecimals")
    }

    return Pair(newmantissa, newdecimals)

}

