package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireworkRocketEntity.class)
public abstract class FireworkRocketEntityInject extends Projectile {
    public FireworkRocketEntityInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Intrinsic
    @Override
    protected void onHit(HitResult result) {
        if (result.getType() == HitResult.Type.MISS || !ForgeEventFactory.onProjectileImpact(this, result)) {
            super.onHit(result);
        }
    }
}
