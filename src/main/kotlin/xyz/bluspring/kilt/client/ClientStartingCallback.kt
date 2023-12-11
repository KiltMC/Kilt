package xyz.bluspring.kilt.client

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.client.Minecraft

/**
 * Event for after Minecraft does its <init>
 */
fun interface ClientStartingCallback {
    fun onClientStarting(mc: Minecraft)

    companion object {
        @JvmField
        val EVENT: Event<ClientStartingCallback> = EventFactory.createArrayBacked(
            ClientStartingCallback::class.java
        ) { callbacks ->
            ClientStartingCallback { client: Minecraft ->
                for (callback in callbacks) {
                    callback.onClientStarting(client)
                }
            }
        };
    }
}