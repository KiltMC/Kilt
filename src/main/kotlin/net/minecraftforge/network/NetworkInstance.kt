package net.minecraftforge.network

import net.minecraft.resources.ResourceLocation
import java.util.function.Predicate
import java.util.function.Supplier

class NetworkInstance(
    val channelName: ResourceLocation,
    internal val networkProtocolVersion: Supplier<String>,
    private val clientAcceptedVersions: Predicate<String>,
    private val serverAcceptedVersions: Predicate<String>
) {
    fun addGatherListener()
}