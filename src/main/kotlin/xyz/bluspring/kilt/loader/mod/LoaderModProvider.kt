package xyz.bluspring.kilt.loader.mod

interface LoaderModProvider {
    val name: String
    fun addModToLoader(mod: ForgeMod)
}