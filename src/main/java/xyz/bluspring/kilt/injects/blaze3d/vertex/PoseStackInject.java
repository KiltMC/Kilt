// TRACKED HASH: 03865c649a58ad37f55988ca3fafb08a94b0a986
package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import net.neoforged.neoforge.client.extensions.IPoseStackExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoseStack.class)
public abstract class PoseStackInject implements IPoseStackExtension {
}