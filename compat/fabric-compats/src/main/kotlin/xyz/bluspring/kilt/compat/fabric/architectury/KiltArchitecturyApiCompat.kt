package xyz.bluspring.kilt.compat.fabric.architectury

import com.google.common.collect.HashMultimap
import dev.architectury.event.EventResult
import dev.architectury.event.events.common.EntityEvent
import net.minecraft.core.Registry
import net.minecraft.world.InteractionResult
import net.neoforged.fml.ModContainer
import net.neoforged.neoforge.event.EventHooks
import net.neoforged.neoforge.registries.RegisterEvent
import xyz.bluspring.kilt.api.KiltWrappedModContainerEntrypoint

object KiltArchitecturyApiCompat : KiltWrappedModContainerEntrypoint {
    fun initCommon() {
        EntityEvent.ANIMAL_TAME.register { animal, player ->
            if (EventHooks.onAnimalTame(animal, player))
                EventResult.interruptDefault()
            else
                EventResult.pass()
        }
    }

    private val waitingForRegisterEvent = HashMultimap.create<Registry<*>, Runnable>()

    override fun onLoadModContainer(container: ModContainer) {
        container.eventBus!!.addListener<RegisterEvent> { event ->
            for (runnable in this.waitingForRegisterEvent.get(event.registry)) {
                runnable.run()
            }
        }
    }

    @JvmStatic
    fun <T : Any> delayForRegisterEvent(registry: Registry<T>, onRegister: Runnable) {
        this.waitingForRegisterEvent.put(registry, onRegister)
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
