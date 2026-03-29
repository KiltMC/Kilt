package xyz.bluspring.kilt.compat.fabric.automodpack

import pl.skidam.automodpack_core.GlobalVariables
import java.nio.file.Path

object KiltAutoModpackCompat {

    fun getModpackDir(): Path? {
        return GlobalVariables.selectedModpackDir
    }

}