package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.MinecartCommandBlock;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecartCommandBlock.class)
public abstract class MinecartCommandBlockInject extends AbstractMinecart {
    protected MinecartCommandBlockInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void kilt$callSuperInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        var ret = super.interact(player, hand);

        if (ret.consumesAction())
            cir.setReturnValue(ret);
    }
}
