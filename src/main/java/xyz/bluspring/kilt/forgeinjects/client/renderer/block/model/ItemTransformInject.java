package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.block.model.ItemTransform;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.render.block.model.ItemTransformInjection;

@Mixin(ItemTransform.class)
public class ItemTransformInject implements ItemTransformInjection {
    public Vector3f rightRotation = Vector3f.ZERO.copy();

    @Override
    public Vector3f getRightRotation() {
        return rightRotation;
    }

    @Override
    public void setRightRotation(Vector3f rightRotation) {
        this.rightRotation = rightRotation.copy();
    }
}
