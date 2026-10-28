package xyz.bluspring.kilt.workarounds

import net.minecraft.world.entity.projectile.ProjectileDeflection

object KiltNoOpImpls {
    @JvmField
    val NO_OP_PROJECTILE_DEFLECTION = ProjectileDeflection { _, _, _ ->
    }
}
