package xyz.bluspring.kilt.loader

import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.javafmlmod.FMLModContainer
import xyz.bluspring.kilt.loader.mod.NeoForgeMod

open class KiltModContainer(internal val mod: NeoForgeMod) : FMLModContainer(mod, emptyList(), mod.scanData, ModuleLayer.empty()) {
    override fun getEventBus(): IEventBus {
        return mod.eventBus
    }
}
