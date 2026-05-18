package xyz.bluspring.kilt.util.neoforge.fluid

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil

object FluidTransferUtils {
    @JvmStatic
    fun Long.toMillibuckets(): Int {
        return TransferUtil.truncateLong(this / 81)
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