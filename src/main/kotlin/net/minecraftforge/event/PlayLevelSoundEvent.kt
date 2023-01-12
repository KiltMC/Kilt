package net.minecraftforge.event

import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundSource
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import net.minecraftforge.eventbus.api.Cancelable
import net.minecraftforge.eventbus.api.Event

@Cancelable
open class PlayLevelSoundEvent(val level: Level, var sound: SoundEvent?, var source: SoundSource, volume: Float, pitch: Float) : Event() {
    val originalVolume = volume
    val originalPitch = pitch

    var newVolume = volume
    var newPitch = pitch

    class AtEntity(val entity: Entity, sound: SoundEvent?, source: SoundSource, volume: Float, pitch: Float) : PlayLevelSoundEvent(entity.level, sound, source, volume, pitch)
    class AtPosition(level: Level, val position: Vec3, sound: SoundEvent?, source: SoundSource, volume: Float, pitch: Float) : PlayLevelSoundEvent(level, sound, source, volume, pitch)
}