package xyz.bluspring.kilt.compat.create.extensions

import com.tterrag.registrate.AbstractRegistrate
import net.minecraftforge.eventbus.api.IEventBus

interface AbstractRegistrateForgeExtension<S : AbstractRegistrate<S>> {
    fun getModEventBus(): IEventBus
    fun registerEventListeners(bus: IEventBus): S
}