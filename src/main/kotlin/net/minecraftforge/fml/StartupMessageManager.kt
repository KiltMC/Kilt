package net.minecraftforge.fml

import net.minecraftforge.fml.loading.progress.ProgressMeter
import java.util.*
import java.util.function.Consumer
import net.minecraftforge.fml.loading.progress.StartupMessageManager as WhyAreThereTwo

object StartupMessageManager {
    @JvmStatic
    fun addModMessage(message: String) {
        WhyAreThereTwo.addModMessage(message)
    }

    @JvmStatic
    fun modLoaderConsumer(): Optional<Consumer<String>> {
        return WhyAreThereTwo.modLoaderConsumer()
    }

    @JvmStatic
    fun mcLoaderConsumer(): Optional<Consumer<String>> {
        return WhyAreThereTwo.mcLoaderConsumer()
    }

    @JvmStatic
    fun addProgressBar(barName: String, count: Int): ProgressMeter {
        return WhyAreThereTwo.addProgressBar(barName, count)
    }
}