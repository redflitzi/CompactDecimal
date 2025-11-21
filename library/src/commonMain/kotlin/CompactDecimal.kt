package io.github.redflitzi.compactdecimals

public enum class CompactRoundingMode {
    UP,
    DOWN,
    CEILING,
    FLOOR,
    HALF_UP,
    HALF_DOWN,
    HALF_EVEN,
    UNNECESSARY
}


public class CompactDecimal /* : DecimalCore */{

    // 64bit:
    // upper 4 Bit: exp = exponent, unsigned ( exp ist 0 - 15)
    // lower 60bit: mant = mantisse, signed


    private var smdecimal: ULong = 0UL

    public companion object {

        private const val PLCS_FILT =  0xF000000000000000UL
        //private const val PLCS_SIZE = 4
        private const val MANT_SIZE = 60
        private const val MANT_FILT = 0x0FFFFFFFFFFFFFFFUL
        private const val SIGN_FILT = 0x0800000000000000UL

        private var precision: Int = 15 /* 0 - 15 */
        private var dispplcs: Int = 0 /* 0 - 15 */

       public fun setPrecision(prec: Int) {
            if ((prec < 0) or (prec > 15)) {
                // Error! wie?
            }
            if (prec < 0) {  precision = 0 }
            else if (prec > 15) { precision = 15 }
            else precision = prec
        }
    }


    public fun getUnpack(): Pair<Long, Int> {
        val plcs: Int = (smdecimal shr MANT_SIZE).toInt()
        var mant: ULong = (smdecimal and MANT_FILT)
        if ((mant and SIGN_FILT) != 0UL) {
            mant = mant or PLCS_FILT
        }
        return Pair(mant.toLong(), plcs)
    }



    override fun toString() : String {
        val (mant, plcs) = getUnpack()
        val core = DecimalCore(mant, plcs)
        return core.toString()
    }


    /*************************************/

    internal constructor (mant: Long, plcs: Int)  {
        // mant can have only 60 digits, upper four = forbidden
        if ((mant.toULong() and PLCS_FILT) != 0UL) {
            // ERROR! Overflow!
        }
        var tmpm: ULong = plcs.toULong() shl (MANT_SIZE)
        tmpm = tmpm or (mant.toULong() and MANT_FILT)
        // Normalisieren fehlt
        smdecimal = tmpm
    }

}