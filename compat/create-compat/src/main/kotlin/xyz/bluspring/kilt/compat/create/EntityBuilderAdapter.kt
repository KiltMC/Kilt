package xyz.bluspring.kilt.compat.create

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityDimensions
import net.minecraft.world.entity.EntityType
import net.minecraft.world.flag.FeatureFlag
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraftforge.network.PlayMessages.SpawnEntity
import xyz.bluspring.kilt.compat.create.extensions.EntityBuilderExtension
import java.util.function.BiFunction


class EntityBuilderAdapter<T : Entity>(
    val builder: FabricEntityTypeBuilder<T>,
    val registrateBuilder: EntityBuilderExtension<T>
) : EntityType.Builder<T>(null, null) {

    override fun sized(width: Float, height: Float): EntityBuilderAdapter<T> {
        builder.dimensions(EntityDimensions.scalable(width, height))
        return this
    }

    override fun noSummon(): EntityBuilderAdapter<T> {
        builder.disableSummon()
        return this
    }

    override fun noSave(): EntityBuilderAdapter<T> {
        builder.disableSaving()
        return this
    }

    override fun fireImmune(): EntityBuilderAdapter<T> {
        builder.fireImmune()
        return this
    }

    override fun immuneTo(vararg blocks: Block?): EntityBuilderAdapter<T> {
        builder.specificSpawnBlocks(*blocks)
        return this
    }

    override fun canSpawnFarFromPlayer(): EntityBuilderAdapter<T> {
        builder.spawnableFarFromPlayer()
        return this
    }

    override fun clientTrackingRange(clientTrackingRange: Int): EntityBuilderAdapter<T> {
        builder.trackRangeChunks(clientTrackingRange)
        return this
    }

    override fun updateInterval(updateInterval: Int): EntityBuilderAdapter<T> {
        builder.trackedUpdateRate(updateInterval)
        return this
    }

    override fun requiredFeatures(vararg requiredFeatures: FeatureFlag?): EntityBuilderAdapter<T> {
        builder.requires(*requiredFeatures)
        return this
    }

    fun setUpdateInterval(interval: Int): EntityType.Builder<T> {
        registrateBuilder.`kilt$setUpdateIntervalSupplier` { t -> interval }
        return this
    }

    fun setTrackingRange(range: Int): EntityType.Builder<T> {
        registrateBuilder.`kilt$setTrackingRangeSupplier` { t -> range }
        return this
    }

    fun setShouldReceiveVelocityUpdates(value: Boolean): EntityType.Builder<T> {
        registrateBuilder.`kilt$setVelocityUpdateSupplier` { t -> value }
        return this
    }

    fun setCustomClientFactory(factory: BiFunction<SpawnEntity, Level, T>): EntityType.Builder<T> {
        registrateBuilder.`kilt$setCustomClientFactory`(factory)
        return this
    }

}