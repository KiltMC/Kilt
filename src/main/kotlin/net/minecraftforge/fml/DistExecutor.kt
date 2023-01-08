package net.minecraftforge.fml

import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.api.distmarker.Dist
import org.apache.logging.log4j.LogManager
import xyz.bluspring.kilt.util.DistUtil
import java.io.Serializable
import java.lang.invoke.SerializedLambda
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.util.*
import java.util.concurrent.Callable
import java.util.function.Supplier

object DistExecutor {
    private val logger = LogManager.getLogger()

    @JvmStatic
    @Deprecated("",
        ReplaceWith("unsafeCallWhenOn(dist, toRun)", "net.minecraftforge.fml.DistExecutor.unsafeCallWhenOn")
    )
    fun <T> callWhenOn(dist: Dist, toRun: Supplier<Callable<T>>): T? {
        return unsafeCallWhenOn(dist, toRun)
    }

    @JvmStatic
    fun <T> unsafeCallWhenOn(dist: Dist, toRun: Supplier<Callable<T>>): T? {
        if (DistUtil.isDistEqual(dist, FabricLoader.getInstance().environmentType)) {
            try {
                return toRun.get().call()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        return null
    }

    @JvmStatic
    fun <T> safeCallWhenOn(dist: Dist, toRun: Supplier<SafeCallable<T>>): T? {
        validateSafeReferent(toRun)
        return callWhenOn(dist, toRun::get)
    }

    @Deprecated("", ReplaceWith("unsafeRunWhenOn(dist, toRun)"))
    @JvmStatic
    fun runWhenOn(dist: Dist, toRun: Supplier<Runnable>) {
        unsafeRunWhenOn(dist, toRun)
    }

    @JvmStatic
    fun unsafeRunWhenOn(dist: Dist, toRun: Supplier<Runnable>) {
        if (DistUtil.isDistEqual(dist, FabricLoader.getInstance().environmentType)) {
            toRun.get().run()
        }
    }

    @JvmStatic
    fun safeRunWhenOn(dist: Dist, toRun: Supplier<SafeRunnable>) {
        validateSafeReferent(toRun)
        if (DistUtil.isDistEqual(dist, FabricLoader.getInstance().environmentType)) {
            toRun.get().run()
        }
    }

    @Deprecated("", ReplaceWith(
        "unsafeRunForDist(clientTarget, serverTarget)",
        "net.minecraftforge.fml.DistExecutor.unsafeRunForDist"
    )
    )
    @JvmStatic
    // forge what the fuck
    fun <T> runForDist(clientTarget: Supplier<Supplier<T>>, serverTarget: Supplier<Supplier<T>>): T {
        return unsafeRunForDist(clientTarget, serverTarget)
    }

    @JvmStatic
    fun <T> unsafeRunForDist(clientTarget: Supplier<Supplier<T>>, serverTarget: Supplier<Supplier<T>>): T {
        return when (FabricLoader.getInstance().environmentType) {
            EnvType.CLIENT -> clientTarget.get().get()
            EnvType.SERVER -> serverTarget.get().get()
            else -> throw IllegalArgumentException("Unsided?")
        }
    }

    @JvmStatic
    fun <T> safeRunForDist(clientTarget: Supplier<SafeSupplier<T>>, serverTarget: Supplier<SafeSupplier<T>>): T {
        validateSafeReferent(clientTarget)
        validateSafeReferent(serverTarget)
        return when (FabricLoader.getInstance().environmentType) {
            EnvType.CLIENT -> clientTarget.get().get()
            EnvType.SERVER -> serverTarget.get().get()
            else -> throw IllegalArgumentException("Unsided?")
        }
    }

    interface SafeReferent
    interface SafeCallable<T> : SafeReferent, Callable<T>, Serializable
    interface SafeSupplier<T> : SafeReferent, Supplier<T>, Serializable
    interface SafeRunnable : SafeReferent, Runnable, Serializable

    private fun validateSafeReferent(supplier: Supplier<out SafeReferent>) {
        if (!FabricLoader.getInstance().isDevelopmentEnvironment)
            return

        val setter = try {
            supplier.get()
        } catch (e: Exception) {
            return
        }

        run {
            var cl: Class<*>? = setter.javaClass
            while (cl != null) {
                try {
                    val m: Method = cl.getDeclaredMethod("writeReplace")
                    m.isAccessible = true
                    val replacement: Any = m.invoke(setter) as? SerializedLambda ?: break
                    // custom interface implementation
                    val l: SerializedLambda = replacement as SerializedLambda
                    if (Objects.equals(l.getCapturingClass(), l.getImplClass())) {
                        logger.fatal(
                            "Detected unsafe referent usage, please view the code at {}",
                            Thread.currentThread().stackTrace[3]
                        )
                        throw RuntimeException("Unsafe Referent usage found in safe referent method")
                    }
                } catch (_: NoSuchMethodException) {
                } catch (e: IllegalAccessException) {
                    break
                } catch (e: InvocationTargetException) {
                    break
                }
                cl = cl.superclass
            }
        }
    }
}