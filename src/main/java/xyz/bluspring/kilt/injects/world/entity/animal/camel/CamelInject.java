package xyz.bluspring.kilt.injects.world.entity.animal.camel;

import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

@Mixin(Camel.class)
public abstract class CamelInject extends AbstractHorse {
    protected CamelInject(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "executeRidersJump", at = @At("TAIL"))
    private void kilt$handleLivingJumpEvent(float playerJumpPendingScale, Vec3 travelVector, CallbackInfo ci) {
        CommonHooks.onLivingJump(this);
    }
}
