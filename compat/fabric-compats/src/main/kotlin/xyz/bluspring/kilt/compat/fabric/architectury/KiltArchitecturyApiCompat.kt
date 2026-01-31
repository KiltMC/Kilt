package xyz.bluspring.kilt.compat.fabric.architectury

import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.minecraft.world.InteractionResult
import net.neoforged.neoforge.event.EventHooks

object KiltArchitecturyApiCompat {
    fun initCommon() {
        EntityEvent.ANIMAL_TAME.register { animal, player ->
            if (EventHooks.onAnimalTame(animal, player))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        }
    }

    /*fun eventBusToArchitectury(result: Event.Result): EventResult {
        return when (result) {
            Event.Result.ALLOW -> EventResult.interruptTrue()
            Event.Result.DEFAULT -> EventResult.pass()
            Event.Result.DENY -> EventResult.interruptFalse()
            else -> EventResult.pass()
        }
    }*/

    fun vanillaToArchitectury(result: InteractionResult): EventResult {
        return when (result) {
            InteractionResult.PASS -> EventResult.pass()
            InteractionResult.FAIL -> EventResult.interruptFalse()
            InteractionResult.SUCCESS -> EventResult.interruptTrue()
            else -> EventResult.interruptDefault()
        }
    }
}