package xyz.bluspring.kilt.loader

import net.minecraftforge.fml.ModContainer
import java.util.*
import java.util.function.Consumer

class KiltModContainer(mod: ForgeMod) : ModContainer(mod.forgeSpi) {
    val fabricModContainer = KiltFabricModContainer(mod)

    init {
        configHandler = Optional.of(Consumer {
            mod.eventBus.post(it.self())
        })
    }
}