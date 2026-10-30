package cpw.mods.modlauncher.util

import java.util.Objects
import java.util.ServiceConfigurationError
import java.util.ServiceLoader
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.stream.Stream

object ServiceLoaderUtils {
    @JvmStatic
    fun <T> streamServiceLoader(slSupplier: Supplier<ServiceLoader<T>>, errorConsumer: Consumer<ServiceConfigurationError>): Stream<T> {
        return streamWithErrorHandling(slSupplier.get(), errorConsumer)
    }

    @JvmStatic
    fun <T> streamWithErrorHandling(sl: ServiceLoader<T>, errorConsumer: Consumer<ServiceConfigurationError>): Stream<T> {
        return sl.stream().map {
            try {
                it.get()
            } catch (e: ServiceConfigurationError) {
                errorConsumer.accept(e)
                null
            }
        }
            .filter { it != null }
            .map { it }
    }

    @JvmStatic
    fun fileNameFor(clazz: Class<*>): String {
        return clazz.protectionDomain.codeSource.location.file
    }
}
