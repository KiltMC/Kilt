package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.Camera;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.CameraInjection;

@Mixin(Camera.class)
public class CameraInject implements CameraInjection {
    @Shadow private float yRot;

    @Shadow private float xRot;

    @Shadow private boolean initialized;

    @Shadow private BlockGetter level;

    @Shadow @Final private BlockPos.MutableBlockPos blockPosition;

    @Shadow private Vec3 position;

    @Override
    public void setAnglesInternal(float yaw, float pitch) {
        this.yRot = yaw;
        this.xRot = pitch;
    }

    @Override
    public BlockState getBlockAtCamera() {
        if (!this.initialized)
            return Blocks.AIR.defaultBlockState();
        else
            return this.level.getBlockState(this.blockPosition).getStateAtViewpoint(this.level, this.blockPosition, this.position);
    }
}
