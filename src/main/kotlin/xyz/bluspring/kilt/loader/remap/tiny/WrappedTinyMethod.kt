package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.tinyremapper.api.TrClass
import net.fabricmc.tinyremapper.api.TrMember
import net.fabricmc.tinyremapper.api.TrMethod
import net.minecraftforge.fart.api.ClassProvider

class WrappedTinyMethod(private val owner: WrappedTinyClass, private val wrapped: ClassProvider.IMethodInfo) : TrMethod {
    override fun getType(): TrMember.MemberType? {
        return TrMember.MemberType.METHOD
    }

    override fun getOwner(): TrClass? {
        return owner
    }

    override fun getName(): String? {
        return wrapped.name
    }

    override fun getNewName(): String? {
        return (owner.environment as MixinTinyEnvironment).mapping.getClass(owner.name).getMethod(wrapped.name, wrapped.descriptor)?.mapped
    }

    override fun getDesc(): String? {
        return wrapped.descriptor
    }

    override fun getAccess(): Int {
        return wrapped.access
    }

    override fun getIndex(): Int {
        return -1
    }
}