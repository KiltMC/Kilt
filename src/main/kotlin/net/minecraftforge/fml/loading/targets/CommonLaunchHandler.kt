package net.minecraftforge.fml.loading.targets

import com.mojang.logging.LogUtils
import cpw.mods.modlauncher.api.ILaunchHandlerService
import cpw.mods.modlauncher.api.ITransformingClassLoaderBuilder
import net.minecraftforge.api.distmarker.Dist
import org.slf4j.Logger
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.function.BiPredicate
import java.util.stream.Collectors


abstract class CommonLaunchHandler : ILaunchHandlerService {
    data class LocatedPaths(
        val minecraftPaths: List<Path>,
        val minecraftFilter: BiPredicate<String, String>,
        val otherModPaths: List<List<Path>>,
        val otherArtifacts: List<Path>
    )

    abstract fun getDist(): Dist

    abstract fun getNaming(): String

    open fun isProduction(): Boolean {
        return false
    }

    open fun isData(): Boolean {
        return false
    }

    abstract fun getMinecraftPaths(): LocatedPaths

    override fun configureTransformationClassLoader(builder: ITransformingClassLoaderBuilder) {}

    protected fun getModClasses(): Map<String, List<Path>> {
        val modClasses: String = Optional.ofNullable(System.getenv("MOD_CLASSES")).orElse("")
        //LOGGER.debug(LogMarkers.CORE, "Got mod coordinates {} from env", modClasses)
        data class ExplodedModPath(val modid: String, val path: Path)

        // "a/b/;c/d/;" -> "modid%%c:\fish\pepper;modid%%c:\fish2\pepper2\;modid2%%c:\fishy\bums;modid2%%c:\hmm"
        val modClassPaths =
            Arrays.stream(modClasses.split(File.pathSeparator.toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray())
                .map { inp -> inp.split("%%", limit = 2) }
                .map { splitString ->
                    ExplodedModPath(
                        if (splitString.size == 1) "defaultmodid" else splitString[0],
                        Paths.get(splitString[splitString.size - 1])
                    )
                }
                .collect(
                    Collectors.groupingBy(
                        ExplodedModPath::modid,
                        Collectors.mapping(ExplodedModPath::path, Collectors.toList())
                    )
                )
        //LOGGER.debug(LogMarkers.CORE, "Found supplied mod coordinates [{}]", modClassPaths)

        //final var explodedTargets = ((Map<String, List<ExplodedDirectoryLocator.ExplodedMod>>)arguments).computeIfAbsent("explodedTargets", a -> new ArrayList<>());
        //modClassPaths.forEach((modlabel,paths) -> explodedTargets.add(new ExplodedDirectoryLocator.ExplodedMod(modlabel, paths)));
        return modClassPaths
    }

    companion object {
        protected val LOGGER: Logger = LogUtils.getLogger()
    }
}