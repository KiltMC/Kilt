package net.minecraftforge.fml

import com.google.common.collect.Streams
import net.minecraftforge.forgespi.language.IModInfo
import java.util.stream.Stream

class ModLoadingException(
    val modInfo: IModInfo,
    private val errorStage: ModLoadingStage,
    val i18NMessage: String,
    val originalException: Throwable,
    vararg val context: Any
) : RuntimeException() {
    val cleanMessage: String
        get() = Bindings.getMessageParser().get().stripControlCodes(formatToString())

    fun formatToString(): String {
        return Bindings.getMessageParser().get().parseMessage(i18NMessage, Streams.concat(Stream.of(modInfo, errorStage, cause), context.toList().stream()))
    }

    override val message: String
        get() = formatToString()
}