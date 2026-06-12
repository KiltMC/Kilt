package xyz.bluspring.kilt.compat.fabric.sodium

import kotlin.math.max

object ModelQuadUtilExtension {

    // Copied from: https://github.com/FiniteReality/embeddium/blob/20.1/fabric/src/main/java/me/jellysquid/mods/sodium/client/util/ModelQuadUtil.java#L116-L128
    @JvmStatic
    fun mergeBakedLight(packedLight: Int, calcLight: Int): Int {
        // bail early in most cases
        if (packedLight == 0) return calcLight

        val psl = (packedLight shr 16) and 0xFF
        val csl = (calcLight shr 16) and 0xFF
        val pbl = (packedLight) and 0xFF
        val cbl = (calcLight) and 0xFF
        val bl = max(pbl, cbl)
        val sl = max(psl, csl)
        return (sl shl 16) or bl
    }


}
