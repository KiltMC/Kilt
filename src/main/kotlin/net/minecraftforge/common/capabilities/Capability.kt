package net.minecraftforge.common.capabilities

import net.minecraftforge.common.util.LazyOptional
import java.util.function.Consumer

open class Capability<T> internal constructor(val name: String) {
    internal var listeners: MutableList<Consumer<Capability<T>>>? = mutableListOf()

    fun isRegistered(): Boolean {
        return listeners == null
    }

    fun <R> orEmpty(toCheck: Capability<R>, inst: LazyOptional<T>): LazyOptional<R> {
        return if (this == toCheck)
            inst.cast()
        else
            LazyOptional.empty()
    }

    @Synchronized
    fun addListener(listener: Consumer<Capability<T>>): Capability<T> {
        if (isRegistered())
            listener.accept(this)
        else
            listeners!!.add(listener)

        return this
    }

    internal fun onRegister() {
        val listeners = this.listeners!!
        this.listeners = null
        listeners.forEach {
            it.accept(this)
        }
    }
}