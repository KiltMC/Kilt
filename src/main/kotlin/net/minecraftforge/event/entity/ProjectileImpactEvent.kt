package net.minecraftforge.event.entity

import net.minecraft.world.entity.projectile.Projectile
import net.minecraft.world.phys.HitResult

class ProjectileImpactEvent(val projectile: Projectile, ray: HitResult) : EntityEvent(projectile) {
    val rayTraceResult = ray
}