package io.github.redflitzi.compactdecimal

internal fun getDivisor(decimalplaces: Int) : Long { // only for between 0 and 15!
    var divisor: Long = 1
    if (decimalplaces !in 0 .. 15) return 0 // solve somehow else, but shouldnt happen

    repeat (decimalplaces) { divisor *= 10 }

    return divisor
}

// first: for positive mantissa, second: for negative mantissa
internal val correctorMap: Map<Decimal.RoundingMode, Pair<Int, Int>> = mapOf<Decimal.RoundingMode, Pair<Int, Int>>(
    Decimal.RoundingMode.UP to Pair(9,-9),     // away from 0
    Decimal.RoundingMode.DOWN to Pair(0,0),    // down to 0
    Decimal.RoundingMode.CEILING to Pair(9,9), // up to positive infinity
    Decimal.RoundingMode.FLOOR to Pair(0,-9),   // down to negative infinity
    Decimal.RoundingMode.HALF_UP to Pair(5,-5),
    Decimal.RoundingMode.HALF_DOWN to Pair(4,-4),
    Decimal.RoundingMode.HALF_EVEN to Pair(5,-5),
    Decimal.RoundingMode.UNNECESSARY to Pair(0,0)
)

// desired precision can be below 0, but decimalplaces is between 0 and 15
internal fun roundWithMode(mantissa: Long, decimalplaces: Int, desiredprecision: Int, rounding: Decimal.RoundingMode): Pair<Long, Int> {
    if (desiredprecision >= decimalplaces) return Pair(mantissa, decimalplaces) // won't improve
    if ((rounding == Decimal.RoundingMode.UNNECESSARY) and (desiredprecision < decimalplaces)) throw ArithmeticException("Rounding is necessary")
    var precisiongap: Int
    var extraprecision: Int
    if (desiredprecision < 0) {
        precisiongap = decimalplaces
        extraprecision = 0-desiredprecision
    } else {
       precisiongap = decimalplaces - desiredprecision
       extraprecision = 0
    }
    val divisor1 = getDivisor(precisiongap)
    val (pos, neg) = correctorMap.get(rounding)?: 0 to 0

    var newmantissa = ((mantissa+(divisor1*if (mantissa < 0) neg; else pos)) / divisor1)
    var newdecimalplaces = if (desiredprecision >= 0) desiredprecision; else 0;

    if (desiredprecision < 0) {
        val divisor2 = getDivisor(extraprecision)
        newmantissa = ((newmantissa+(divisor2*if (newmantissa < 0) neg; else pos)) / divisor2) * divisor2
        // newdecimalplaces stays 0
    }

    return Pair(newmantissa, newdecimalplaces)

}

