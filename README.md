# Compact Decimals

### Compact Decimal class with small exponents and a predictive, smaller footprint than BigDecimal.  

KMP Multiplatform, only Common, no platform dependencies (and no restriction to JVM).


This Decimal has 64 bit:   
60 bit mantissa, 4 bit exponent

It's value range is from -576460752303423488 to +576460752303423487,  

with 0 - 15 decimal places.

It's small fixed 64bit footprint makes it possible to store it as an (unsigned) Long variable type anywhere where 8Byte places are available.
.
---

Based on the official Multiplatform Library Template [https://github.com/Kotlin/multiplatform-library-template](https://github.com/Kotlin/multiplatform-library-template).

