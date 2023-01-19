package net.minecraftforge.fml.loading.progress

import com.google.common.base.Ascii
import com.google.common.base.CharMatcher
import java.util.EnumMap
import java.util.Optional
import java.util.function.Consumer

object StartupMessageManager {
    @Volatile
    private var startupMessages = EnumMap<MessageType, List<Message>>(MessageType::class.java)

    @JvmStatic
    val messages: List<org.apache.commons.lang3.tuple.Pair<Int, Message>>
        get() {
            val ts = System.nanoTime()
            return startupMessages.values
                .asSequence()
                .flatten()
                .sortedWith(Comparator.comparingLong<Message> { it.timestamp }.thenComparing(Message::text).reversed())
                .map { org.apache.commons.lang3.tuple.Pair.of(((ts - it.timestamp) / 1e6).toInt(), it) }
                .take(5).toList()
        }

    class Message(val text: String, val type: MessageType) {
        val timestamp = System.nanoTime()
        val typeColour = type.colour()
    }

    enum class MessageType(r: Float, g: Float, b: Float) {
        MC(1F, 1F, 1F),
        ML(0F, 0F, .5F),
        LOC(.5F, .5F, 0F),
        MOD(.5F, 0F, 0F);

        private val colour = arrayOf(r, g, b)

        fun colour(): Array<Float> {
            return colour
        }
    }

    @JvmStatic
    @Synchronized
    private fun addMessage(type: MessageType, message: String, maxSize: Int) {
        val newMessages = EnumMap(startupMessages)
        newMessages.compute(type) { _, existing ->
            val newList = mutableListOf<Message>()

            if (existing != null)
                if (maxSize < 0)
                    newList.addAll(existing)
                else
                    newList.addAll(existing.subList(0, existing.size.coerceAtMost(maxSize)))

            newList.add(Message(message, type))
            return@compute newList
        }

        startupMessages = newMessages
    }

    @JvmStatic
    fun addModMessage(message: String) {
        val safeMessage = Ascii.truncate(CharMatcher.ascii().retainFrom(message), 80, "~")
        addMessage(MessageType.MOD, safeMessage, 20)
    }

    @JvmStatic
    fun modLoaderConsumer(): Optional<Consumer<String>> {
        return Optional.of(
            Consumer {
                addMessage(MessageType.ML, it, -1)
            }
        )
    }

    @JvmStatic
    fun locatorConsumer(): Optional<Consumer<String>> {
        return Optional.of(
            Consumer {
                addMessage(MessageType.LOC, it, -1)
            }
        )
    }

    @JvmStatic
    fun mcLoaderConsumer(): Optional<Consumer<String>> {
        return Optional.of(
            Consumer {
                addMessage(MessageType.MC, it, -1)
            }
        )
    }
}