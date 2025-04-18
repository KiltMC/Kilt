package xyz.bluspring.kilt.compat.sodium.immersiveengineering

import com.mojang.blaze3d.vertex.VertexConsumer
import me.jellysquid.mods.sodium.client.render.chunk.terrain.material.Material
import me.jellysquid.mods.sodium.client.render.chunk.vertex.builder.ChunkMeshBufferBuilder
import me.jellysquid.mods.sodium.client.render.chunk.vertex.format.ChunkVertexEncoder
import net.minecraft.util.FastColor

class SodiumIEVertexConsumer(val bufferBuilder: ChunkMeshBufferBuilder, val material: Material) : VertexConsumer {
    var x = 0f
    var y = 0f
    var z = 0f
    var color = -1
    var u = 0f
    var v = 0f
    var light = 0
    var overlay = 0

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

        val array = vertexArray.get()
        array[0] = vertex

        bufferBuilder.push(array, material)
    }

    override fun defaultColor(defaultR: Int, defaultG: Int, defaultB: Int, defaultA: Int) {
    }

    override fun unsetDefaultColor() {
    }

    companion object {
        private val vertexArray = ThreadLocal.withInitial { arrayOfNulls<ChunkVertexEncoder.Vertex>(1) }
    }
}