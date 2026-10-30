package xyz.bluspring.kilt.loader.provider

import net.minecraftforge.forgespi.language.ILifecycleEvent
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import java.util.function.Consumer
import java.util.function.Supplier

object NoopLanguageProvider : IModLanguageProvider {
    override fun name(): String = "kilt_noop"

    override fun getFileVisitor(): Consumer<ModFileScanData> {
        return Consumer {

        }
    }

    override fun <R : ILifecycleEvent<R?>?> consumeLifecycleEvent(consumeEvent: Supplier<R?>?) {
    }
}
