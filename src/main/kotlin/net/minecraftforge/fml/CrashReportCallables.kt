package net.minecraftforge.fml

import xyz.bluspring.kilt.Kilt
import java.util.function.BooleanSupplier
import java.util.function.Supplier

object CrashReportCallables {
    private val callables = mutableListOf<ISystemReportExtender>()

    @JvmStatic
    fun registerCrashCallable(callable: ISystemReportExtender) {
        callables.add(callable)
    }

    @JvmStatic
    fun registerCrashCallable(headerName: String, reportGenerator: Supplier<String>) {
        registerCrashCallable(object : ISystemReportExtender {
            override val label = headerName
            override fun isActive(): Boolean {
                return true
            }

            override fun get(): String {
                return reportGenerator.get()
            }
        })
    }

    @JvmStatic
    fun registerCrashCallable(headerName: String, reportGenerator: Supplier<String>, active: BooleanSupplier) {
        registerCrashCallable(object : ISystemReportExtender {
            override val label = headerName
            override fun isActive(): Boolean {
                try {
                    return active.asBoolean
                } catch (e: Throwable) {
                    Kilt.logger.warn("CrashCallable '$headerName' threw an exception while checking the active flag, disabling", e)
                    return false
                }
            }

            override fun get(): String {
                return reportGenerator.get()
            }
        })
    }

    @JvmStatic
    fun allCrashCallables(): List<ISystemReportExtender> {
        return callables.toList()
    }
}