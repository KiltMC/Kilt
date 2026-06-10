package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.*
import org.objectweb.asm.tree.*
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.util.Annotations
import org.spongepowered.asm.util.asm.ASM
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.loader.remap.fixers.mixin.MixinRemapper
import xyz.bluspring.kilt.util.KiltHelper
import java.io.ByteArrayOutputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.function.Predicate

object OverridingMethodWrappers {

    const val MIXIN_PACKAGE = "xyz.bluspring.kilt.mixin"

    private const val unmappedFinalizedSpawnDesc = "(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;"

    val finalizeSpawnName: String = KiltRemapper.enhancedMojangRemapper?.mapMethodName("net/minecraft/world/entity/Mob", "finalizeSpawn", unmappedFinalizedSpawnDesc)!!

    val finalizeSpawnDesc = KiltRemapper.remapDescriptor(unmappedFinalizedSpawnDesc)

    @JvmStatic
    val mixins = mutableListOf<String>()
    private val classCache = mutableMapOf<Path, ClassNode>()

    var enhancedMojangRemapper: KiltEnhancedRemapper? = KiltRemapper.enhancedMojangRemapper
        private set

    interface TargetClass {}

    init {
        // Finalize spawn exists in a bunch of places, we need a mixin where the exact value of targets depends on what classes exist at runtime.
        val mainName = "gen/FinalizeSpawnFix"
        val template = "xyz/bluspring/kilt/loader/asm/template/FinalizeSpawnTemplate"

        prepareMixinFromTemplate(mainName, template, false, { classTarget ->
            classTarget.methods.any { method ->
                method.name == finalizeSpawnName &&
                method.desc == finalizeSpawnDesc &&
                (Opcodes.ACC_STATIC and method.access == 0) &&
                (Opcodes.ACC_PUBLIC and method.access != 0)
            }
        })

        classCache.clear()
        enhancedMojangRemapper = null
    }

    private fun prepareMixinFromTemplate(
        name: String, template: String, oneMixinPerTarget: Boolean, predicate: Predicate<ClassNode>
    ) {
        val helperUrl = Kilt::class.java.classLoader.getResource("$template.class")!!
        val classReader = ClassReader(helperUrl.readBytes())

        // We go through all the classes in vanilla and every single loaded mod to find overrides of finalizeSpawn.
        // All of them will need the mixin.
        // Or well, technically only the ones that extend Mob need it, but checking that isn't really worth it since we'll be using intermediary names in production anyway, and they're usually unique.
        val targets = getClassesIf(predicate).getTargets()

        val mappings = mutableMapOf<String, String>()
        val alreadyRefMapped = mutableSetOf<String>()

		if (oneMixinPerTarget) {
            for ((i, target) in targets.withIndex()) {
                prepareSingleMixinFromTemplate(
                    classReader, targets, template, "$name$i",
                    setOf(target), mappings, alreadyRefMapped
                )
            }
        } else {
            prepareSingleMixinFromTemplate(
                classReader, targets, template, name,
                targets, mappings, alreadyRefMapped
            )
        }
    }

    private fun prepareSingleMixinFromTemplate(
        classReader: ClassReader, remapTargets: Set<String>,
        template: String, currentName: String, actualTargets: Set<String>,
        mappings: MutableMap<String, String>, alreadyRefMapped: MutableSet<String>,
    ) {
        val classNode = ClassNode(ASM.API_VERSION)
        classReader.accept(classNode, 0)

        val mixinAnnotation = Annotations.getInvisible(classNode, Mixin::class.java)
        classNode.invisibleAnnotations.removeIf { node -> node.desc == "L${Pseudo::class.java.name.replace(".", "/")};" }

        fun remapAnnotation(annotationNode: AnnotationNode) {
            MixinRemapper.remapMixinAnnotation(
                annotationNode, enhancedMojangRemapper!!,
                remapTargets.map { KiltRemapper.unmapClass(it) }, classNode.name, mappings,
                alreadyRefMapped, null
            )
        }

        // When renaming the class we need to check inside the methods where the class name is hardcoded in some places.
        classNode.name = "${MIXIN_PACKAGE.replace(".", "/")}/$currentName"
        for (method in classNode.methods) {
            for (variable in method.localVariables) {
                if (variable.desc == "L$template;") {
                    variable.desc = "L${classNode.name};"
                }
            }
            for (ins in method.instructions) {
                when (ins) {
                    is InvokeDynamicInsnNode -> {
                        ins.desc = ins.desc.replace(template, classNode.name)
                        for (i in ins.bsmArgs.indices) {
                            val arg = ins.bsmArgs[i]
                            if (arg is Handle) {
                                ins.bsmArgs[i] = Handle(
                                    arg.tag, if (arg.owner == template) classNode.name else arg.owner,
                                    arg.name, arg.desc.replace(template, classNode.name),
                                    arg.isInterface
                                )
                            }
                        }
                    }
                    is MethodInsnNode -> {
                        if (ins.owner == template) {
                            ins.owner = classNode.name
                        }
                    }
                    is LdcInsnNode -> {
                        if (actualTargets.size == 1) {
                            val type = ins.cst
                            if (type is Type && type.descriptor == "L${TargetClass::class.java.name.replace(".", "/")};") {
                                ins.cst = Type.getType("L${actualTargets.first()};")
                            }
                        }
                    }
                }
            }
            method.invisibleAnnotations?.forEach { remapAnnotation(it) }
            method.visibleAnnotations?.forEach { remapAnnotation(it) }
        }

        Annotations.setValue(mixinAnnotation, "targets", actualTargets.toList())
        val writer = ClassWriter(0)
        classNode.accept(writer)
        ClassTinkerers.define(classNode.name, writer.toByteArray())
        mixins.add(currentName.replace("/", "."))
    }

    @JvmStatic
    fun init() {}

    // Based on ClassProviderBuilderImpl::addLibrary
    private fun findClasses(path: Path): Set<Path> {
        var innerPath: Path
        if (Files.isDirectory(path)) {
            innerPath = path
        } else if (Files.isRegularFile(path)) {
            val zipFs = FileSystems.newFileSystem(path, null as ClassLoader?)
            innerPath = zipFs.getPath("/")
        } else {
            return setOf()
        }
        val classes = mutableSetOf<Path>()
        Files.walkFileTree(
            innerPath, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    if (file.toString().endsWith(".class")) {
                        classes.add(file)
                    }
                    return FileVisitResult.CONTINUE
                }
            }
        )
        return classes
    }

    // Based on ClassProviderImpl.
    private fun getClassInfo(name: Path): ClassNode? {
        if (!classCache.containsKey(name)) {
            val stream = Files.newInputStream(name)
            val byteArray = ByteArrayOutputStream()
            val buf = ByteArray(0x100)
            var cnt = 0
            while ((stream.read(buf, 0, buf.size).also { cnt = it }) != -1) {
                byteArray.write(buf, 0, cnt)
            }
            val reader = ClassReader(byteArray.toByteArray())
            val node = ClassNode()
            reader.accept(node, ClassReader.SKIP_CODE)
            classCache[name] = node
        }
        return classCache[name]
    }

    private fun getClassesIf(paths: Collection<Path>, predicate: Predicate<ClassNode>): ClassResult {
        val classes = mutableSetOf<String>()
        val mixins = mutableMapOf<String, MutableSet<String>>()
        paths.forEach { path ->
            for (foundClass in findClasses(path)) {
                val info = getClassInfo(foundClass)
                if (info != null && predicate.test(info)) {
                    val mixinAnnotation = Annotations.getInvisible(info, Mixin::class.java)
                    if (mixinAnnotation != null) {
                        mixins[info.name] = Annotations.getValue<List<Type>?>(mixinAnnotation, "value")?.map { it.internalName }?.toMutableSet() ?: mutableSetOf()
                        val targets = Annotations.getValue<List<String>?>(mixinAnnotation, "targets")
                        if (targets != null) {
                            mixins[info.name]?.addAll(targets)
                        }
                    } else {
                        classes.add(info.name)
                    }
                }
            }
        }
        return ClassResult(classes, mixins)
    }

    data class ClassResult(val normalClasses: Set<String>, val mixinClasses: Map<String, Set<String>>) {
        fun getTargets(): Set<String> {
            return normalClasses.plus(mixinClasses.values.flatten())
        }
    }

    private fun combine(vararg results: ClassResult): ClassResult {
        return ClassResult(
            results.flatMap { it.normalClasses }.toSet(),
            results.flatMap { it.mixinClasses.entries.map { e -> Pair(e.key, e.value) } }.toMap()
        )
    }

    private fun getClassesIf(predicate: Predicate<ClassNode>): ClassResult {
        return combine(
            getClassesIf(listOf(*runBlocking { KiltRemapper.getGameClassPath() }), predicate),
            getClassesIf(KiltHelper.getKiltPaths(), predicate),
            getClassesIf(FabricLoader.getInstance().allMods.flatMap { container -> container.rootPaths }, predicate),
            getClassesIf(KiltLoader.instance.mods.flatMap { it.paths }, predicate)
        )
    }
}
