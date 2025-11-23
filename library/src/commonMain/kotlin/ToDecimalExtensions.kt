import io.github.redflitzi.compactdecimals.Decimal

public fun String.toDecimal():Decimal = Decimal(this)
public fun Float.toDecimal():Decimal = Decimal(this)
public fun Double.toDecimal():Decimal = Decimal(this)

public fun Byte.toDecimal():Decimal = Decimal(this)
public fun UByte.toDecimal():Decimal = Decimal(this)
public fun Short.toDecimal():Decimal = Decimal(this)
public fun UShort.toDecimal():Decimal = Decimal(this)
public fun Int.toDecimal():Decimal = Decimal(this)
public fun UInt.toDecimal():Decimal = Decimal(this)
public fun Long.toDecimal():Decimal = Decimal(this)
public fun ULong.toDecimal():Decimal = Decimal(this)

