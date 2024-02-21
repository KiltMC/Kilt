package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.blaze3d.vertex.BufferBuilderInjection;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderInject implements BufferBuilderInjection {
    @Shadow protected abstract void ensureCapacity(int increaseAmount);

    @Shadow private VertexFormat format;

    @Shadow private ByteBuffer buffer;

    @Shadow private int nextElementByte;

    @Shadow private int vertices;

    @Override
    public void putBulkData(ByteBuffer buffer) {
        this.ensureCapacity(buffer.limit() + this.format.getVertexSize());
        this.buffer.position(this.nextElementByte);
        this.buffer.put(buffer);
        this.buffer.position(0);
        this.vertices += buffer.limit() / this.format.getVertexSize();
        this.nextElementByte += buffer.limit();
    }
}
