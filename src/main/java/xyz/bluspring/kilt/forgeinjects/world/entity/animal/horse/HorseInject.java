package xyz.bluspring.kilt.forgeinjects.world.entity.animal.horse;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Horse.class)
public abstract class HorseInject extends AbstractHorse {
    @Shadow public abstract boolean isArmor(ItemStack stack);

    protected HorseInject(EntityType<? extends AbstractHorse> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "playGallopSound", at = @At("TAIL"))
    private void kilt$handleArmorTick(SoundType soundType, CallbackInfo ci) {
        var stack = this.inventory.getItem(1);

        if (isArmor(stack)) {
            stack.onHorseArmorTick(this.level(), this);
        }
    }
}
