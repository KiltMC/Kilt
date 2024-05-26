package xyz.bluspring.kilt.loader.asm

import org.openjdk.nashorn.api.scripting.ScriptObjectMirror
import java.util.function.Function
import javax.script.Bindings

object NashornHelper {
    fun <A, R> getFunction(obj: Bindings): Function<A, R> {
        return Function {
            (obj as ScriptObjectMirror).call(obj, it) as R
        }
    }
}