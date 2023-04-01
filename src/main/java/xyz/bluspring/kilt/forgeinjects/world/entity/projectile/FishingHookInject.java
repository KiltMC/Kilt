package xyz.bluspring.kilt.forgeinjects.world.entity.projectile;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(FishingHook.class)
public abstract class FishingHookInject extends Projectile {
    @Shadow private boolean biting;

    public FishingHookInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @Redirect(method = "shouldStopFishing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean kilt$checkForgeActions(ItemStack instance, Item item) {
        return instance.canPerformAction(ToolActions.FISHING_ROD_CAST);
    }

    @Redirect(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    public void kilt$checkHitResultFirst(FishingHook instance, HitResult hitResult) {
        if (hitResult.getType() == HitResult.Type.MISS || ForgeEventFactory.onProjectileImpact((FishingHook) (Object) this, hitResult))
            onHit(hitResult);
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z"), method = "catchingFish")
    public boolean kilt$checkLiquidState(BlockState instance, Block block) {
        return instance.getMaterial() == Material.WATER;
    }

    @ModifyVariable(at = @At("STORE"), method = "retrieve")
    private LootContext.Builder kilt$addContextsToBuilder(LootContext.Builder builder) {
        return builder.withParameter(LootContextParams.KILLER_ENTITY, this.getOwner()).withParameter(LootContextParams.THIS_ENTITY, (FishingHook) (Object) this);
    }

    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/advancements/critereon/FishingRodHookedTrigger;trigger(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/projectile/FishingHook;Ljava/util/Collection;)V", shift = At.Shift.BEFORE), method = "retrieve", cancellable = true)
    public void kilt$checkForgeEvent(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> list, @Share("kilt$fishEvent") LocalRef<ItemFishedEvent> eventLocalRef) {
        eventLocalRef.set(new ItemFishedEvent(list, this.biting ? 2 : 1, (FishingHook) (Object) this));
        MinecraftForge.EVENT_BUS.post(eventLocalRef.get());

        if (eventLocalRef.get().isCanceled()) {
            this.discard();
            cir.setReturnValue(eventLocalRef.get().getRodDamage());
        }
    }

    @Inject(at = @At("RETURN"), method = "retrieve", cancellable = true)
    public void kilt$returnEventRodDamage(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @Share("kilt$fishEvent") LocalRef<ItemFishedEvent> eventLocalRef) {
        if (eventLocalRef.get() != null)
            cir.setReturnValue(eventLocalRef.get().getRodDamage());
    }
}
