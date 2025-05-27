package xyz.bluspring.knit.loader.mod

data class ModDependency(
    val id: String,
    val constraint: VersionConstraint,
    val type: Type
) {
    enum class Type(val checkIsMissing: Boolean, val shouldExitOnFail: Boolean = false) {
        /**
         * If no dependencies exist by the provided constraints, an exception will be thrown immediately.
         */
        REQUIRED(true, true),

        /**
         * If no dependencies exist by the mod ID, it will skip over this dependency.
         * However, if a dependency exists that is outside the provided constraints, an exception will be thrown.
         */
        OPTIONAL(true, false),

        /**
         * If any dependencies match the provided constraints, an exception will be thrown immediately.
         */
        INCOMPATIBLE(false, true),

        /**
         * If any dependencies match the provided constraints, a warning will be logged to the console.
         */
        DISCOURAGED(false, false);
    }
}
