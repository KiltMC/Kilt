package net.minecraftforge.fml

class LoadingFailedException(val errors: List<ModLoadingException>) : RuntimeException() {
    override val message: String
        get() = "Loading errors encountered: ${errors.joinToString(",\n\t", "[\n\t", "\n]") {
            it.message
        }}"
}