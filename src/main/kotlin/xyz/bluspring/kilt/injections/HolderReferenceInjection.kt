package xyz.bluspring.kilt.injections

import net.minecraft.core.Holder

interface HolderReferenceInjection {
    fun getType(): Holder.Reference.Type {
        throw RuntimeException("mixin, why didn't you add this")
    }
}