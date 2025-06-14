package net.neoforged.fml.javafmlmod

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.forgespi.language.IModInfo
import net.neoforged.fml.ModContainer

open class FMLModContainer(info: IModInfo) : ModContainer(info) {
    open val eventBus: IEventBus?
        get() {
            return null
        }
}