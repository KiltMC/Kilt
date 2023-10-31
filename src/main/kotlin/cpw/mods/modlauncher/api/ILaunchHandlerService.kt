package cpw.mods.modlauncher.api

/**
 * A singleton instance of this is loaded by the system to designate the launch target
 */
interface ILaunchHandlerService {
    fun name(): String

    @Deprecated("")
    fun configureTransformationClassLoader(builder: ITransformingClassLoaderBuilder)

    fun launchService(arguments: Array<String>, gameLayer: ModuleLayer): ServiceRunner

    fun getPaths(): Array<NamedPath> {
        return arrayOf()
    }
}