package xyz.bluspring.kilt.loader.remap.fixers

import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode

//import xyz.bluspring.kilt.workarounds.GameTestWorkaround

object AnnotationWorkaroundFixer {
    val mappingResolver = FabricLoader.getInstance().mappingResolver
    //val GAME_TEST_WORKAROUND = GameTestWorkaround::class.java.typeName
    //val GAME_TEST = mappingResolver.mapClassName("intermediary", "net.minecraft.class_6302")

    fun fixClass(classNode: ClassNode) {
        for (method in classNode.methods) {
            if (method.visibleAnnotations == null)
                continue

            val annotationsToRemove = mutableListOf<AnnotationNode>()
            val annotationsToAdd = mutableListOf<AnnotationNode>()

            for (annotation in method.visibleAnnotations) {
                /*if (annotation.desc == "L${GAME_TEST_WORKAROUND.replace(".", "/")};") {
                    annotationsToRemove.add(annotation)
                    annotationsToAdd.add(AnnotationNode(Opcodes.ASM9, "L${GAME_TEST.replace(".", "/")};").apply {
                        this.values = annotation.values
                    })
                }*/
            }

            if (annotationsToRemove.isNotEmpty() || annotationsToAdd.isNotEmpty()) {
                method.visibleAnnotations.removeAll(annotationsToRemove)
                method.visibleAnnotations.addAll(annotationsToAdd)
            }
        }
    }
}