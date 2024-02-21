package net.minecraftforge.fml

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
    fun allCrashCallables(): List<ISystemReportExtender> {
        return callables.toList()
    }
}