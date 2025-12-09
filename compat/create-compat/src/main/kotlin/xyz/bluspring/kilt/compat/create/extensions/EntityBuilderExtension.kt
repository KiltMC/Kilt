package xyz.bluspring.kilt.compat.create.extensions

import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.Level
import net.minecraftforge.network.PlayMessages
import java.util.function.BiFunction
import java.util.function.Predicate
import java.util.function.ToIntFunction

interface EntityBuilderExtension<T> {

    fun `kilt$setCustomClientFactory`(customClientFactory: BiFunction<PlayMessages.SpawnEntity, Level, T>)
    fun `kilt$setVelocityUpdateSupplier`(velocityUpdateSupplier : Predicate<EntityType<out Entity>>)
    fun `kilt$setTrackingRangeSupplier`(trackingRangeSupplier : ToIntFunction<EntityType<out Entity>>)
    fun `kilt$setUpdateIntervalSupplier`(updateIntervalSupplier : ToIntFunction<EntityType<out Entity>>)

}