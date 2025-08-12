package xyz.bluspring.kilt.compat.transfer.fluid

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil

object FluidTransferUtils {
    fun Long.toMillibuckets(): Int {
        return TransferUtil.truncateLong(this / 81)
    }

    fun Long.toMillibucketsLong(): Long {
        return this / 81
    }

    fun Int.toDroplets(): Long {
        return this * 81L
    }

    fun Long.toDroplets(): Long {
        return this * 81L
    }
}