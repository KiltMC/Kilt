package xyz.bluspring.kilt.injects.world.level.entity;

import java.util.concurrent.atomic.AtomicBoolean;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.entity.PersistentEntitySectionManagerInjection;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;

@Mixin(PersistentEntitySectionManager.class)
public abstract class PersistentEntitySectionManagerInject<T extends EntityAccess> implements PersistentEntitySectionManagerInjection<T> {
    @Shadow protected abstract boolean addEntity(T entity, boolean worldGenSpawned);

    @Unique private final AtomicBoolean kilt$callWithoutEvent = new AtomicBoolean(false);

    @Override
    public boolean addNewEntityWithoutEvent(T entity) {
        return this.addEntityWithoutEvent(entity, false);
    }

    @Override
    public void kilt$markWithoutEvent() {
        this.kilt$callWithoutEvent.set(true);
    }

    @Inject(method = "addEntity", at = @At("HEAD"), cancellable = true)
    private void kilt$callEntityJoinLevelEvent(T entity, boolean worldGenSpawned, CallbackInfoReturnable<Boolean> cir) {
        if (this.kilt$callWithoutEvent.getAndSet(false) && entity instanceof Entity e && NeoForge.EVENT_BUS.post(new EntityJoinLevelEvent(e, e.level(), worldGenSpawned)).isCanceled())
            cir.setReturnValue(false);
    }

    private boolean addEntityWithoutEvent(T entity, boolean worldGenSpawned) {
        this.kilt$callWithoutEvent.set(true);
        return this.addEntity(entity, worldGenSpawned);
    }

    @Inject(method = {"method_31857", "method_31863", "method_31864"}, at = @At("TAIL"))
    private void kilt$callEntityAddedToWorld(EntityAccess entityAccess, CallbackInfo ci) {
        if (entityAccess instanceof Entity entity)
            entity.onAddedToLevel();
    }

    @Mixin(PersistentEntitySectionManager.Callback.class)
    public static abstract class CallbackInject {
        @Shadow private long currentSectionKey;
        @Unique private Entity realEntity;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$storeRealEntity(PersistentEntitySectionManager persistentEntitySectionManager, EntityAccess entity, long currentSectionKey, EntitySection currentSection, CallbackInfo ci) {
            this.realEntity = entity instanceof Entity e ? e : null;
        }

        @Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/EntitySection;add(Lnet/minecraft/world/level/entity/EntityAccess;)V"))
        private void kilt$storeOldSectionKey(CallbackInfo ci, @Share("oldSectionKey") LocalLongRef oldSectionKey) {
            oldSectionKey.set(this.currentSectionKey);
        }

        // Kilt: handled via Porting Lib
        /*@Inject(method = "onMove", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/entity/PersistentEntitySectionManager$Callback;updateStatus(Lnet/minecraft/world/level/entity/Visibility;Lnet/minecraft/world/level/entity/Visibility;)V", shift = At.Shift.AFTER))
        private void kilt$callForgeEntityEnterSection(CallbackInfo ci, @Share("oldSectionKey") LocalLongRef oldSectionKey) {
            if (this.realEntity != null)
                CommonHooks.onEntityEnterSection(this.realEntity, oldSectionKey.get(), this.currentSectionKey);
        }*/
    }
}
