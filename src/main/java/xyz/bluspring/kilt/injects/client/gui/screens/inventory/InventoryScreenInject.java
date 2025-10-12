// TRACKED HASH: c029261ae38cc61261096fafc56f6cf6b641dbe9
package xyz.bluspring.kilt.injects.client.gui.screens.inventory;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenInject {

    @Shadow
    public static void renderEntityInInventory(GuiGraphics graphics, float x, float y, float scale, Vector3f translate, Quaternionf pose, @Nullable Quaternionf cameraOrientation, LivingEntity livingEntity) {
        throw new IllegalStateException("bruh");
    }

    @CreateStatic
    private static void renderEntityInInventoryFollowsAngle(
            GuiGraphics graphics,
            int x1,
            int y1,
            int x2,
            int y2,
            int scale,
            float yOffset,
            float angleXComponent,
            float angleYComponent,
            LivingEntity entity
    ) {
        float f = (float)(x1 + x2) / 2.0F;
        float f1 = (float)(y1 + y2) / 2.0F;
        graphics.enableScissor(x1, y1, x2, y2);
        float f2 = angleXComponent;
        float f3 = angleYComponent;
        Quaternionf quaternionf = new Quaternionf().rotateZ((float) Math.PI);
        Quaternionf quaternionf1 = new Quaternionf().rotateX(f3 * 20.0F * (float) (Math.PI / 180.0));
        quaternionf.mul(quaternionf1);
        float f4 = entity.yBodyRot;
        float f5 = entity.getYRot();
        float f6 = entity.getXRot();
        float f7 = entity.yHeadRotO;
        float f8 = entity.yHeadRot;
        entity.yBodyRot = 180.0F + f2 * 20.0F;
        entity.setYRot(180.0F + f2 * 40.0F);
        entity.setXRot(-f3 * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        float f9 = entity.getScale();
        Vector3f vector3f = new Vector3f(0.0F, entity.getBbHeight() / 2.0F + yOffset * f9, 0.0F);
        float f10 = (float)scale / f9;
        renderEntityInInventory(graphics, f, f1, f10, vector3f, quaternionf, quaternionf1, entity);
        entity.yBodyRot = f4;
        entity.setYRot(f5);
        entity.setXRot(f6);
        entity.yHeadRotO = f7;
        entity.yHeadRot = f8;
        graphics.disableScissor();
    }
}