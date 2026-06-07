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
        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap && !name.startsWith("this$")) {
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
        }

        return intermediary
    }

    override fun mapMethodName(owner: String, name: String, descriptor: String): String {
        // really, really dumb remap because some mods actually call into this, unfortunately.
        fun matchesSelf(className: String): Boolean {
            return name == "self" && descriptor == "()L$className;" && getClassHierarchy(owner).any { it.name == className || it.`super` == className }
        }

        if (matchesSelf("net/minecraft/world/entity/LivingEntity")
            || matchesSelf("net/minecraft/network/protocol/PacketFlow")
            || matchesSelf("net/minecraft/server/level/ServerChunkCache")
            || matchesSelf("net/minecraft/server/players/PlayerList")
        )
            return $$"neoforge$self"

        if (FabricLoader.getInstance().isDevelopmentEnvironment && !KiltRemapper.forceProductionRemap && !name.startsWith("lambda$")) {
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
        }

        return intermediary
    }

    private fun mapToMojang(name: String): String {
        if (name.startsWith("net/minecraft/class_")) {
            return KiltRemapper.unmapClass(name)
        }

        return name
    }

    private fun getClassHierarchy(name: String): List<ClassProvider.IClassInfo> {
        val hierarchy = mutableListOf<ClassProvider.IClassInfo>()

        var currentClass = provider.getClass(mapToMojang(name)).orElse(null) ?: return emptyList()
        hierarchy.add(currentClass)

        do {
            if (currentClass.`super` == null) {
                break
            }

            for (itf in currentClass.interfaces) {
                hierarchy.addAll(getClassHierarchy(itf))
            }

            currentClass = provider.getClass(mapToMojang(currentClass.`super` ?: break)).orElse(null) ?: break

            if (currentClass.name.startsWith("java/lang/") || currentClass.name.startsWith("com/google/")) {
                break
            }

            hierarchy.add(currentClass)
        } while (true)

        return hierarchy
    }
}
