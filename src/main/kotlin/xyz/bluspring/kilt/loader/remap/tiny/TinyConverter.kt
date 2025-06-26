package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.mapping.reader.v2.MappingGetter
import net.fabricmc.mapping.reader.v2.TinyMetadata
import net.fabricmc.mapping.reader.v2.TinyVisitor
import net.fabricmc.mapping.tree.ClassDef
import net.fabricmc.mapping.tree.TinyTree
import net.fabricmc.tinyremapper.TinyRemapper
import net.minecraftforge.srgutils.IMappingFile

object TinyConverter {
    private val visitorClass = Class.forName("net.fabricmc.mapping.tree.TinyMappingFactory\$Visitor")
    private val metadataField = visitorClass.getDeclaredField("metadata").apply {
        this.isAccessible = true
    }
    private val classNamesField = visitorClass.getDeclaredField("classNames").apply {
        this.isAccessible = true
    }
    private val classesField = visitorClass.getDeclaredField("classes").apply {
        this.isAccessible = true
    }

    // This isn't used, but if for whatever reason we need to convert this to a tree directly with this, we can do so.
    fun convert(mapping: IMappingFile, from: String, to: String): TinyTree {
        val mappingGetter = ForgeMappingGetter()
        val visitor = visitorClass.getConstructor(Boolean::class.java).apply {
            isAccessible = true
        }.newInstance(false) as TinyVisitor

        visitor.start(BasicTinyMetadataImpl(listOf(from, to)))

        for (clazz in mapping.classes) {
            mappingGetter.currentContext = Context.CLASS
            mappingGetter.currentClass = clazz
            visitor.pushClass(mappingGetter)

            for (field in clazz.fields) {
                mappingGetter.currentContext = Context.FIELD
                mappingGetter.currentField = field
                visitor.pushField(mappingGetter, field.descriptor)
                visitor.pop(1)
            }

            for (method in clazz.methods) {
                mappingGetter.currentContext = Context.METHOD
                mappingGetter.currentMethod = method
                visitor.pushMethod(mappingGetter, method.descriptor)
                visitor.pop(1)
            }

            visitor.pop(1)
        }

        return TinyTreeImpl(metadataField.get(visitor) as TinyMetadata, classNamesField.get(visitor) as Map<String, ClassDef>, classesField.get(visitor) as Collection<ClassDef>)
    }

    private class ForgeMappingGetter : MappingGetter {
        var currentContext = Context.CLASS
        lateinit var currentClass: IMappingFile.IClass
        lateinit var currentField: IMappingFile.IField
        lateinit var currentMethod: IMappingFile.IMethod

        override fun get(namespace: Int): String {
            return when (currentContext) {
                Context.CLASS -> if (namespace == 0) currentClass.original else currentClass.mapped
                Context.FIELD -> if (namespace == 0) currentField.original else currentField.mapped
                Context.METHOD -> if (namespace == 0) currentMethod.original else currentMethod.mapped
            }
        }

        override fun getAllNames(): Array<out String> {
            return when (currentContext) {
                Context.CLASS -> arrayOf(currentClass.original, currentClass.mapped)
                Context.FIELD -> arrayOf(currentField.original, currentField.mapped)
                Context.METHOD -> arrayOf(currentMethod.original, currentMethod.mapped)
            }
        }

        override fun getRaw(namespace: Int): String {
            return get(namespace)
        }

        override fun getRawNames(): Array<out String> {
            return allNames
        }
    }

    private enum class Context {
        CLASS, FIELD, METHOD
    }

    private val analyzeVisitorsField = TinyRemapper.Builder::class.java.getDeclaredField("analyzeVisitors").apply {
        isAccessible = true
    }

    private val stateProcessorsField = TinyRemapper.Builder::class.java.getDeclaredField("stateProcessors").apply {
        isAccessible = true
    }

    private val preApplyVisitorsField = TinyRemapper.Builder::class.java.getDeclaredField("preApplyVisitors").apply {
        isAccessible = true
    }

    val TinyRemapper.Builder.analyzeVisitors: List<TinyRemapper.AnalyzeVisitorProvider>
        get() {
            return analyzeVisitorsField.get(this) as List<TinyRemapper.AnalyzeVisitorProvider>
        }

    val TinyRemapper.Builder.stateProcessors: List<TinyRemapper.StateProcessor>
        get() {
            return stateProcessorsField.get(this) as List<TinyRemapper.StateProcessor>
        }

    val TinyRemapper.Builder.preApplyVisitors: List<TinyRemapper.ApplyVisitorProvider>
        get() {
            return preApplyVisitorsField.get(this) as List<TinyRemapper.ApplyVisitorProvider>
        }
}