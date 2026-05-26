package xyz.bluspring.kilt.loader.asm

import com.chocohead.mm.api.ClassTinkerers
import kotlinx.coroutines.runBlocking
import net.fabricmc.loader.api.FabricLoader
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.fart.internal.ClassProviderImpl
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Handle
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InvokeDynamicInsnNode
import org.spongepowered.asm.mixin.Mixin
import org.spongepowered.asm.mixin.Pseudo
import org.spongepowered.asm.util.Annotations
import org.spongepowered.asm.util.asm.ASM
import xyz.bluspring.kilt.Kilt
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.remap.KiltRemapper
import xyz.bluspring.kilt.util.KiltHelper
import java.io.ByteArrayOutputStream
import java.nio.file.*
import java.nio.file.attribute.BasicFileAttributes
import java.util.*
import java.util.function.Consumer
import java.util.function.Predicate
import java.util.stream.Stream

object OverridingMethodWrappers {

    @JvmStatic
    val mixinPackage = "xyz.bluspring.kilt.mixin"

    private val mixins = mutableListOf<String>()
    private val classCache = mutableMapOf<Path, ClassProvider.IClassInfo>()

    init {
        // Finalize spawn exists in a bunch of places, we need a mixin where the exact value of targets depends on what classes exist at runtime.
        val mainName = "gen/FinalizeSpawnFix"
        // The mixin itself is simple, we define it in a template class.
        val template = "xyz/bluspring/kilt/loader/asm/FinalizeSpawnTemplate"
        val helperUrl = Kilt::class.java.classLoader.getResource("$template.class")!!
        val classReader = ClassReader(helperUrl.readBytes())
        val classNode = ClassNode(ASM.API_VERSION)
        classReader.accept(classNode, 0)

        val mixinAnnotation = Annotations.getInvisible(classNode, Mixin::class.java)
        classNode.invisibleAnnotations.removeIf { node -> node.desc == "L${Pseudo::class.java.name.replace(".", "/")};" }

        // We go through all the classes in vanilla and every single loaded mod to find overrides of finalizeSpawn.
        // All of them will need the mixin.
        // Or well, technically only the ones that extend Mob need it, but checking that isn't really worth it since we'll be using intermediary names in production anyway, and they're usually unique.
        val targets = getClassesIf { classTarget ->
            classTarget.methods.any { method ->
                method.name == "finalizeSpawn" &&
                method.descriptor == "(Lnet/minecraft/world/level/ServerLevelAccessor;Lnet/minecraft/world/DifficultyInstance;Lnet/minecraft/world/entity/MobSpawnType;Lnet/minecraft/world/entity/SpawnGroupData;Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/entity/SpawnGroupData;" &&
                (Opcodes.ACC_STATIC and method.access == 0) &&
                (Opcodes.ACC_PUBLIC and method.access != 0)
            }
        }

        // When renaming the class we need to check inside the methods where the class name is hardcoded in some places.
        classNode.name = "${mixinPackage.replace(".", "/")}/$mainName"
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
                }
            }
        }

        Annotations.setValue(mixinAnnotation, "targets", targets.toList())
        val writer = ClassWriter(0)
        classNode.accept(writer)
        ClassTinkerers.define(classNode.name, writer.toByteArray())
        mixins.add(mainName.replace("/", "."))
    }

    @JvmStatic
    fun getMixins(): List<String> {
        return mixins
    }

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

    private fun getClassInfo(name: Path): ClassProvider.IClassInfo? {
        if (!classCache.containsKey(name)) {
            val stream = Files.newInputStream(name)
            val byteArray = ByteArrayOutputStream()
            val buf = ByteArray(0x100)
            var cnt = 0
            while ((stream.read(buf, 0, buf.size).also { cnt = it }) != -1) {
                byteArray.write(buf, 0, cnt)
            }
            classCache[name] = ClassProviderImpl.ClassInfo(byteArray.toByteArray())
        }
        return classCache[name]
    }

    private fun getClassesIf(paths: Stream<Path>, predicate: Predicate<ClassProvider.IClassInfo>): Set<String> {
        val classes = mutableSetOf<String>()
        paths.forEach { path ->
            for (foundClass in findClasses(path)) {
                val info = getClassInfo(foundClass)
                if (info != null && predicate.test(info)) {
                    classes.add(info.name)
                }
            }
        }
        return classes
    }

    private fun getClassesIf(predicate: Predicate<ClassProvider.IClassInfo>): Set<String> {
        return setOf(
            *getClassesIf(Arrays.stream(runBlocking { KiltRemapper.getGameClassPath() }), predicate).toTypedArray(),
            *getClassesIf(KiltHelper.getKiltPaths().stream(), predicate).toTypedArray(),
            *getClassesIf(FabricLoader.getInstance().allMods.flatMap { container -> container.rootPaths }.stream(), predicate).toTypedArray(),
            *getClassesIf(KiltLoader.instance.mods.stream().flatMap { it.paths.stream() }, predicate).toTypedArray()
        )
    }

    private fun addFromPaths(paths: Stream<Path>, name: String, subClasses: MutableSet<String>) {
        paths.forEach { path ->
            for (foundClass in findClasses(path)) {
                val info = getClassInfo(foundClass)
                if (info?.`super` == name) {
                    subClasses.add(info.name)
                }
            }
        }
    }

    private fun forClassAndSubclasses(name: String, action: Consumer<String>) {
        action.accept(name)
        val subClasses = mutableSetOf<String>()
        addFromPaths(
            Arrays.stream(runBlocking { KiltRemapper.getGameClassPath() }), name, subClasses
        )
        addFromPaths(
            KiltHelper.getKiltPaths().stream(), name, subClasses
        )
        addFromPaths(
            FabricLoader.getInstance().allMods.flatMap { container -> container.rootPaths }.stream(), name, subClasses
        )
        addFromPaths(
            KiltLoader.instance.mods.stream().flatMap { it.paths.stream() }, name, subClasses
        )
        for (subClass in subClasses) {
            forClassAndSubclasses(subClass, action)
        }
    }

}