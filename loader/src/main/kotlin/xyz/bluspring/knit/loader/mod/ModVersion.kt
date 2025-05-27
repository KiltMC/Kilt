package xyz.bluspring.knit.loader.mod

interface ModVersion : Comparable<ModVersion> {
    val asString: String

    companion object {
        /**
         * Empty version, used for when a type requires a version despite the version being effectively unavailable.
         */
        val EMPTY: ModVersion = object : ModVersion {
            override val asString: String
                get() = "0.0.0"

            override fun compareTo(other: ModVersion): Int {
                return -1
            }
        }
    }
}