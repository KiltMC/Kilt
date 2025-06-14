package net.neoforged.fml.loading

import com.electronwill.nightconfig.core.CommentedConfig
import com.electronwill.nightconfig.core.ConfigSpec
import com.electronwill.nightconfig.core.file.CommentedFileConfig
import fuzs.forgeconfigapiport.impl.config.ForgeConfigApiPortConfig
import java.lang.Boolean
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
            Boolean.TRUE,
            "Should we control the window. Disabling this disables new GL features and can be bad for mods that rely on them."
        ),
        MAX_THREADS(
            "maxThreads",
            -1,
            "Max threads for early initialization parallelism,  -1 is based on processor count",
            FMLConfig::maxThreads
        ),
        VERSION_CHECK("versionCheck", Boolean.TRUE, "Enable forge global version checking"),
        DEFAULT_CONFIG_PATH("defaultConfigPath", "defaultconfigs", "Default config path for servers"),
        DISABLE_OPTIMIZED_DFU(
            "disableOptimizedDFU",
            Boolean.TRUE,
            "Disables Optimized DFU client-side - already disabled on servers"
        ),
        EARLY_WINDOW_PROVIDER("earlyWindowProvider", "fmlearlywindow", "Early window provider"),
        EARLY_WINDOW_WIDTH("earlyWindowWidth", 854, "Early window width"),
        EARLY_WINDOW_HEIGHT("earlyWindowHeight", 480, "Early window height"),
        EARLY_WINDOW_FBSCALE("earlyWindowFBScale", 1, "Early window framebuffer scale"),
        EARLY_WINDOW_MAXIMIZED("earlyWindowMaximized", Boolean.FALSE, "Early window starts maximized"),
        EARLY_WINDOW_SKIP_GL_VERSIONS(
            "earlyWindowSkipGLVersions",
            mutableListOf<Any?>(),
            "Skip specific GL versions, may help with buggy graphics card drivers"
        ),
        EARLY_WINDOW_SQUIR("earlyWindowSquir", Boolean.FALSE, "Squir?"),
        EARLY_WINDOW_SHOW_CPU("earlyWindowShowCPU", Boolean.FALSE, "Whether to show CPU usage stats in early window"),
        EARLY_WINDOW_LOG_HELP_MSG(
            "earlyWindowLogHelpMessage",
            Boolean.TRUE,
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

        private fun <T> getConfigValue(config: CommentedFileConfig): T? {
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

    @JvmStatic
    fun load() {
    }

    @JvmStatic
    fun getConfigValue(v: ConfigValue): String {
        return ForgeConfigApiPortConfig.INSTANCE.getValue<String>(v.entry)
    }

    @JvmStatic
    fun getBoolConfigValue(v: ConfigValue): Boolean {
        return ForgeConfigApiPortConfig.INSTANCE.getValue<Boolean>(v.entry)
    }

    @JvmStatic
    fun getIntConfigValue(v: ConfigValue): Int {
        return ForgeConfigApiPortConfig.INSTANCE.getValue<Int>(v.entry)
    }

    @JvmStatic
    fun <A> getListConfigValue(v: ConfigValue): List<A> {
        return ForgeConfigApiPortConfig.INSTANCE.getValue<List<A>>(v.entry)
    }

    fun <T> updateConfig(v: ConfigValue, value: T) {
        // NO-OP
    }

    @JvmStatic
    fun defaultConfigPath(): String {
        return ForgeConfigApiPortConfig.INSTANCE.getValue<String>("defaultConfigsPath")
    }
}