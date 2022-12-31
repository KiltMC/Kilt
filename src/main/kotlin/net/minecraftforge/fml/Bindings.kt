package net.minecraftforge.fml

import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.eventbus.api.IEventBus
import java.util.function.Supplier

object Bindings {
    @JvmStatic
    fun getForgeBus(): Supplier<IEventBus> {
        return Supplier {
            MinecraftForge.EVENT_BUS
        }
    }

    @JvmStatic
    fun getMessageParser(): Supplier<I18NParser> {
        return Supplier {
            object : I18NParser {
                override fun parseMessage(i18nMessage: String, vararg args: Any): String {
                    TODO("Not yet implemented")
                }

                override fun stripControlCodes(toStrip: String): String {
                    TODO("Not yet implemented")
                }
            }
        }
    }

    @JvmStatic
    fun getConfigConfiguration(): Supplier<Any> {
        return Supplier {  }
    }
}