package io.github.redflitzi.compactdecimals

import kotlin.math.abs
//import kotlin.reflect.jvm.jvmName

public open class DecimalCore {

    // 64bit long plus 32 Bit exp:

    private var mantissa: Long = 0L
    private var decimalplaces: Int = 0


    public companion object {

        // constructor imitations, just for JVM :-(
        // allows: DecimalCore(int), DecimalCore(Short), etc...
       // @JvmName("constFromByte")
        public operator fun invoke(input:Byte): DecimalCore = DecimalCore(input.toLong(),0)
       // @JvmName("constfromUByte")
        public operator fun invoke(input:UByte): DecimalCore = DecimalCore(input.toLong(),0)
       // @JvmName("constFromShort")
        public operator fun invoke(input:Short): DecimalCore = DecimalCore(input.toLong(),0)
       // @JvmName("constfromUShort")
        public operator fun invoke(input:UShort): DecimalCore = DecimalCore(input.toLong(),0)
      //  @JvmName("constFromInt")
        public operator fun invoke(input:Int): DecimalCore = DecimalCore(input.toLong(),0)
      //  @JvmName("constfromUInt")
        public operator fun invoke(input:UInt): DecimalCore= DecimalCore(input.toLong(),0)
      //  @JvmName("constFromLong")
        public operator fun invoke(input:Long): DecimalCore = DecimalCore(input,0)
      //  @JvmName("constfromULong")
        public operator fun invoke(input:ULong): DecimalCore = DecimalCore(input.toLong(),0)

        // static (common) variables and functions

        private var precision: Int = 15 /* 0 - 15 */

        // see also class DecimalFormat (leider nur JVM).
        // stattdessen ebenso lösen? (nicht in Lib, sondern bei formatierter Darstellung)
        // only for display
        private var mindecimalplaces: Int = 0 /*  0 - 15 */

        // for automatic rounding
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
    }

    private fun normalize() {
        while ((mantissa % 10) == 0L) {
            if (mantissa == 0L) break
            mantissa /= 10
            decimalplaces--
        }


    }

/* how to round an integer

Example: to next thousand (step = 1000)
int offset = (number >= 0) ? (step / 2) : -(step/2);
int roundedNumber = ((number + offset) / step) * step;

missing: shiftplaces muss mit einfließen (step shiftmult. mit shiftplaces?)

fun shiftmult: mult mit 10^shift

 */



    public fun toPlainString() : String {
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

        if (decimalplaces > 0) { // decimal digits exist
            var pos = decimalString.count() - decimalplaces
            if (pos <= 0) { // more than significant digits! prepend zeros
              decimalString = "0"+"0".repeat(0-pos)+decimalString
                pos = 1
            }
            decimalString = decimalString.take(pos) + '.' + decimalString.substring(pos)
        } else if (decimalplaces < 0) { // factor exists
            decimalString += ("0".repeat(0-decimalplaces))
        }
        return prefix+decimalString
    }

    public fun toScientificString() : String {
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
        if (decimalString.count() > 1) decimalString = decimalString.take(1) + '.' + decimalString.substring(1)

        return prefix+decimalString+'E'+adjustedExp.toString(10)
    }

    public override fun toString() : String {
        // ab wann / wie / wo / warum Exponentialdarstellung?
        if (abs(mantissa) >= 10000000) return this.toScientificString()
        return this.toPlainString()
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


    /******************  Constructors  *******************/

    // see also the invoke expressions in Companion object, for all int types

    public constructor (inputstr: String) {

        val intpartpattern = """(?<intg>[+-]?\d*)(?:\.(?<fract>\d*))?"""
        val exppartpattern = """(?:[Ee](?<exp>[+-]?\d+))?"""
        val decimalNumberPattern = intpartpattern+exppartpattern
        val decimalNumberRegex = Regex(decimalNumberPattern)

        val match = decimalNumberRegex.matchEntire(inputstr) ?: return
        // will call this(0L,0)

        val expn = (match.groups["exp"]?.value ?: "0").toInt()

        val fractString = (match.groups["fract"]?.value ?: "0").trimEnd('0')
        var decimPlcs = fractString.length

        var intgString = match.groups["intg"]?.value ?: ""

        intgString += fractString
        decimPlcs -= expn                 // expn rechnet andersrum, expn < 0 = Nachkommastellen!

        var mantString = intgString.trimEnd('0')

        val tens = intgString.length - mantString.length
        if (mantString in listOf("+","- ", "")) mantString +="0"
        mantissa = mantString.toLong()
        decimalplaces = decimPlcs - tens

    }

    public constructor (input:Float): this(input.toString())
    public constructor (input:Double): this(input.toString())


    internal constructor (mant: Long, plcs: Int)  {
        mantissa = mant
        decimalplaces = plcs
        normalize()
    }


}

public fun Float.toDecimal():DecimalCore = DecimalCore(this)
public fun Double.toDecimal():DecimalCore = DecimalCore(this)

public fun Byte.toDecimal():DecimalCore = DecimalCore(this)
public fun UByte.toDecimal():DecimalCore = DecimalCore(this)
public fun Short.toDecimal():DecimalCore = DecimalCore(this)
public fun UShort.toDecimal():DecimalCore = DecimalCore(this)
public fun Int.toDecimal():DecimalCore = DecimalCore(this)
public fun UInt.toDecimal():DecimalCore = DecimalCore(this)
public fun Long.toDecimal():DecimalCore = DecimalCore(this)
public fun ULong.toDecimal():DecimalCore = DecimalCore(this)


