package net.minecraftforge.fml.loading.progress

class Message(val text: String?, val type: MessageType) {
    private val timestamp: Long

    init {
        this.timestamp = System.nanoTime()
    }

    fun timestamp(): Long {
        return timestamp
    }

    val typeColour: FloatArray
        get() = type.colour()

    enum class MessageType(r: Float, g: Float, b: Float) {
        MC(1.0f, 1.0f, 1.0f),
        ML(0.0f, 0.0f, 0.5f),
        LOC(0.0f, 0.5f, 0.0f),
        MOD(0.5f, 0.0f, 0.0f);

        private val colour: FloatArray

        init {
            colour = floatArrayOf(r, g, b)
        }

        fun colour(): FloatArray {
            return colour
        }
    }
}
