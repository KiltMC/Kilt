package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.Remapper
import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import org.objectweb.asm.tree.ClassNode
import java.util.jar.JarFile

// TODO: please make faster and make it work in dev
class KiltAsmRemapper(val dependencies: List<JarFile>) : Remapper() {
    private val mc = KiltRemapper.mcRemapper
    private val mappingResolver = FabricLoader.getInstance().mappingResolver
    private val from = "intermediary"

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val mcClass = mc.map(owner)
        val mcName = mc.mapFieldName(owner, name, descriptor)

        if (mcName != name)
            return mappingResolver.mapFieldName(from, mcClass.replace("/", "."), mcName, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))

        val nodes = recursiveLoadClasses(owner)
        val nodeMatch = nodes.firstOrNull { it.fields.any { f -> f.name == name && f.desc == descriptor } }

        if (nodeMatch != null) {
            return super.mapFieldName(owner, name, descriptor)
        } else {
            for (node in nodes) {
                val mcClasses = mutableListOf<String>()

                if (node.superName != null && remappedPackages.any { node.superName.startsWith(it) }) {
                    mcClasses.add(node.superName)
                }

                if (node.interfaces != null) {
                    for (iface in node.interfaces) {
                        if (remappedPackages.any { iface.startsWith(it) }) {
                            mcClasses.add(iface)
                        }
                    }
                }

                for (mcClass2 in mcClasses) {
                    val mapped = mappingResolver.mapFieldName("intermediary",
                        KiltRemapper.remapClass(mcClass2, true).replace("/", "."),
                        mc.mapFieldName(mcClass2, name, descriptor),
                        KiltRemapper.remapDescriptor(descriptor, toIntermediary = true)
                    )

                    if (mapped != name)
                        return mapped
                }
            }
        }

        return super.mapFieldName(owner, name, descriptor)
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        // i hate that this is a possibility
        if (name.startsWith("f_") && name.endsWith("_"))
            return mapFieldName(owner, name, descriptor)

        val mcClass = mc.map(owner)
        val mcName = mc.mapMethodName(owner, name, descriptor)

        if (mcName != name)
            return mappingResolver.mapMethodName(from,
                mcClass.replace("/", "."),
                mcName,
                KiltRemapper.remapDescriptor(descriptor, toIntermediary = true)
            )

        val nodes = recursiveLoadClasses(owner)

        nodes.forEach { node ->
            val nodeName = mc.mapMethodName(KiltRemapper.unmapClass(node.name).replace(".", "/"), name, descriptor)

            if (nodeName != name)
                return mappingResolver.mapMethodName(from, node.name.replace("/", "."), nodeName, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))
        }

        val nodeMatch = nodes.firstOrNull {
            it.methods.any { f -> f.name == name && f.desc == descriptor }
        }

        if (nodeMatch != null) {
            return super.mapMethodName(owner, name, descriptor)
        } else {
            for (node in nodes) {
                val mcClasses = mutableListOf<String>()

                if (node.superName != null && remappedPackages.any { node.superName.startsWith(it) }) {
                    mcClasses.add(node.superName)
                }

                if (node.interfaces != null) {
                    for (iface in node.interfaces) {
                        if (remappedPackages.any { iface.startsWith(it) }) {
                            mcClasses.add(iface)
                        }
                    }
                }

                for (mcClass2 in mcClasses) {
                    val mapped = mappingResolver.mapMethodName(from,
                        KiltRemapper.remapClass(mcClass2, true).replace("/", "."),
                        mc.mapMethodName(mcClass2.replace("/", "."), name, descriptor),
                        KiltRemapper.remapDescriptor(descriptor, toIntermediary = true)
                    )

                    if (mapped != name)
                        return mapped
                }
            }
        }

        return super.mapMethodName(owner, name, descriptor)
    }

    private fun recursiveLoadClasses(name: String): List<ClassNode> {
        // let's just speed that up.
        if (notMappedCache.contains(name))
            return emptyList()

        val classList = mutableListOf<ClassNode>()
        val node: ClassNode

        // Cache any classes that are loaded, primarily to save on load times.
        if (classLoadCache.contains(name)) {
            node = classLoadCache[name]!!
        } else {
            val bestMatchingDependency = dependencies.firstOrNull { jar ->
                jar.getJarEntry("$name.class") != null
            }

            if (bestMatchingDependency != null) {
                val classEntry = bestMatchingDependency.getJarEntry("$name.class")
                val data = ClassReader(bestMatchingDependency.getInputStream(classEntry))
                node = ClassNode(Opcodes.ASM9)

                data.accept(node, 0)

                classLoadCache[name] = node
            } else {
                val mcClassNode = KiltRemapper.getGameClassNode(KiltRemapper.remapClass(name, ignoreWorkaround = true))

                if (mcClassNode == null) {
                    notMappedCache.add(name)
                    return emptyList()
                }
                node = mcClassNode

                classLoadCache[name] = node
            }
        }

        if (node.superName != null)
            classList.addAll(recursiveLoadClasses(node.superName.replace(".", "/")))

        if (node.interfaces != null)
            node.interfaces.forEach {
                classList.addAll(recursiveLoadClasses(it.replace(".", "/")))
            }

        classList.add(node)

        return classList
    }

    override fun mapRecordComponentName(owner: String, name: String, descriptor: String): String {
        return mapFieldName(owner, name, descriptor)
    }

    override fun map(name: String): String {
        return KiltRemapper.remapClass(name, false)
    }

    override fun mapSignature(signature: String?, typeSignature: Boolean): String? {
        if (signature == null)
            return null

        return remapSignature(signature)
    }

    private fun remapSignature(signature: String): String {
        val parser = SignatureReader(signature)
        val writer = object : SignatureWriter() {
            override fun visitClassType(name: String?) {
                super.visitClassType(if (name != null) KiltRemapper.remapClass(name) else null)
            }

            override fun visitInnerClassType(name: String?) {
                super.visitInnerClassType(if (name != null) KiltRemapper.remapClass(name) else null)
            }

            override fun visitTypeVariable(name: String?) {
                super.visitTypeVariable(if (name != null) KiltRemapper.remapClass(name) else null)
            }
        }

        parser.accept(writer)

        return writer.toString()
    }

    override fun mapInnerClassName(name: String, ownerName: String?, innerName: String?): String {
        return KiltRemapper.remapClass(name)
    }

    override fun mapInvokeDynamicMethodName(name: String, descriptor: String): String {
        // this is what you call terrible performance.
        // TODO: please find a way to make this faster.
        KiltRemapper.srgIntermediaryTree.classes.forEach { c ->
            c.methods.forEach { m ->
                if (m.getName("searge") == name && m.getDescriptor("searge") == descriptor) {
                    return mappingResolver.mapMethodName("intermediary", c.getName("intermediary").replace("/", "."), m.getName("intermediary"), m.getDescriptor("intermediary"))
                }
            }
        }

        return name
    }

    companion object {
        private val remappedPackages = listOf(
            "net/minecraft/",
            "com/mojang/",
            "net/minecraftforge/"
        )
        private val classLoadCache = mutableMapOf<String, ClassNode>()
        private val notMappedCache = mutableListOf<String>()
    }
}