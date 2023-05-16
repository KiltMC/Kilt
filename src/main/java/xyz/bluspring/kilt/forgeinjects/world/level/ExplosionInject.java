package xyz.bluspring.kilt.forgeinjects.world.level;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(Explosion.class)
public class ExplosionInject {
    @Shadow @Final private double x;
    @Shadow @Final private double y;
    @Shadow @Final private double z;
    @Shadow @org.jetbrains.annotations.Nullable public Entity source;
    private Vec3 position;

    // implemented events: onExplosionDetonate

    @Inject(method = "<init>(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/entity/Entity;DDDF)V", at = @At("TAIL"))
    public void kilt$setPosition(Level level, Entity entity, double d, double e, double f, float g, CallbackInfo ci) {
        this.position = new Vec3(this.x, this.y, this.z);
    }

    public Vec3 getPosition() {
        return this.position;
    }

    @Nullable
    public Entity getExploder() {
        return this.source;
    }
}
