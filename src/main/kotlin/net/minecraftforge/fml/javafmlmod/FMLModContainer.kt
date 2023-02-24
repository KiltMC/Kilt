package net.minecraftforge.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.ModContainer
import net.minecraftforge.forgespi.language.IModInfo

open class FMLModContainer(info: IModInfo) : ModContainer(info) {
    open val eventBus: IEventBus?
        get() {
            return null
        }
}