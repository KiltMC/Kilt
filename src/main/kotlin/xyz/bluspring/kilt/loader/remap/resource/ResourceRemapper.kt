package xyz.bluspring.kilt.loader.remap.resource

import xyz.bluspring.knit.loader.mod.ModDefinition
import java.io.InputStream

interface ResourceRemapper {
    fun canTransform(path: String): Boolean

    fun transform(mod: ModDefinition, path: String, input: InputStream): ByteArray? {
        return transform(path, input)
    }

    fun transform(path: String, input: InputStream): ByteArray? {
        return null
    }
}