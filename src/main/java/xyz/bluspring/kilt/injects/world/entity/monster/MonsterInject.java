package xyz.bluspring.kilt.injects.world.entity.monster;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Monster.class)
public abstract class MonsterInject extends PathfinderMob {
    protected MonsterInject(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReturnValue(method = "getProjectile", at = @At("RETURN"))
    private ItemStack kilt$tryGetForgeProjectile(ItemStack original, @Local(argsOnly = true) ItemStack stack) {
        return CommonHooks.getProjectile(this, stack, original);
    }
}
