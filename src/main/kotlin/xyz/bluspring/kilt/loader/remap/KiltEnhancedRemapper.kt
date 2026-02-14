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
        val hierarchy = getClassHierarchy(owner)

        for (info in hierarchy) {
            for (methodInfo in info.methods) {
                if (methodInfo.name == name && methodInfo.descriptor.startsWith(descPrefix)) {
                    return this.mapMethodName(info.name, methodInfo.name, methodInfo.descriptor)
                }
            }
        }

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
        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap) {
            return name
        }

        val intermediary = super.map(name)

        if (shouldTryRemap) {
            initDevRemapper()
            return devRemapper.map(intermediary)
        }

        return intermediary
    }

    override fun mapFieldName(owner: String, name: String, descriptor: String): String {
        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap) {
            return name
        }

        val intermediary = super.mapFieldName(owner, name, descriptor)

        if (shouldTryRemap && (intermediary.startsWith("field_") || intermediary.startsWith("comp_"))) {
            initDevRemapper()
            for (info in getClassHierarchy(owner)) {
                val mapped = devRemapper.mapFieldName(KiltRemapper.remapClass(info.name, toIntermediary = true, ignoreWorkaround = true), intermediary, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))

                if (mapped != intermediary)
                    return mapped
            }
        }

        // Special handling for production libraries that rely on Intermediary-mapped libraries
        if (!shouldTryRemap && intermediary == name) {
            val hierarchy = getClassHierarchy(owner)
            for (info in hierarchy) {
                val mapped = super.mapFieldName(KiltRemapper.unmapClass(info.name), name, descriptor)

                if (mapped != intermediary)
                    return mapped
            }

            if (name.startsWith("f_") && name.endsWith("_")) {
                // Worst case scenario, we need to ensure that it still gets mapped *somehow*, because in a bunch of cases, this is due to a missing library.
                val mapped = KiltRemapper.srgMappedFields[name]

                if (mapped != null) {
                    return mapped.second
                }

                KiltRemapper.logger.warn("Failed to remap field $name:$descriptor! (owner: $owner, hierarchy: ${hierarchy.map { it.name }})")
            }
        }

        return intermediary
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap) {
            return name
        }

        val intermediary = super.mapMethodName(owner, name, descriptor)

        // Special handling for dev environments
        if (shouldTryRemap && (intermediary.startsWith("method_") || intermediary.startsWith("comp_"))) {
            initDevRemapper()

            for (info in getClassHierarchy(owner)) {
                val mapped = devRemapper.mapMethodName(KiltRemapper.remapClass(info.name, toIntermediary = true, ignoreWorkaround = true), intermediary, KiltRemapper.remapDescriptor(descriptor, toIntermediary = true))

                if (mapped != intermediary)
                    return mapped
            }
        }

        // Special handling for production libraries that rely on Intermediary-mapped libraries
        if (!shouldTryRemap && intermediary == name) {
            val hierarchy = getClassHierarchy(owner)
            for (info in hierarchy) {
                val mapped = super.mapMethodName(KiltRemapper.unmapClass(info.name), name, descriptor)

                if (mapped != intermediary)
                    return mapped
            }

            if (name.startsWith("m_") && name.endsWith("_")) {
                // Worst case scenario, we need to ensure that it still gets mapped *somehow*, because in a bunch of cases, this is due to a missing library.
                val mappedList = KiltRemapper.srgMappedMethods[name]

                if (mappedList != null && mappedList.values.isNotEmpty()) {
                    KiltRemapper.logger.debug("Using fallback method $name$descriptor! (owner: $owner, hierarchy: ${hierarchy.map { it.name }}, value: ${mappedList.values.first()})")
                    return mappedList.values.first()
                }

                KiltRemapper.logger.warn("Failed to remap method $name$descriptor! (owner: $owner, hierarchy: ${hierarchy.map { it.name }})")
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

            for (itf in currentClass.interfaces) {
                hierarchy.addAll(getClassHierarchy(itf))
            }

            currentClass = provider.getClass(currentClass.`super`).orElse(null) ?: break

            if (currentClass.name.startsWith("java/lang/") || currentClass.name.startsWith("com/google/")) {
                break
            }

            hierarchy.add(currentClass)
        } while (true)

        return hierarchy
    }
}