package xyz.bluspring.knit.loader.mod

enum class ModEnvironment {
    CLIENT, SERVER, BOTH;

    fun supportsClient(): Boolean {
        return this == CLIENT || this == BOTH
    }

    fun supportsServer(): Boolean {
        return this == SERVER || this == BOTH
    }
}