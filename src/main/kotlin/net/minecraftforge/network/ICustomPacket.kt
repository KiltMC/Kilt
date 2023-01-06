package net.minecraftforge.network

import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.Packet
import net.minecraft.resources.ResourceLocation

interface ICustomPacket<T : Packet<*>> {
    fun getInternalData(): FriendlyByteBuf?
    fun getName(): ResourceLocation
    fun getIndex(): Int
    fun getDirection(): NetworkDirection {
        return NetworkDirection.directionFor(this.javaClass)
    }
    fun getThis(): T {
        return this as T
    }
}