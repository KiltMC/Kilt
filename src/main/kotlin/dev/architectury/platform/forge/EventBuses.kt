package dev.architectury.platform.forge

import com.google.common.collect.LinkedListMultimap
import com.google.common.collect.Multimaps
import net.minecraftforge.eventbus.api.IEventBus
import java.util.*
import java.util.function.Consumer

// Reimplemented from https://github.com/architectury/architectury-api/blob/1.20/forge/src/main/java/dev/architectury/platform/forge/EventBuses.java,
// because OpenComputers II: Reimagined uses this for some reason.
object EventBuses {
    private val EVENT_BUS_MAP = Collections.synchronizedMap(mutableMapOf<String, IEventBus>())
    private val ON_REGISTERED = Multimaps.synchronizedMultimap(LinkedListMultimap.create<String, Consumer<IEventBus>>())

    @JvmStatic
    fun registerModEventBus(modId: String, bus: IEventBus) {
        if (EVENT_BUS_MAP.putIfAbsent(modId, bus) != null)
            throw IllegalStateException("Can't register event bus for mod $modId because it was previously registered!")

        for (consumer in ON_REGISTERED.get(modId)) {
            consumer.accept(bus)
        }
    }

    @JvmStatic
    fun onRegistered(modId: String, busConsumer: Consumer<IEventBus>) {
        if (EVENT_BUS_MAP.containsKey(modId)) {
            busConsumer.accept(EVENT_BUS_MAP[modId]!!)
        } else {
            ON_REGISTERED.put(modId, busConsumer)
        }
    }

    @JvmStatic
    fun getModEventBus(modId: String): Optional<IEventBus> {
        return Optional.ofNullable(EVENT_BUS_MAP[modId])
    }
}