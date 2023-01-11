package net.minecraftforge.event.entity.living

import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.LivingEntity
import java.util.Collections

class PotionColorCalculationEvent(entity: LivingEntity, var color: Int, private var hideParticle: Boolean, effectList: Collection<MobEffectInstance>) : LivingEvent(entity) {
    val effects: Collection<MobEffectInstance> = Collections.unmodifiableCollection(effectList)

    fun areParticlesHidden(): Boolean {
        return hideParticle
    }

    fun shouldHideParticles(hideParticle: Boolean) {
        this.hideParticle = hideParticle
    }
}