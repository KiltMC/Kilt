package xyz.bluspring.kilt.injects.world.entity.vehicle;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerInject extends AbstractMinecart {
    protected AbstractMinecartContainerInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "interact", at = @At("HEAD"), cancellable = true)
    private void kilt$callSuperInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        InteractionResult ret = super.interact(player, hand);

        if (ret.consumesAction())
            cir.setReturnValue(ret);
    }
}
