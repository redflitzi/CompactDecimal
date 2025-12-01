package io.github.redflitzi.compactdecimal

import kotlin.math.abs
import kotlin.math.min

//import kotlin.reflect.jvm.jvmName


public open class Decimal : Number, Comparable<Decimal> {

    // 60bit long mantissa plus 4 Bit int exponent (decimal places):
    private var decimal64: Long = 0L

    internal fun getdecimal64() : Long = decimal64

    internal fun unpack64(): Pair<Long, Int> {
        val decimalplaces: Int = (decimal64 and 0x0FL).toInt()
        val mantissa: Long = (decimal64 shr 4)
        return Pair(mantissa, decimalplaces)
    }

    internal fun pack64(mantissa: Long, decimalplaces: Int, omitNormalize:Boolean = false): Long {
        var compactMantissa = mantissa
        var compactDecimalPlaces = decimalplaces
        if ((decimalplaces != 0) and !(omitNormalize)) {
            val (cm, cd) = normalizeDecimalPlaces(mantissa, decimalplaces)
            compactMantissa = cm
            compactDecimalPlaces = cd
        }
        return ((compactMantissa shl 4) or (compactDecimalPlaces and 0x0F).toLong())
    }


    public companion object {

        // constructor imitations, just for JVM :-(
        // allows: Decimal(int), Decimal(Short), etc...
       // @JvmName("constFromByte")
        public operator fun invoke(input:Byte): Decimal = Decimal(input.toLong(),0, true)
       // @JvmName("constfromUByte")
        public operator fun invoke(input:UByte): Decimal = Decimal(input.toLong(),0, true)
       // @JvmName("constFromShort")
        public operator fun invoke(input:Short): Decimal = Decimal(input.toLong(),0,true)
       // @JvmName("constfromUShort")
        public operator fun invoke(input:UShort): Decimal = Decimal(input.toLong(),0, true)
      //  @JvmName("constFromInt")
        public operator fun invoke(input:Int): Decimal = Decimal(input.toLong(),0,true)
      //  @JvmName("constfromUInt")
        public operator fun invoke(input:UInt): Decimal= Decimal(input.toLong(),0,true)
      //  @JvmName("constFromLong")
        public operator fun invoke(input:Long): Decimal = Decimal(input,0,true)
      //  @JvmName("constfromULong")
        public operator fun invoke(input:ULong): Decimal = Decimal(input.toLong(),0, true)

        public const val MAX_VALUE: Long = +576460752303423487L
        public const val MIN_VALUE: Long = -576460752303423487L
        public const val NOT_A_NUMBER: Long = -576460752303423488L

        public val NaN: Decimal = Decimal(NOT_A_NUMBER,0, true)
        // static (common) variables and functions

        // for automatic rounding
        private var precision: Int = 15 /* 0 - 15 */
        public fun setPrecision(prec: Int) {
            /*
            if ((prec < 0) or (prec > 15)) {  // Error! wie?  }
            */
            precision = if (prec < 0) {
                0
            } else if (prec > 15) {
                15
            } else prec
        }

        // only for display toString()!
        private var mindecimals: Int = 0 /*  0 - max */
        public fun setMinDecimals(mind: Int) {
              mindecimals = if (mind < 0) 0; else mind
        }


        public enum class RoundingMode {
            UP,
            DOWN,
            CEILING,
            FLOOR,
            HALF_UP,
            HALF_DOWN,
            HALF_EVEN,
            UNNECESSARY
        }

        public fun abs(d : Decimal): Decimal {
            return d.abs()
        }


    }  // end of companion object

    public fun abs() : Decimal  {
        val (mantissa, decimalplaces) = unpack64()
        return Decimal(0-mantissa, decimalplaces)
    }

    internal data class EqualizedDecimalplaces(val thism:Long, val thatm: Long, val deci: Int)
    internal fun equalizeDecimalplaces(thism:Long, thisd: Int, thatm: Long, thatd: Int): EqualizedDecimalplaces {
        // error handling still missing!
        var thismantissa = thism
        var thisdecimalplaces = thisd
        var thatmantissa = thatm
        var thatdecimalplaces = thatd

        // error handling still missing!
        while (thisdecimalplaces < thatdecimalplaces) {
            thismantissa *= 10
            thisdecimalplaces++
        }
        while (thatdecimalplaces < thisdecimalplaces) {
            thatmantissa *= 10
            thatdecimalplaces++
        }

        return EqualizedDecimalplaces(thismantissa, thatmantissa, thisdecimalplaces)
    }

    private fun normalizeDecimalPlaces(mant:Long, deci:Int): Pair<Long, Int>{
        var mantissa = mant
        var decimalplaces = if (mantissa == 0L) 0; else deci
        val maxdecimalplaces = min(precision, 15)
        val isnegative = (mantissa < 0)

         if (isnegative) mantissa = 0-mantissa

        // most important, correct negative decimal places, as we dont support them
        while (decimalplaces < 0) {
            mantissa *=10
            decimalplaces++
        }

        // round to desired precision, HALF_UP (later on, use roundmode instead?)
        while ((decimalplaces > 0) and (mantissa != 0L) and ((decimalplaces > maxdecimalplaces) or ((mantissa % 10) == 0L))) {
             mantissa = (mantissa+5) / 10
            decimalplaces--
        }

        // at last truncate any empty decimal places
        /*
        while ((decimalplaces > 0) and (mantissa != 0L) and ((mantissa % 10) == 0L)) {
            //mantissa = (mantissa+5) / 10
            mantissa /= 10
            decimalplaces--
        }*/

        if (isnegative) mantissa = 0-mantissa

        return Pair(mantissa, if (mantissa == 0L)  0 else decimalplaces)
    }



    /**/
    public fun roundHalfEven(localprecision: Int = 3, roundingMode: RoundingMode = RoundingMode.HALF_UP): Decimal {
        val (mant, deci) = unpack64()
        var mantissa = mant
        var decimalplaces = deci
        val maxdecimalplaces = min(localprecision, 15)
        while (decimalplaces > maxdecimalplaces) {
            if ((mantissa == 0L) or (decimalplaces == 0)) break

            // https://lemire.me/blog/2020/04/16/rounding-integers-to-even-efficiently/
            // https://github.com/lemire/Code-used-on-Daniel-Lemire-s-blog/blob/master/2020/04/16/round.c
            // numerator n = mantissa, divisor d = 10, ist das richtig?
            var divisor: Long = 10
            var offsetnumerator = (mantissa + (divisor / 2))
            var roundup = (offsetnumerator / divisor)
            var down = roundup - (roundup and 1)
            var ismultiple = if ((offsetnumerator % divisor) == 0L) 1L; else 0L
             //mantissa = if (ismultiple && iseven) roundup - (roundup and 1) else roundup
            mantissa = (divisor  or (roundup xor ismultiple)) and roundup;
            decimalplaces--
        }
        return Decimal(mantissa, if (mantissa == 0L)  0 else decimalplaces)
    }

    public fun round(localprecision: Int, roundingMode: RoundingMode = RoundingMode.HALF_UP): Decimal {
        val (mant, deci) = unpack64()
        var mantissa = mant
        var decimalplaces = deci
        val maxdecimalplaces = min(localprecision, 15)
        while (decimalplaces > maxdecimalplaces) {
            if ((mantissa == 0L) or (decimalplaces == 0)) break
            mantissa = (mantissa+5) / 10
            decimalplaces--
        }
        return Decimal(mantissa, if (mantissa == 0L)  0 else decimalplaces)
    }

    private fun getdecimalstep(): Int {
        val (_, decimalplaces) = unpack64()
        var factor = 1
        if (decimalplaces > 0 ) for (zahl in 1..decimalplaces) factor *= 10
        return factor
    }

    /**********  Operators ************/

    /***  unary operators ***/

    /*** operator unaryPlus (+) , unaryMinus (-) and not() (!) ***/
    public operator fun unaryPlus() : Decimal = this

    public operator fun unaryMinus() : Decimal {
        var (mantissa, decimalplaces) = unpack64()
        mantissa = (0-mantissa)
        return Decimal(mantissa, decimalplaces, true)
    }

    public operator fun not() : Boolean = (decimal64 == 0L)

    public operator fun inc() : Decimal {
        val (mantissa, decimalplaces) = unpack64()
        val decimalstep = getdecimalstep()
        return Decimal(mantissa+decimalstep, decimalplaces, true)
    }
    public operator fun dec() : Decimal {
        val (mantissa, decimalplaces) = unpack64()
        val decimalstep = getdecimalstep()
        return Decimal(mantissa-decimalstep, decimalplaces, true)
    }

     /****************************************************/

    /*** operator plus (+) ***/

    public operator fun plus(other: Decimal) : Decimal {
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()
        val (thismantissa,thatmantissa, decimalplaces) = equalizeDecimalplaces(thism, thisd, thatm, thatd)
        return Decimal(thismantissa+thatmantissa, decimalplaces)
    }
    public operator fun plus(other: Double) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Float) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Long) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Int) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Short) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: Byte) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: ULong) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UInt) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UShort) : Decimal = plus(other.toDecimal())
    public operator fun plus(other: UByte) : Decimal = plus(other.toDecimal())

    /*** operator minus (-) ***/

    public operator fun minus(other: Decimal) : Decimal {
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()
        val (thismantissa,thatmantissa, decimalplaces) = equalizeDecimalplaces(thism, thisd, thatm, thatd)
        return Decimal(thismantissa-thatmantissa, decimalplaces)
    }
    public operator fun minus(other: Double) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Float) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Long) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Int) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Short) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: Byte) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: ULong) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UInt) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UShort) : Decimal = minus(other.toDecimal())
    public operator fun minus(other: UByte) : Decimal = minus(other.toDecimal())


    /*** operator times (*) ***/

    public operator fun times(other: Decimal) : Decimal {
        val (thismantissa, thisdecimalplaces) = unpack64()
        val (thatmantissa, thatdecimalplaces) = other.unpack64()
        return Decimal(thismantissa*thatmantissa, thisdecimalplaces+thatdecimalplaces)
    }
    public operator fun times(other: Double) : Decimal = times(other.toDecimal())
    public operator fun times(other: Float) : Decimal = times(other.toDecimal())
    public operator fun times(other: Long) : Decimal = times(other.toDecimal())
    public operator fun times(other: Int) : Decimal = times(other.toDecimal())
    public operator fun times(other: Short) : Decimal = times(other.toDecimal())
    public operator fun times(other: Byte) : Decimal = times(other.toDecimal())
    public operator fun times(other: ULong) : Decimal = times(other.toDecimal())
    public operator fun times(other: UInt) : Decimal = times(other.toDecimal())
    public operator fun times(other: UShort) : Decimal = times(other.toDecimal())
    public operator fun times(other: UByte) : Decimal = times(other.toDecimal())

    /*** operator div (/) ***/

    public operator fun div(other: Decimal) : Decimal {
        var (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()
        while ((thisd - thatd) < 17) {
            if ((thatm * (thism / thatm)) == thism) break // rest 0, done
            if (abs(thism) > (Long.MAX_VALUE/10)) break // would overflow
            thism *=10; thisd++
        }
        var resultm = (thism/thatm)
        var resultd = (thisd-thatd)
        while (resultd > min(precision, 15)) {
            resultm = (resultm+5)/10; resultd--
        }
        return Decimal(resultm, resultd)
    }
    public operator fun div(other: Double) : Decimal = div(other.toDecimal())
    public operator fun div(other: Float) : Decimal = div(other.toDecimal())
    public operator fun div(other: Long) : Decimal = div(other.toDecimal())
    public operator fun div(other: Int) : Decimal = div(other.toDecimal())
    public operator fun div(other: Short) : Decimal = div(other.toDecimal())
    public operator fun div(other: Byte) : Decimal = div(other.toDecimal())
    public operator fun div(other: ULong) : Decimal = div(other.toDecimal())
    public operator fun div(other: UInt) : Decimal = div(other.toDecimal())
    public operator fun div(other: UShort) : Decimal = div(other.toDecimal())
    public operator fun div(other: UByte) : Decimal = div(other.toDecimal())

    /*** operator rem (%), but what about modulo/mod ? ***/

    internal fun integerdivided(other: Decimal) : Decimal {
        var (thism, thisd) = unpack64()
        var (thatm, thatd) = other.unpack64()
        // preserve from running endlessly if thism cannot reach thatm!
        if (thatm > (Long.MAX_VALUE/10)) {
            thatm /= 10; thatd--
        }
        while (thism < thatm){
            //if ((thism * (thism / thatm)) == thatm) break
            thism *=10; thisd++
        }
        return Decimal(thism/thatm, thisd-thatd)
    }

    public operator fun rem(other:Decimal) : Decimal {
       val divisionresult = (this.integerdivided(other))
        return (this - (other * divisionresult))
    }
    public operator fun rem(other: Double) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Float) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Long) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Int) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Short) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: Byte) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: ULong) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UInt) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UShort) : Decimal = rem(other.toDecimal())
    public operator fun rem(other: UByte) : Decimal = rem(other.toDecimal())



    /* helpers */


    private fun shiftedMantissa() : Long  {
        val (mantissa, decimalplaces) = unpack64()
        var shift: Int
        when {
            (decimalplaces > 0) -> {
                shift = 10; repeat (decimalplaces) { shift *= 10 }
                return (mantissa / shift)
            }
            (decimalplaces < 0) -> {
                shift = 10; repeat (0-decimalplaces) {shift *= 10}
                return (mantissa * shift)
            }
            else -> return mantissa
        }

    }

    public override fun toDouble(): Double = this.toString().toDouble()
    public override fun toFloat(): Float = this.toString().toFloat()
    public override fun toLong(): Long = shiftedMantissa()
    public override fun toInt(): Int = shiftedMantissa().toInt()
    public override fun toShort(): Short = shiftedMantissa().toShort()
    public override fun toByte() : Byte = shiftedMantissa().toByte()
    public fun toULong(): ULong = shiftedMantissa().toULong()
    public fun toUInt(): UInt = shiftedMantissa().toUInt()
    public fun toUShort(): UShort = shiftedMantissa().toUShort()
    public fun toUByte(): UByte = shiftedMantissa().toUByte()



    /* how to round an integer

      Example: to next thousand (step = 1000)
      int offset = (number >= 0) ? (step / 2) : -(step/2);
      int roundedNumber = ((number + offset) / step) * step;

      missing: shiftplaces muss mit einfließen (step shiftmult. mit shiftplaces?)

      fun shiftmult: mult mit 10^shift

       */

    public fun toPlainString() : String {
        val (mantissa, decimalplaces) = unpack64()
        if (mantissa == 0L) return "0"
        var decimalString: String
        val prefix : String
        if (mantissa < 0) {
            decimalString = (0L - mantissa).toString(10); prefix = "-"
        }
        else {
            decimalString = mantissa.toString(10); prefix = ""
        }

        if (decimalplaces > 0) { // decimal digits exist, insert a dot
            var decimaldotpos = decimalString.count() - decimalplaces
            if (decimaldotpos <= 0) { // more than significant digits! prepend zeros!
              decimalString = "0"+"0".repeat(0-decimaldotpos)+decimalString
                decimaldotpos = 1
            }
            decimalString = decimalString.take(decimaldotpos) + '.' + decimalString.substring(decimaldotpos)
        }
        return prefix+decimalString
    }

    public fun toScientificString() : String {
        val (mantissa, decimalplaces) = unpack64()
        if (mantissa == 0L) return "0E0"
        var decimalString: String
        val prefix : String
        if (mantissa < 0) {
            decimalString = (0L - mantissa).toString(10)
            prefix = "-"
        }
        else {
            decimalString = mantissa.toString(10)
            prefix = ""
        }
        val adjustedExp = (decimalString.count()-1) - decimalplaces
        if (decimalString.count() > 1) decimalString = decimalString.take(1) + '.' + decimalString.substring(1).trimEnd('0')

        return prefix+decimalString+'E'+adjustedExp.toString(10)
    }

    public override fun toString() : String {
        val (_, decimalplaces) = unpack64()
        var decimalstring = this.toPlainString()
        // only for display!: add decimal places filled with "0"
        if (mindecimals > 0) {
            val  missingplaces = mindecimals - decimalplaces
            if (decimalplaces <= 0) decimalstring+='.'
            if (missingplaces > 0) decimalstring += ("0".repeat(missingplaces))
        }
        return decimalstring
    }

    /*** Comparable interface, and equality operators ***/

    public override operator fun compareTo(other: Decimal): Int {
        if (this.decimal64 == other.decimal64) return 0
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()

        val (thismantissa,thatmantissa, _) = equalizeDecimalplaces(thism, thisd, thatm, thatd)

        return when {
            (thismantissa > thatmantissa) -> 1
            (thismantissa < thatmantissa) -> -1
            else -> 0
        }
    }

    public override operator fun equals(other: Any?) : Boolean
        = ((other != null) and (other is Decimal)  and (this.decimal64 == (other as Decimal).decimal64))

    public override fun hashCode(): Int {
        return ((this.decimal64 ushr 32).toInt() xor (this.decimal64 and 0x00000000FFFFFFFFL).toInt())

    }

    /******************  Constructors  *******************/

    // see also the invoke expressions in Companion object, for all int types

    public constructor (inputstr: String) {

        val decimalNumberPattern = """(?<intg>[+-]?\d*)(?:\.(?<fract>\d*))?(?:[Ee](?<exp>[+-]?\d+))?"""
        val decimalNumberRegex = Regex(decimalNumberPattern)

        val match = decimalNumberRegex.matchEntire(inputstr) ?: return
        // will automatically call this(0L,0)

        val expn = (match.groups["exp"]?.value ?: "0").toInt()

        val fractString = (match.groups["fract"]?.value ?: "0").trimEnd('0')
        var decimPlcs = fractString.length

        var intgString = match.groups["intg"]?.value ?: ""

        var mantString = intgString + fractString
        decimPlcs -= expn                 // expn rechnet andersrum, 0 - expn = Nachkommastellen!

        if (mantString in listOf("+","- ", "")) mantString +="0"
        val mantissa: Long = mantString.toLong()
        val decimalplaces: Int = decimPlcs

        decimal64 = pack64(mantissa, decimalplaces)
    }

    public constructor (input:Float): this(input.toString())
    public constructor (input:Double): this(input.toString())

    internal constructor (mantissa: Long, decimalplaces: Int, omitNormalize:Boolean = false)  {
        decimal64 = pack64(mantissa,decimalplaces, omitNormalize)
    }


}


public fun abs(d : Decimal): Decimal {
    return d.abs()
}



