package xyz.bluspring.kilt.remaps.world.level

import net.minecraft.core.BlockPos
import net.minecraft.tags.FluidTags
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.SpawnPlacements
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.NaturalSpawner
import net.minecraft.world.level.block.state.BlockState
import net.minecraftforge.common.extensions.IForgeBlockState

interface NaturalSpawnerRemap {
    companion object {
        @JvmStatic
        fun canSpawnAtBody(
            type: SpawnPlacements.Type,
            levelReader: LevelReader,
            blockPos: BlockPos,
            entityType: EntityType<*>?
        ): Boolean {
            return if (type == SpawnPlacements.Type.NO_RESTRICTIONS) {
                true
            } else if (entityType != null && levelReader.worldBorder.isWithinBounds(blockPos)) {
                val blockState = levelReader.getBlockState(blockPos)
                val fluidState = levelReader.getFluidState(blockPos)
                val blockPos2 = blockPos.above()
                val blockPos3 = blockPos.below()
                when (type) {
                    SpawnPlacements.Type.IN_WATER -> fluidState.`is`(FluidTags.WATER) && !levelReader.getBlockState(
                        blockPos2
                    ).isRedstoneConductor(levelReader, blockPos2)
                    SpawnPlacements.Type.IN_LAVA -> fluidState.`is`(FluidTags.LAVA)
                    else -> {
                        val blockState2: BlockState = levelReader.getBlockState(blockPos3)
                        if (!(blockState2 as IForgeBlockState).isValidSpawn(levelReader, blockPos3, type, entityType)) {
                            false
                        } else {
                            NaturalSpawner.isValidEmptySpawnBlock(
                                levelReader, blockPos, blockState, fluidState, entityType
                            ) && NaturalSpawner.isValidEmptySpawnBlock(
                                levelReader,
                                blockPos2,
                                levelReader.getBlockState(blockPos2),
                                levelReader.getFluidState(blockPos2),
                                entityType
                            )
                        }
                    }
                }
            } else {
                false
            }
        }
    }
}