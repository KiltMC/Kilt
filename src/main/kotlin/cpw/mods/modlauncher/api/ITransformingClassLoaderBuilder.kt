package cpw.mods.modlauncher.api

import java.net.URL
import java.nio.file.Path
import java.util.*
import java.util.function.Function


interface ITransformingClassLoaderBuilder {
    fun addTransformationPath(path: Path)

    fun setClassBytesLocator(additionalClassBytesLocator: Function<String, Optional<URL>>)

    fun setResourceEnumeratorLocator(resourceEnumeratorLocator: Function<String?, Enumeration<URL>>)
}