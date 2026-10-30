package xyz.bluspring.kilt.loader

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.*
import java.util.function.Consumer

open class KiltModContainer(internal val mod: ForgeMod) : FMLModContainer(mod, null, mod.scanData, ModuleLayer.empty()) {
    init {
        configHandler = Optional.of(Consumer {
            mod.eventBus.post(it.self())
        })
    }

    override fun getEventBus(): IEventBus {
        return mod.eventBus
    }
}
