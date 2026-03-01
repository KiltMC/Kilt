package net.minecraftforge.fml.loading

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.ConfigSpec
import com.electronwill.nightconfig.core.file.CommentedFileConfig
import com.electronwill.nightconfig.core.file.FileNotFoundAction
import com.electronwill.nightconfig.core.io.ParsingException
import com.electronwill.nightconfig.core.io.WritingMode
import com.mojang.logging.LogUtils
import net.minecraftforge.versions.forge.ForgeVersion
import xyz.bluspring.kilt.loader.KiltLoader
import java.nio.file.Path
import java.nio.file.Paths
import java.util.function.Function
import java.util.function.Predicate
import kotlin.Any
import kotlin.Int
import kotlin.String

object FMLConfig {
    enum class ConfigValue(
        val entry: String,
        private val defaultValue: Any,
        private val comment: String?,
        private val entryFunction: Function<Any?, Any?> = Function.identity()
    ) {
        EARLY_WINDOW_CONTROL(
            "earlyWindowControl",
            true,
            "Should we control the window. Disabling this disables new GL features and can be bad for mods that rely on them."
        ),
        MAX_THREADS(
            "maxThreads",
            -1,
            "Max threads for early initialization parallelism,  -1 is based on processor count",
            FMLConfig::maxThreads
        ),
        VERSION_CHECK("versionCheck", true, "Enable forge global version checking"),
        DEFAULT_CONFIG_PATH("defaultConfigPath", "defaultconfigs", "Default config path for servers"),
        DISABLE_OPTIMIZED_DFU(
            "disableOptimizedDFU",
            true,
            "Disables Optimized DFU client-side - already disabled on servers"
        ),
        EARLY_WINDOW_PROVIDER("earlyWindowProvider", "fmlearlywindow", "Early window provider"),
        EARLY_WINDOW_WIDTH("earlyWindowWidth", 854, "Early window width"),
        EARLY_WINDOW_HEIGHT("earlyWindowHeight", 480, "Early window height"),
        EARLY_WINDOW_FBSCALE("earlyWindowFBScale", 1, "Early window framebuffer scale"),
        EARLY_WINDOW_MAXIMIZED("earlyWindowMaximized", false, "Early window starts maximized"),
        EARLY_WINDOW_SKIP_GL_VERSIONS(
            "earlyWindowSkipGLVersions",
            mutableListOf<Any?>(),
            "Skip specific GL versions, may help with buggy graphics card drivers"
        ),
        EARLY_WINDOW_SQUIR("earlyWindowSquir", false, "Squir?"),
        EARLY_WINDOW_SHOW_CPU("earlyWindowShowCPU", false, "Whether to show CPU usage stats in early window"),
        EARLY_WINDOW_LOG_HELP_MSG(
            "earlyWindowLogHelpMessage",
            true,
            "Whether to log a help message on first attempt, to aid troubleshooting. This setting should automatically disable itself after a successful launch"
        ),
        ;

        private val valueType: Class<*> = defaultValue.javaClass

        fun buildConfigEntry(spec: ConfigSpec, commentedConfig: CommentedConfig) {
            if (this.defaultValue is MutableList<*>) {
                spec.defineList(this.entry, defaultValue, Predicate { e: Any? -> e is String })
            } else {
                spec.define(this.entry, this.defaultValue)
            }
            commentedConfig.add(this.entry, this.defaultValue)
            commentedConfig.setComment(this.entry, this.comment)
        }

        fun <T> getConfigValue(config: CommentedFileConfig): T? {
            return this.entryFunction.apply(config.get<T?>(this.entry)) as T?
        }

        fun <T> updateValue(configData: CommentedFileConfig, value: T?) {
            configData.set<T?>(this.entry, value)
        }
    }

    @JvmStatic
    private fun maxThreads(value: Any?): Any? {
        val threads = value as Int
        if (threads <= 0)
            return Runtime.getRuntime().availableProcessors()

        return threads
    }

    @JvmStatic private val LOGGER = LogUtils.getLogger()
    @JvmStatic private val configSpec = ConfigSpec()
    @JvmStatic private val configComments = CommentedConfig.inMemory()

    init {
        for (cv in ConfigValue.entries) {
            cv.buildConfigEntry(configSpec, configComments)
        }
    }

    private lateinit var configData: CommentedFileConfig

    private fun loadFrom(configFile: Path) {
        configData = CommentedFileConfig.builder(configFile).sync()
            .onFileNotFound(FileNotFoundAction.copyData(KiltLoader::class.java.getResourceAsStream("/META-INF/defaultfmlconfig.toml")!!))
            .writingMode(WritingMode.REPLACE)
            .build()

        try {
            configData.load()
        } catch (e: ParsingException) {
            throw RuntimeException("Failed to load FML config from $configFile", e)
        }

        if (!configSpec.isCorrect(configData)) {
            LOGGER.warn(LogMarkers.CORE, "Configuration file $configFile is not correct. Correcting")
            configSpec.correct(configData) { _, path, incorrectValue, correctedValue ->
                LOGGER.info(LogMarkers.CORE, "Incorrect key $path was corrected from $incorrectValue to $correctedValue")
            }
        }

        configData.putAll(configComments)
        configData.save()
    }

    @JvmStatic
    fun load() {
        val configFile = FMLPaths.FMLCONFIG.get()
        loadFrom(configFile)

        if (LOGGER.isTraceEnabled(LogMarkers.CORE)) {
            LOGGER.trace(LogMarkers.CORE, "Loaded FML config from ${FMLPaths.FMLCONFIG.get()}")

            for (cv in ConfigValue.entries) {
                LOGGER.trace(LogMarkers.CORE, "FMLConfig ${cv.entry} is ${cv.getConfigValue<Any?>(configData)}")
            }
        }

        FMLPaths.getOrCreateGameRelativePath(Paths.get(getConfigValue(ConfigValue.DEFAULT_CONFIG_PATH)))
    }

    @JvmStatic
    fun getConfigValue(v: ConfigValue): String? {
        return v.getConfigValue<String>(configData)
    }

    @JvmStatic
    fun getBoolConfigValue(v: ConfigValue): Boolean? {
        return v.getConfigValue(configData)
    }

    @JvmStatic
    fun getIntConfigValue(v: ConfigValue): Int? {
        return v.getConfigValue(configData)
    }

    @JvmStatic
    fun <A> getListConfigValue(v: ConfigValue): List<A>? {
        return v.getConfigValue(configData)
    }

    fun <T> updateConfig(v: ConfigValue, value: T) {
        v.updateValue(configData, value)
        configData.save()
    }

    @JvmStatic
    fun defaultConfigPath(): String {
        return getConfigValue(ConfigValue.DEFAULT_CONFIG_PATH)!!
    }
}