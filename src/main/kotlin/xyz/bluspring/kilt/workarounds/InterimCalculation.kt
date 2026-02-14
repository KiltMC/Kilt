package xyz.bluspring.kilt.workarounds

import net.minecraft.world.phys.Vec3

data class InterimCalculation(
    var fluidHeight: Double = 0.0,
    var flowVector: Vec3 = Vec3.ZERO,
    var blockCount: Int = 0
)
