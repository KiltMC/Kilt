package xyz.bluspring.kilt.util.neoforge.fluid

object FluidTransferUtils {
    @JvmStatic
    fun truncateLong(long: Long): Int {
        if (long > Int.MAX_VALUE) {
            return Int.MAX_VALUE
        } else if (long < Int.MIN_VALUE) {
            return Int.MIN_VALUE
        }
        return long.toInt()
    }

    @JvmStatic
    fun Long.toMillibuckets(): Int {
        return truncateLong(this / 81)
    }

    @JvmStatic
    fun Long.toMillibucketsLong(): Long {
        return this / 81
    }

    @JvmStatic
    fun Int.toDroplets(): Long {
        return this * 81L
    }

    @JvmStatic
    fun Long.toDroplets(): Long {
        return this * 81L
    }
}
