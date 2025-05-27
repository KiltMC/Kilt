package xyz.bluspring.knit.loader.mod

interface VersionConstraint {
    fun matches(versionString: String): Boolean

    /**
     * Used to display the proper constraints in "missing dependency" screens.
     */
    override fun toString(): String
}