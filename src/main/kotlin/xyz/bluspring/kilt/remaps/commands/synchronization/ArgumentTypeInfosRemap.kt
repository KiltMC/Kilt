package xyz.bluspring.kilt.remaps.commands.synchronization

import com.mojang.brigadier.arguments.ArgumentType
import net.minecraft.commands.synchronization.ArgumentTypeInfo

import xyz.bluspring.kilt.mixin.ArgumentTypeInfosAccessor

object ArgumentTypeInfosRemap {
    @JvmStatic
    @Synchronized
    fun <A : ArgumentType<*>, T : ArgumentTypeInfo.Template<A>, I : ArgumentTypeInfo<A, T>> registerByClass(infoClass: Class<A>, argumentTypeInfo: I): I {
        ArgumentTypeInfosAccessor.getByClass()[infoClass] = argumentTypeInfo;

        return argumentTypeInfo;
    }
}