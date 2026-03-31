package xyz.bluspring.kilt.compat.fabric.automodpack

import pl.skidam.automodpack_core.GlobalVariables
import java.nio.file.Path

object KiltAutoModpackCompat {

    val modpackDir: Path?
        get() = GlobalVariables.selectedModpackDir

}