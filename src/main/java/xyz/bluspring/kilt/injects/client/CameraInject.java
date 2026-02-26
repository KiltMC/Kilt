// TRACKED HASH: 988ae85739fcd12e564f775b34a9e6306e31f01f
package xyz.bluspring.kilt.injects.client;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.injections.client.CameraInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(Camera.class)
public abstract class CameraInject implements CameraInjection {
    @Shadow private float yRot;
    @Shadow private float xRot;
    @Shadow private boolean initialized;
    @Shadow private BlockGetter level;
    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;
    @Shadow private Vec3 position;
    @Shadow protected abstract void setRotation(float yRot, float xRot);

    @Unique private float kilt$roll;

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 0))
    private void kilt$handleCameraAngleSetup(Camera instance, float yRot, float xRot, Operation<Void> original, @Local(argsOnly = true) float partialTick) {
        var cameraSetup = NeoForge.EVENT_BUS.post(new ViewportEvent.ComputeCameraAngles(instance, partialTick, yRot, xRot, 0));

        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Camera.class, "setRotation", float.class, float.class, float.class)) {
            instance.setRotation(cameraSetup.getYaw(), cameraSetup.getPitch(), cameraSetup.getRoll());
        } else {
            this.kilt$roll = cameraSetup.getRoll();
            original.call(instance, cameraSetup.getYaw(), cameraSetup.getPitch());
        }
    }

    @WrapOperation(method = "setup", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Camera;setRotation(FF)V", ordinal = 1))
    private void kilt$handleThirdPersonReverse(Camera instance, float yRot, float xRot, Operation<Void> original, @Local(argsOnly = true) float partialTick) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Camera.class, "setRotation", float.class, float.class, float.class)) {
            this.setRotation(yRot, xRot, -this.kilt$roll);
        } else {
            this.kilt$roll = -this.kilt$roll;
            original.call(instance, yRot, xRot);
        }
    }

    @Definition(id = "getMaxZoom", method = "Lnet/minecraft/client/Camera;getMaxZoom(F)F")
    @Expression("-this.getMaxZoom(@(4.0) * ?)")
    @ModifyExpressionValue(method = "setup", at = @At("MIXINEXTRAS:EXPRESSION"))
    private float kilt$tryGetDetachedCameraDistance(float original, @Local(argsOnly = true, ordinal = 1) boolean thirdPersonReverse, @Local(ordinal = 1) float scale) {
        return ClientHooks.getDetachedCameraDistance((Camera) (Object) this, thirdPersonReverse, scale, original);
    }

    @Override
    public void setRotation(float yaw, float pitch, float roll) {
        this.kilt$roll = roll;
        this.setRotation(yaw, pitch);
    }

    @ModifyArg(method = "setRotation", at = @At(value = "INVOKE", target = "Lorg/joml/Quaternionf;rotationYXZ(FFF)Lorg/joml/Quaternionf;"), index = 2)
    private float kilt$addRollToRotation(float angleY) {
        return angleY + this.kilt$roll;
    }

    @Override
    public float getRoll() {
        return this.kilt$roll;
    }

    @Override
    public BlockState getBlockAtCamera() {
        if (!this.initialized)
            return Blocks.AIR.defaultBlockState();
        else
            return this.level.getBlockState(this.blockPosition).getStateAtViewpoint(this.level, this.blockPosition, this.position);
    }
}