package net.minecraftforge.fml

import java.util.Spliterator
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.function.Supplier
import java.util.stream.Stream
import java.util.stream.StreamSupport

object InterModComms {
    @JvmRecord
    data class IMCMessage(val senderModId: String, val modId: String, val method: String, val messageSupplier: Supplier<*>)

    private val containerQueues = ConcurrentHashMap<String, ConcurrentLinkedQueue<IMCMessage>>()
    @JvmStatic
    fun sendTo(modId: String, method: String, thing: Supplier<*>): Boolean {
        if (!ModList.get().isLoaded(modId))
            return false

        containerQueues.computeIfAbsent(modId) {
            ConcurrentLinkedQueue()
        }.add(IMCMessage(
            ModLoadingContext.activeContainer.modId,
            modId, method, thing
        ))

        return true
    }

    @JvmStatic
    fun sendTo(senderModId: String, modId: String, method: String, thing: Supplier<*>): Boolean {
        if (!ModList.get().isLoaded(modId))
            return false

        containerQueues.computeIfAbsent(modId) {
            ConcurrentLinkedQueue()
        }.add(IMCMessage(senderModId, modId, method, thing))

        return true
    }

    @JvmStatic
    fun getMessages(modId: String, methodMatcher: Predicate<String>): Stream<IMCMessage> {
        val queue = containerQueues[modId] ?: return Stream.empty()

        return StreamSupport.stream(QueueFilteringSpliterator(queue, methodMatcher), false)
    }

    @JvmStatic
    fun getMessages(modId: String): Stream<IMCMessage> {
        return getMessages(modId) {
            true // why does Forge have it set to Boolean.TRUE?
        }
    }

    private class QueueFilteringSpliterator(private val queue: ConcurrentLinkedQueue<IMCMessage>, private val methodFilter: Predicate<String>) : Spliterator<IMCMessage> {
        private val iterator = queue.iterator()

        override fun tryAdvance(p0: Consumer<in IMCMessage>): Boolean {
            var next: IMCMessage
            do {
                if (!iterator.hasNext())
                    return false

                next = iterator.next()
            } while (!methodFilter.test(next.method))

            p0.accept(next)
            iterator.remove()
            return true
        }

        override fun trySplit(): Spliterator<IMCMessage>? {
            return null
        }

        override fun estimateSize(): Long {
            return queue.size.toLong()
        }

        override fun characteristics(): Int {
            return Spliterator.CONCURRENT or Spliterator.NONNULL or Spliterator.ORDERED
        }
    }
}