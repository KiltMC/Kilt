package net.minecraftforge.fml

import com.google.common.collect.Streams
import net.minecraftforge.forgespi.language.IModInfo
import java.util.stream.Stream

class ModLoadingWarning(
    private val modInfo: IModInfo,
    private val warningStage: ModLoadingStage,
    private val i18nMessage: String,
    private vararg val context: Any
) {
    fun formatToString(): String {
        return Bindings.getMessageParser().get().parseMessage(i18nMessage, Streams.concat(Stream.of(modInfo, warningStage), context.asList().stream()))
    }
}