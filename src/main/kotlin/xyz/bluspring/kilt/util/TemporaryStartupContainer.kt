package xyz.bluspring.kilt.util

class TemporaryStartupContainer<T>(
    private var contents: T?
) {
    private var references = 0

    fun capture(): Container {
        if (contents == null) {
            throw IllegalStateException("Already discarded")
        }
        references++
        return Container()
    }

    inner class Container {
        fun get(): T {
            return contents!!
        }

        fun release() {
            references--
            if (references <= 0) {
                contents = null
            }
        }

    }

}
