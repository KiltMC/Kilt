// TRACKED HASH: 16d8228163cb34e67ad0a852b03a7ae5d7a2e843
package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import net.minecraft.client.renderer.block.model.ItemTransform;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.client.renderer.block.model.ItemTransformInjection;

@Mixin(ItemTransform.class)
public abstract class ItemTransformInject implements ItemTransformInjection {
    public Vector3f rightRotation = new Vector3f();

    @Override
    public Vector3f getRightRotation() {
        return rightRotation;
    }

    @Override
    public void setRightRotation(Vector3f rightRotation) {
        this.rightRotation = new Vector3f(rightRotation);
    }

    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale) {}

    @CreateInitializer
    public ItemTransformInject(Vector3f leftRotation, Vector3f translation, Vector3f scale, Vector3f rightRotation) {
        this(leftRotation, translation, scale);

        this.setRightRotation(rightRotation);
    }


}