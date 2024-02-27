// TRACKED HASH: 03865c649a58ad37f55988ca3fafb08a94b0a986
package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.extensions.IForgePoseStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PoseStack.class)
public class PoseStackInject implements IForgePoseStack {
}