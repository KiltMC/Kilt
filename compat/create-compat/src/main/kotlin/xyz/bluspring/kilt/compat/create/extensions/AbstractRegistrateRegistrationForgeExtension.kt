package xyz.bluspring.kilt.compat.create.extensions

import net.minecraft.resources.ResourceLocation
import net.neoforged.neoforge.registries.RegisterEvent

interface AbstractRegistrateRegistrationForgeExtension<R, T : R> {
    fun register(event: RegisterEvent)
    fun getName(): ResourceLocation
}