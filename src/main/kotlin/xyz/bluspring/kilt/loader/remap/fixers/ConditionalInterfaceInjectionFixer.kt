package xyz.bluspring.kilt.loader.remap.fixers

import org.objectweb.asm.signature.SignatureReader
import org.objectweb.asm.signature.SignatureWriter
import org.objectweb.asm.tree.ClassNode
import xyz.bluspring.kilt.loader.remap.KiltRemapper

/**
 * Implements certain Porting Lib interfaces directly onto NeoForge mod classes that implement methods such as getLightEmission.
 */
object ConditionalInterfaceInjectionFixer : GenericClassFixer {
    private val forgeMethodLookup = mapOf(
        ("getLightEmission" to KiltRemapper.remapDescriptor("(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)I")) to "io/github/fabricators_of_create/porting_lib/blocks/extensions/LightEmissiveBlock"
    )

    override fun fixClass(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (forgeMethodLookup.contains(method.name to method.desc)) {
                val className = forgeMethodLookup[method.name to method.desc]!!
                classNode.interfaces?.add(className)
                if (classNode.signature != null) {
                    val reader = SignatureReader(classNode.signature)
                    val writer = SignatureWriter()
                    reader.accept(writer)
                    writer.visitClassType(className)
                    classNode.signature = writer.toString()
                }
            }
        }
    }
}
