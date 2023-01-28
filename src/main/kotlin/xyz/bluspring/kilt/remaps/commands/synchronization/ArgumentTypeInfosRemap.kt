package xyz.bluspring.kilt.remaps.commands.synchronization

import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.synchronization.ArgumentTypeInfo
import net.minecraft.commands.synchronization.ArgumentTypeInfos

import xyz.bluspring.kilt.mixin.ArgumentTypeInfosAccessor

object ArgumentTypeInfosRemap : ArgumentTypeInfos() {
    @JvmStatic
    @Synchronized
    fun <A : ArgumentType<*>, T : ArgumentTypeInfo.Template<A>, I : ArgumentTypeInfo<A, T>> registerByClass(infoClass: Class<A>, argumentTypeInfo: I): I {
        BY_CLASS[infoClass] = argumentTypeInfo;

        return argumentTypeInfo;
    }
}