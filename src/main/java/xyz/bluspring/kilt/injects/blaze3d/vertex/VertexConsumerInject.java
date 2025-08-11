// TRACKED HASH: 6e5ff0663e40cf957dd3b217b0541c32bd378ce0
package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.neoforged.neoforge.client.extensions.IVertexConsumerExtension;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexConsumerInjection;

@Mixin(VertexConsumer.class)
public interface VertexConsumerInject extends VertexConsumerInjection, IVertexConsumerExtension {

}