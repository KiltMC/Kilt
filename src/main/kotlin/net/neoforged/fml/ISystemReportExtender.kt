package net.neoforged.fml

import java.util.function.Supplier

interface ISystemReportExtender : Supplier<String> {
    val label: String

    fun isActive(): Boolean
}