package xyz.bluspring.kilt.loader.remap

import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.EnhancedRemapper
import net.minecraftforge.srgutils.IMappingFile
import java.util.function.Consumer
import java.util.function.Supplier

class KiltEnhancedRemapper(private val provider: ClassProvider, private val file: IMappingFile, log: Consumer<String>, private val devClassProvider: Supplier<ClassProvider>) : EnhancedRemapper(provider, file, log) {
    fun mapMethodNamePrefixDesc(
        owner: String,
        name: String,
        descPrefix: String
    ): String? {
        val cls = file.classes.firstOrNull { it.original == owner } ?: return this.mapMethodName(owner, name, descPrefix)
        for (method in cls.methods) {
            if (method.original == name && method.descriptor.startsWith(descPrefix)) {
                return method.mapped
            }
        }

        return this.mapMethodName(owner, name, descPrefix)
    }

    private val mappingResolver = FabricLoader.getInstance().mappingResolver
    private val shouldTryRemap = mappingResolver.currentRuntimeNamespace != "intermediary"
    private lateinit var devRemapper: EnhancedRemapper

    fun initDevRemapper() {
        if (!::devRemapper.isInitialized) {
            devRemapper = EnhancedRemapper(devClassProvider.get(), KiltRemapper.fabricMappings.getMap("intermediary", "named")) {}
        }
    }

    override fun map(name: String): String {
        val intermediary = super.map(name)

        if (shouldTryRemap) {
            initDevRemapper()
            return devRemapper.map(intermediary)
        }

        return intermediary
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        val intermediary = super.mapFieldName(owner, name, descriptor)

        if (shouldTryRemap && (intermediary.startsWith("field_") || intermediary.startsWith("comp_"))) {
            initDevRemapper()
            for (info in getClassHierarchy(name)) {
                val mapped = devRemapper.mapFieldName(KiltRemapper.remapClass(info.name, toIntermediary = true, ignoreWorkaround = true), intermediary, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))

                if (mapped != intermediary)
                    return mapped
            }
        }

        return intermediary
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        val intermediary = super.mapMethodName(owner, name, descriptor)

        if (shouldTryRemap && (intermediary.startsWith("method_") || intermediary.startsWith("comp_"))) {
            initDevRemapper()

            for (info in getClassHierarchy(name)) {
                val mapped = devRemapper.mapMethodName(KiltRemapper.remapClass(info.name, toIntermediary = true, ignoreWorkaround = true), intermediary, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))

                if (mapped != intermediary)
                    return mapped
            }
        }

        return intermediary
    }

    private fun getClassHierarchy(name: String): List<ClassProvider.IClassInfo> {
        val hierarchy = mutableListOf<ClassProvider.IClassInfo>()

        var currentClass = provider.getClass(name).orElse(null) ?: return emptyList()
        hierarchy.add(currentClass)

        do {
            if (currentClass.`super` == null) {
                break
            }

            currentClass = provider.getClass(currentClass.`super`).orElse(null)

            if (currentClass.name.startsWith("java/lang/") || currentClass.name.startsWith("com/google/")) {
                break
            }

            hierarchy.add(currentClass ?: break)
        } while (true)

        return hierarchy
    }
}