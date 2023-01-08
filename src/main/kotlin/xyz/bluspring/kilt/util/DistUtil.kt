package xyz.bluspring.kilt.util

import net.fabricmc.api.EnvType
import net.minecraftforge.api.distmarker.Dist

object DistUtil {
    fun distToEnvType(dist: Dist): EnvType {
        return when (dist) {
            Dist.CLIENT -> EnvType.CLIENT
            Dist.DEDICATED_SERVER -> EnvType.SERVER
            else -> throw IllegalStateException()
        }
    }

    fun isDistEqual(dist: Dist, envType: EnvType): Boolean {
        return distToEnvType(dist) == envType
    }
}