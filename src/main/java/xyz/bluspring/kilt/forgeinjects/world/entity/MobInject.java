// TRACKED HASH: 509c9dc95ab8642482abd57150c1cc2f0c792d50
package xyz.bluspring.kilt.forgeinjects.world.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.MobInjection;

@Mixin(Mob.class)
public abstract class MobInject extends LivingEntity implements MobInjection {
    @Nullable @Unique private MobSpawnType spawnType;
    @Unique private boolean spawnCancelled = false;

    protected MobInject(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "finalizeSpawn", at = @At("TAIL"))
    private void kilt$setSpawnType(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData spawnData, CompoundTag dataTag, CallbackInfoReturnable<SpawnGroupData> cir) {
        this.spawnType = reason;
    }

    @Override
    public boolean isSpawnCancelled() {
        return this.spawnCancelled;
    }

    @Override
    public void setSpawnCancelled(boolean cancel) {
        if (this.isAddedToWorld()) {
            throw new UnsupportedOperationException("Late invocations of Mob#setSpawnCancelled are not permitted.");
        }

        this.spawnCancelled = cancel;
    }

    @Override
    public MobSpawnType getSpawnType() {
        return this.spawnType;
    }
}