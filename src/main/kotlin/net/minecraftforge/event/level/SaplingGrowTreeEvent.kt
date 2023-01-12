package net.minecraftforge.event.level

import net.minecraft.core.BlockPos
import net.minecraft.core.Holder
import net.minecraft.util.RandomSource
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraftforge.eventbus.api.Event.HasResult

@HasResult
class SaplingGrowTreeEvent(level: LevelAccessor, val randomSource: RandomSource, val pos: BlockPos, var feature: Holder<out ConfiguredFeature<*, *>>?) : LevelEvent(level) {
    constructor(level: LevelAccessor, randomSource: RandomSource, pos: BlockPos) : this(level, randomSource, pos, null)
}