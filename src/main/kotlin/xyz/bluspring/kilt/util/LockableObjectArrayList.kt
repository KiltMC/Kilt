package xyz.bluspring.kilt.util

import it.unimi.dsi.fastutil.objects.ObjectArrayList

class LockableObjectArrayList<K>() : ObjectArrayList<K>() {
    private var isFrozen = false

    fun freeze() {
        isFrozen = true
    }

    private fun checkFrozen() {
        if (isFrozen)
            throw IllegalStateException("List is already frozen!")
    }

    override fun clear() {
        checkFrozen()
        super.clear()
    }

    override fun size(size: Int) {
        checkFrozen()
        super.size(size)
    }

    override fun trim(n: Int) {
        checkFrozen()
        super.trim(n)
    }

    override fun set(index: Int, k: K): K {
        checkFrozen()
        return super.set(index, k)
    }

    override fun remove(k: K): Boolean {
        checkFrozen()
        return super.remove(k)
    }

    override fun add(index: Int, k: K) {
        checkFrozen()
        super.add(index, k)
    }

    override fun add(k: K): Boolean {
        checkFrozen()
        return super.add(k)
    }
}