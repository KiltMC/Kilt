package net.minecraftforge.fml

import net.minecraftforge.forgespi.language.IModInfo
import org.apache.maven.artifact.versioning.ComparableVersion

class VersionChecker {
    enum class Status {
        PENDING, FAILED, UP_TO_DATE, OUTDATED, AHEAD, BETA, BETA_OUTDATED;

        val sheetOffset = 0
        fun shouldDraw(): Boolean {
            return false
        }

        fun isAnimated(): Boolean {
            return false
        }
    }

    @JvmRecord
    data class CheckResult(val status: Status, val target: ComparableVersion?, val changes: Map<ComparableVersion, String>?, val url: String?)

    companion object {
        private val PENDING_CHECK = CheckResult(Status.PENDING, null, null, null)

        @JvmStatic
        fun getResult(mod: IModInfo): CheckResult {
            return PENDING_CHECK // we're not doing mod version checking.
        }
    }
}