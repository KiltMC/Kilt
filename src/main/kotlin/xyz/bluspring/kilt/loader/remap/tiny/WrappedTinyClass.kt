package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.tinyremapper.api.*
import net.minecraftforge.fart.api.ClassProvider
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import java.util.*
import java.util.function.Predicate

class WrappedTinyClass(private val environment: MixinTinyEnvironment, private val cls: ClassProvider.IClassInfo, private val classProvider: ClassProvider) : TrClass {
    override fun getEnvironment(): TrEnvironment {
        return environment
    }

    override fun getName(): String? {
        return cls.name
    }

    override fun getSuperName(): String? {
        return cls.`super`
    }

    override fun getInterfaceNames(): List<String>? {
        return cls.interfaces?.toList()
    }

    override fun getSignature(): String? {
        return null
    }

    override fun getAccess(): Int {
        return cls.access
    }

    override fun getSuperClass(): TrClass? {
        val cls = classProvider.getClass(cls.`super`).orElse(null) ?: return null
        return WrappedTinyClass(environment, cls, classProvider)
    }

    override fun getInterfaces(): List<TrClass> {
        return cls.interfaces.mapNotNull {
            val cls = classProvider.getClass(it).orElse(null) ?: return@mapNotNull null
            WrappedTinyClass(environment, cls, classProvider)
        }
    }

    override fun getParents(): Collection<TrClass?>? {
        return emptyList()
    }

    override fun getChildren(): Collection<TrClass?>? {
        return emptyList()
    }

    override fun getField(name: String?, desc: String?): TrField? {
        val field = cls.getField(name).orElse(null) ?: return null
        return WrappedTinyField(this, field)
    }

    override fun getMethod(name: String?, desc: String?): TrMethod? {
        val method = cls.getMethod(name, desc).orElse(null) ?: return null
        return WrappedTinyMethod(this, method)
    }

    override fun getFields(): Collection<TrField> {
        return cls.fields.map { WrappedTinyField(this, it) }
    }

    override fun getMethods(): Collection<TrMethod> {
        return cls.methods.map { WrappedTinyMethod(this, it) }
    }

    override fun getMembers(): Collection<TrMember> {
        return listOf(*fields.toTypedArray(), *methods.toTypedArray())
    }

    override fun getFields(
        name: String,
        desc: String,
        isDescPrefix: Boolean,
        filter: Predicate<TrField>?,
        out: MutableCollection<TrField>?
    ): Collection<TrField> {
        val out = out ?: mutableListOf()

        out.addAll(cls.fields.filter {
            it.name == name && (if (isDescPrefix) it.descriptor.startsWith(desc) else it.descriptor == desc)
        }
            .map {
                WrappedTinyField(this, it)
            }
            .filter { filter?.test(it) ?: true }
        )

        return out
    }

    override fun getMethods(
        name: String,
        desc: String?,
        isDescPrefix: Boolean,
        filter: Predicate<TrMethod>?,
        out: MutableCollection<TrMethod>?
    ): Collection<TrMethod> {
        val out = out ?: mutableListOf()

        out.addAll(cls.methods.filter {
            it.name == name && if (desc != null) (if (isDescPrefix) it.descriptor.startsWith(desc) else it.descriptor == desc) else true
        }
            .map {
                WrappedTinyMethod(this, it)
            }
            .filter { filter?.test(it) ?: true }
        )

        return out
    }

    override fun resolveField(name: String, desc: String?): TrField? {
        val field = cls.getField(name).orElse(null) ?: return null
        return WrappedTinyField(this, field)
    }

    override fun resolveMethod(name: String?, desc: String?): TrMethod? {
        val method = cls.getMethod(name, desc).orElse(null) ?: return null
        return WrappedTinyMethod(this, method)
    }

    override fun resolveFields(
        name: String,
        desc: String,
        isDescPrefix: Boolean,
        filter: Predicate<TrField>?,
        out: MutableCollection<TrField>?
    ): Collection<TrField> {
        return getFields(name, desc, isDescPrefix, filter, out)
    }

    override fun resolveMethods(
        name: String,
        desc: String,
        isDescPrefix: Boolean,
        filter: Predicate<TrMethod>?,
        out: MutableCollection<TrMethod>?
    ): Collection<TrMethod> {
        return getMethods(name, desc, isDescPrefix, filter, out)
    }

    override fun isAssignableFrom(cls: TrClass): Boolean {
        if (cls == this)
            return true

        var cls: TrClass? = cls

        if (isInterface) {
            val visited: MutableSet<TrClass> = Collections.newSetFromMap(IdentityHashMap())
            val queue = ArrayDeque<TrClass>()
            visited.add(cls!!)

            do {
                for (parent in cls!!.parents) {
                    if (parent == this)
                        return true

                    if (visited.add(parent))
                        queue.addLast(parent)
                }
            } while (queue.pollFirst().apply { cls = this } != null)
        } else {
            do {
                var superCls: TrClass? = null

                for (parent in cls!!.parents) {
                    if (!parent.isInterface) {
                        if (parent == this)
                            return true

                        superCls = parent
                        break
                    }
                }

                cls = superCls
            } while (cls != null)
        }

        return false
    }

    override fun accept(cv: ClassVisitor?, readerFlags: Int) {
        return ClassReader(this.classProvider.getClassStream(this.name) ?: throw IllegalStateException("Data unavailable!"))
            .accept(cv, readerFlags)
    }

    override fun isInput(): Boolean {
        return false
    }
}