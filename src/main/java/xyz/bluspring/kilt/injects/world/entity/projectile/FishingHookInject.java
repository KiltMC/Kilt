package xyz.bluspring.kilt.injects.world.entity.projectile;

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.ItemFishedEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.HitResult;

@Mixin(FishingHook.class)
public abstract class FishingHookInject extends Projectile {
    public FishingHookInject(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "shouldStopFishing", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean kilt$checkForgeActions(ItemStack instance, Item item, Operation<Boolean> original) {
        return original.call(instance, item) || instance.canPerformAction(ItemAbilities.FISHING_ROD_CAST);
    }

    @WrapOperation(method = "checkCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/projectile/FishingHook;hitTargetOrDeflectSelf(Lnet/minecraft/world/phys/HitResult;)Lnet/minecraft/world/entity/projectile/ProjectileDeflection;"))
    public ProjectileDeflection kilt$checkHitResultFirst(FishingHook instance, HitResult hitResult, Operation<ProjectileDeflection> original) {
        if (hitResult.getType() == HitResult.Type.MISS || !EventHooks.onProjectileImpact((FishingHook) (Object) this, hitResult)) {
            // Kilt: on Neo this is onHit..... this seems like a bug.
            return original.call(instance, hitResult);
        } else {
            return null;
        }
    }

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/storage/loot/LootParams$Builder;create(Lnet/minecraft/world/level/storage/loot/parameters/LootContextParamSet;)Lnet/minecraft/world/level/storage/loot/LootParams;"), method = "retrieve")
    private LootParams kilt$addContextsToBuilder(LootParams.Builder instance, LootContextParamSet params, Operation<LootParams> original) {
        return original.call(instance.withParameter(LootContextParams.ATTACKING_ENTITY, this.getOwner()), params);
    }

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/List;iterator()Ljava/util/Iterator;", shift = At.Shift.BEFORE), method = "retrieve", cancellable = true)
    public void kilt$checkForgeEvent(ItemStack itemStack, CallbackInfoReturnable<Integer> cir, @Local List<ItemStack> list, @Share("kilt$fishEvent") LocalRef<ItemFishedEvent> eventLocalRef) {
        eventLocalRef.set(new ItemFishedEvent(list, this.onGround() ? 2 : 1, (FishingHook) (Object) this));
        NeoForge.EVENT_BUS.post(eventLocalRef.get());

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
