package xyz.bluspring.kilt.compat.forge.immersiveengineering

import com.mojang.blaze3d.vertex.VertexConsumer
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder
import net.minecraft.util.FastColor

class SodiumIEVertexConsumer private constructor() : VertexConsumer {
    private val vertexArray = arrayOfNulls<ChunkVertexEncoder.Vertex>(4)
    private var currentIndex = 0

    var x = 0f
    var y = 0f
    var z = 0f
    var color = -1
    var u = 0f
    var v = 0f
    var light = 0
    var overlay = 0

    private var builder: ChunkMeshBufferBuilder? = null
    private var material: Material? = null

    private fun bind(builder: ChunkMeshBufferBuilder, material: Material) {
        this.builder = builder
        this.material = material
    }

    fun unbind() {
        this.builder = null
        this.material = null
    }

    override fun vertex(x: Double, y: Double, z: Double): VertexConsumer {
        this.x = x.toFloat()
        this.y = y.toFloat()
        this.z = z.toFloat()

        return this
    }

    override fun color(red: Int, green: Int, blue: Int, alpha: Int): VertexConsumer {
        this.color = FastColor.ABGR32.color(alpha, blue, green, red)

        return this
    }

    override fun uv(u: Float, v: Float): VertexConsumer {
        this.u = u
        this.v = v

        return this
    }

    override fun overlayCoords(u: Int, v: Int): VertexConsumer {
        overlay = u or (v shl 16)
        return this
    }

    override fun uv2(u: Int, v: Int): VertexConsumer {
        light = u or (v shl 16)
        return this
    }

    override fun normal(x: Float, y: Float, z: Float): VertexConsumer {
        return this
    }

    override fun endVertex() {
        val vertex = ChunkVertexEncoder.Vertex()
        vertex.x = x
        vertex.y = y
        vertex.z = z
        vertex.color = color
        vertex.u = u
        vertex.v = v
        vertex.light = light

        vertexArray[currentIndex++] = vertex

        if (currentIndex >= 4) {
            pushVertexData()
        }
    }

    override fun defaultColor(defaultR: Int, defaultG: Int, defaultB: Int, defaultA: Int) {
    }

    override fun unsetDefaultColor() {
    }

    private fun pushVertexData() {
        if (this.builder == null)
            throw IllegalStateException("Vertex consumer not bound!")

        this.builder!!.push(vertexArray, material)

        for (i in 0 until 4) {
            this.vertexArray[i] = null
        }

        this.currentIndex = 0
    }

    companion object {
        private val consumerProvider = ThreadLocal.withInitial(::SodiumIEVertexConsumer)

        @JvmStatic
        fun grab(builder: ChunkMeshBufferBuilder, material: Material): SodiumIEVertexConsumer {
            val consumer = consumerProvider.get()
            consumer.bind(builder, material)

            return consumer
        }
    }
}