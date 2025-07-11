package net.minecraftforge.fml.loading

import org.lwjgl.opengl.GL32C

object ImmediateWindowHandler {
    @JvmStatic
    fun getGLVersion(): String {
        val major = GL32C.glGetInteger(GL32C.GL_MAJOR_VERSION)
        val minor = GL32C.glGetInteger(GL32C.GL_MINOR_VERSION)

        return "$major.$minor"
    }
}