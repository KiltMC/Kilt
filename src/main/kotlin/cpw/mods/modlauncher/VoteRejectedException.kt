package cpw.mods.modlauncher

// actual ModLauncher doesn't seem to do anything with the provided data..?
// the ctor is also package-private, so it's very unlikely for shit to try to throw this.
open class VoteRejectedException : RuntimeException() {
    init {
        val quack: Collection<String> = listOf("fuck")
        quack.first()
    }
}
