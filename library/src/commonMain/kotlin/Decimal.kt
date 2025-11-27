import kotlin.math.min
import kotlin.text.get

//import kotlin.reflect.jvm.jvmName


public open class Decimal : Number, Comparable<Decimal> {

    // 60bit long mantissa plus 4 Bit int exponent (decimal places):
     private var decimal64: Long = 0L

    internal fun unpack64(): Pair<Long, Int> {
        val decimalplaces: Int = (decimal64 and 0x0FL).toInt()
        val mantissa: Long = (decimal64 shr 4)
        return Pair(mantissa, decimalplaces)
    }

    internal fun pack64(mant: Long, deci: Int): Long {
        val(normalizedMant, normalizedDplcs) = normalizeMant(mant, deci)
        return ((normalizedMant shl 4) or (normalizedDplcs and 0x0F).toLong())
    }



    public companion object {

        // constructor imitations, just for JVM :-(
        // allows: DecimalCore(int), DecimalCore(Short), etc...
       // @JvmName("constFromByte")
        public operator fun invoke(input:Byte): Decimal = Decimal(input.toLong(),0)
       // @JvmName("constfromUByte")
        public operator fun invoke(input:UByte): Decimal = Decimal(input.toLong(),0)
       // @JvmName("constFromShort")
        public operator fun invoke(input:Short): Decimal = Decimal(input.toLong(),0)
       // @JvmName("constfromUShort")
        public operator fun invoke(input:UShort): Decimal = Decimal(input.toLong(),0)
      //  @JvmName("constFromInt")
        public operator fun invoke(input:Int): Decimal = Decimal(input.toLong(),0)
      //  @JvmName("constfromUInt")
        public operator fun invoke(input:UInt): Decimal= Decimal(input.toLong(),0)
      //  @JvmName("constFromLong")
        public operator fun invoke(input:Long): Decimal = Decimal(input,0)
      //  @JvmName("constfromULong")
        public operator fun invoke(input:ULong): Decimal = Decimal(input.toLong(),0)

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
    }

    internal data class AllMants(val thism:Long, val thatm: Long, val deci: Int)
    internal fun adjustMants(thism:Long, thisd: Int, thatm: Long, thatd: Int): AllMants {
        // error handling still missing!
        var thismantissa = thism
        var thisdecimalplaces=thisd
        var thatmantissa = thatm
        var thatdecimalplaces=thatd

        // error handling still missing!
        while (thisdecimalplaces < thatdecimalplaces) {
            thismantissa *= 10
            thisdecimalplaces++
        }
        while (thatdecimalplaces < thisdecimalplaces) {
            thatmantissa *= 10
            thatdecimalplaces++
        }

        return AllMants(thismantissa, thatmantissa, thisdecimalplaces)
    }

    private fun normalizeMant(mant:Long, deci:Int): Pair<Long, Int>{
        var mantissa = mant
        var decimalplaces = deci
        val maxdecimalplaces = min(precision, 15)

        // most important, correct negative decimal places
        while (decimalplaces < 0) {
            mantissa *=10
            decimalplaces++
        }

        // round to desired precision, HALF_UP (later on, use roundmode instead?)
        while ((decimalplaces > 0) and (mantissa != 0L) and (decimalplaces > maxdecimalplaces)) {
             mantissa = (mantissa+5) / 10
            decimalplaces--
        }

        // at last truncate empty decimal places
        while ((decimalplaces > 0) and (mantissa != 0L) and ((mantissa % 10) == 0L)) {
            mantissa /= 10
            decimalplaces--
         }
        return Pair(mantissa, if (mantissa == 0L)  0 else decimalplaces)
    }

    /*
    internal fun roundMant(mant:Long, deci:Int, roundingMode: RoundingMode): Pair<Long, Int>{
        var mantissa = mant
        var decimalplaces = deci
        val maxdecimalplaces = min(precision, 15)
        while (decimalplaces > maxdecimalplaces) {
            if ((mantissa == 0L) or (decimalplaces == 0)) break
            mantissa = (mantissa+5) / 10
            decimalplaces--
        }
        return Pair(mantissa, if (mantissa == 0L)  0 else decimalplaces)
    }
    */


    public operator fun plus(other: Decimal) : Decimal {
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()

        val (thismantissa,thatmantissa, decimalplaces) = adjustMants(thism, thisd, thatm, thatd)
        return Decimal(thismantissa+thatmantissa, decimalplaces)
    }
    public operator fun minus(other: Decimal) : Decimal {
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()

        val (thismantissa,thatmantissa, decimalplaces) = adjustMants(thism, thisd, thatm, thatd)
        return Decimal(thismantissa-thatmantissa, decimalplaces)
    }

    public operator fun times(other: Decimal) : Decimal {
        val (thismantissa, thisdecimalplaces) = unpack64()
        val (thatmantissa, thatdecimalplaces) = other.unpack64()
        //var (mantissa, decimalplaces) = normalizeMant(thismantissa*thatmantissa,thisdecimalplaces+thatdecimalplaces)
        return Decimal(thismantissa*thatmantissa, thisdecimalplaces+thatdecimalplaces)
    }
    public operator fun times(other: Int) : Decimal {
        val (mantissa, decimalplaces) = unpack64()
        return Decimal(mantissa*other, decimalplaces)
    }

    public operator fun div(other: Decimal) : Decimal {
        var (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()
        while ((thisd + thatd) < 15) {
            if ((thism * (thism / thatm)) == thatm) break
            thism *=10; thisd++
        }
        //var (mantissa, dplaces) = normalizeMant(thism/thatm,thisd-thatd)
        return Decimal(thism/thatm, thisd-thatd)
    }



    /*
    private fun normalize() {
        while ((mantissa % 10) == 0L) {
            if (mantissa == 0L) break
            mantissa /= 10
            decimalplaces--
        }
    }
    */

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
            decimalString = (0L - mantissa).toString(10)
            prefix = "-"
        }
        else {
            decimalString = mantissa.toString(10)
            prefix = ""
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
        var decstring = this.toPlainString()
        // only for the optics
        if (mindecimals > 0) {
            val  missingplaces = mindecimals - decimalplaces
            if (decimalplaces <= 0) decstring+='.'
            if (missingplaces > 0) decstring += ("0".repeat(missingplaces))
        }

        return decstring
        // evtl hier mindecimalplaces formatierung
    }

/*
    fun toFormattedString() {

        var decstring = this.toPlainString()

        // now to the optics
        // oder ist das nur ein Anzeigeproblem und gehört hier nicht her / nicht zur class?
        if (mindecimalplaces > 0) {
            val  missingplaces = mindecimalplaces - decplaces
            if (decplaces <= 0) decstring+='.'
            decstring += ("0".repeat(missingplaces))

        } else if (mindecimalplaces < 0) { // does this exist?
            // von rechts kürzen = evtl. mit round() anzeigen
        } else { /* mindecimalplaces == 0 */

            // no reason to touch.
            // if there is no minimum, everything is ok

        }

        return decstring
    }

 */

    override operator fun compareTo(other: Decimal): Int {
        val (thism, thisd) = unpack64()
        val (thatm, thatd) = other.unpack64()

        val (thismantissa,thatmantissa, _) = adjustMants(thism, thisd, thatm, thatd)

        return when {
            (thismantissa > thatmantissa) -> 1
            (thismantissa < thatmantissa) -> -1
            else -> 0
        }
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


    internal constructor (mant: Long, plcs: Int)  {
        decimal64 = pack64(mant,plcs)
    }


}


