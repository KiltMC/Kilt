package xyz.bluspring.kilt.loader

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.javafmlmod.FMLModContainer
import xyz.bluspring.kilt.loader.mod.NeoForgeMod
import java.util.*
import java.util.function.Consumer

open class KiltModContainer(internal val mod: NeoForgeMod) : FMLModContainer(mod) {
    init {
        configHandler = Optional.of(Consumer {
            mod.eventBus.post(it.self())
        })
    }

    override val eventBus: IEventBus
        get() = mod.eventBus
}