package net.minecraftforge.fml

import net.fabricmc.api.EnvType

enum class LogicalSide {
    CLIENT, SERVER;

    fun isServer(): Boolean {
        return !isClient()
    }

    fun isClient(): Boolean {
        return this == CLIENT
    }

    fun toEnvType(): EnvType {
        return EnvType.valueOf(name)
    }
}