package net.minecraftforge.fml

import net.minecraftforge.eventbus.api.IEventBus
import net.minecraftforge.fml.config.IConfigEvent
import java.util.function.Supplier

interface IBindingsProvider {
    fun getForgeBusSupplier(): Supplier<IEventBus>
    fun getMessageParser(): Supplier<I18NParser>
    fun getConfigConfiguration(): Supplier<IConfigEvent.ConfigConfig>
}