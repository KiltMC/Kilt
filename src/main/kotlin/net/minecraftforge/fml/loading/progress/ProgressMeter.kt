package net.minecraftforge.fml.loading.progress

import java.util.concurrent.atomic.AtomicInteger


class ProgressMeter(private val name: String?, private val steps: Int, current: Int, label: Message?) {
    private val current: AtomicInteger
    private var label: Message?

    init {
        this.current = AtomicInteger(current)
        this.label = label
    }

    fun name(): String? {
        return name
    }

    fun steps(): Int {
        return steps
    }

    fun current(): Int {
        return current.get()
    }

    fun label(): Message? {
        return label
    }

    fun increment() {
        this.current.incrementAndGet()
    }

    fun complete() {
        //StartupNotificationManager.popBar(this)
    }

    fun progress(): Float {
        return current.get() / steps.toFloat()
    }

    fun setAbsolute(absolute: Int) {
        this.current.set(absolute)
    }

    fun label(message: String?) {
        this.label = Message(message, Message.MessageType.ML)
    }
}
