package xyz.bluspring.kilt.loader

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import xyz.bluspring.kilt.loader.mod.ForgeMod
import java.util.*
import java.util.function.Consumer

open class KiltModContainer(internal val mod: ForgeMod) : FMLModContainer(mod) {
    init {
        configHandler = Optional.of(Consumer {
            mod.eventBus.post(it.self())
        })
    }

    override val eventBus: IEventBus
        get() = mod.eventBus
}