package xyz.bluspring.kilt.compat.create.extensions

import net.minecraftforge.registries.RegisterEvent

interface AbstractRegistrateRegistrationForgeExtension<R, T : R> {
    fun register(event: RegisterEvent)
    fun getName(): String
}