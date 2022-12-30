package net.minecraftorge.fml.loading.moddiscovery

import com.mojang.logging.LogUtils
import cpw.mods.jarhandling.SecureJar
import net.minecraftforge.forgespi.language.IModFileInfo
import net.minecraftforge.forgespi.language.IModInfo
import net.minecraftforge.forgespi.language.IModLanguageProvider
import net.minecraftforge.forgespi.language.ModFileScanData
import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import net.minecraftforge.forgespi.locating.ModFileFactory.ModFileInfoParser
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer
import java.util.function.Supplier
import java.util.jar.Attributes
import java.util.jar.Manifest
/*
class ModFile(
    private val jar: SecureJar,
    private val provider: IModProvider,
    private val parser: ModFileInfoParser,
    type: String = parseType(jar)
) : IModFile {
    private var fileProperties = mutableMapOf<String, Any>()
    private val loaders = mutableListOf<IModLanguageProvider>()
    private var scanError: Throwable? = null
    private val modFileType = IModFile.Type.valueOf(type)
    private val manifest: Manifest = jar.moduleDataProvider().manifest
    private val jarVersion: String = Optional.ofNullable(manifest.mainAttributes.getValue(Attributes.Name.IMPLEMENTATION_VERSION)).orElse("0.0NONE");
    private var modFileInfo: IModFileInfo? = ModFileParser.readModList(this, parser)
    private var fileModFileScanData: ModFileScanData? = null
    private var futureScanResult: CompletableFuture<ModFileScanData>? = null
    private val coreMods = mutableListOf<CoreModFile>()
    private var accessTransformer: Path? = null

    private var securityStatus: SecureJar.Status? = null

    override fun getLoaders(): MutableList<IModLanguageProvider> {
        return loaders
    }

    override fun findResource(vararg pathName: String): Path {
        if (pathName.isEmpty())
            throw IllegalArgumentException("Missing path")

        return secureJar.getPath(pathName.joinToString("/"))
    }

    override fun getSubstitutionMap(): Supplier<MutableMap<String, Any>> {
        return Supplier {
            mutableMapOf<String, Any>().apply {
                this["jarVersion"] = jarVersion
                this.putAll(fileProperties)
            }
        }
    }

    override fun getType(): IModFile.Type {
        return modFileType
    }

    override fun getFilePath(): Path {
        return jar.primaryPath
    }

    override fun getSecureJar(): SecureJar {
        return jar
    }

    override fun setSecurityStatus(status: SecureJar.Status?) {
        securityStatus = status
    }

    override fun getModInfos(): MutableList<IModInfo> {
        return modFileInfo.mods
    }

    fun getAccessTransformer(): Optional<Path> {
        return Optional.ofNullable(if (accessTransformer?.let { Files.exists(it) } == true) accessTransformer else null)
    }

    fun identifyMods(): Boolean {
        modFileInfo = ModFileParser.readModList(this, parser)

        if (modFileInfo == null)
            return type != IModFile.Type.MOD

        LOGGER.debug("Loading mod file $filePath with languages ${modFileInfo!!.requiredLanguageLoaders()}")

        coreMods = ModFileParser.getCoreMods(this)
        coreMods.forEach {
            LOGGER.debug("Found coremod ${it.path}")
        }
        accessTransformer = findResource("META-INF", "accesstransformer.cfg")

        return true
    }

    fun setScanResult(modFileScanData: ModFileScanData?, throwable: Throwable?) {
        futureScanResult = null
        fileModFileScanData = modFileScanData
        if (throwable != null) {
            scanError = throwable
        }
        LOGGER.info("Completed deep scan of $fileName")
    }

    fun setFileProperties(fileProperties: Map<String, Any>) {
        this.fileProperties = fileProperties.toMutableMap()
    }

    override fun getScanResult(): ModFileScanData? {
        if (futureScanResult != null) {
            try {
                futureScanResult!!.get()
            } catch (e: Exception) {
                LOGGER.error("Caught unexpected exception processing scan results", e)
            }
        }

        if (scanError != null) {
            throw RuntimeException(scanError)
        }

        return fileModFileScanData
    }

    override fun getFileName(): String {
        return filePath.fileName.toString()
    }

    override fun getProvider(): IModProvider {
        return provider
    }

    override fun getModFileInfo(): IModFileInfo? {
        return modFileInfo
    }

    /**
     * Run in an executor thread to harvest the class and annotation list
     */
    fun compileContent(): ModFileScanData {
        return Scanner(this).scan()
    }

    fun scanFile(pathConsumer: Consumer<Path?>?) {
        provider.scanFile(this, pathConsumer)
    }

    fun setFutureScanResult(future: CompletableFuture<ModFileScanData>?) {
        futureScanResult = future
    }

    companion object {
        private val TYPE = Attributes.Name("FMLModType")
        private val LOGGER = LogUtils.getLogger()

        private fun parseType(jar: SecureJar): String {
            val m = jar.moduleDataProvider().manifest
            val value: Optional<String> = Optional.ofNullable(m.mainAttributes.getValue(TYPE))
            return value.orElse("MOD")
        }
    }
}*/