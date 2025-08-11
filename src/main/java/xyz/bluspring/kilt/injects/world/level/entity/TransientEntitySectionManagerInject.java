package xyz.bluspring.kilt.injects.world.level.entity;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalLongRef;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.entity.EntityAccess;
import net.minecraft.world.level.entity.EntitySection;
import net.minecraft.world.level.entity.TransientEntitySectionManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TransientEntitySectionManager.class)
public abstract class TransientEntitySectionManagerInject {
    @Mixin(TransientEntitySectionManager.Callback.class)
    public abstract static class CallbackInject {
        @Shadow
        private long currentSectionKey;
        @Unique
        private Entity realEntity;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$storeRealEntity(TransientEntitySectionManager persistentEntitySectionManager, EntityAccess entity, long currentSectionKey, EntitySection currentSection, CallbackInfo ci) {
            this.realEntity = entity instanceof Entity e ? e : null;
        }

        @Inject(method = "onMove", at = @At("HEAD"))
        private void kilt$storeOldSectionKey(CallbackInfo ci, @Share("oldSectionKey") LocalLongRef oldSectionKey) {
            oldSectionKey.set(this.currentSectionKey);
        }

        // Kilt: handled via Architectury
        /*@Inject(method = "onMove", at = @At("TAIL"))
        private void kilt$callForgeEntityEnterSection(CallbackInfo ci, @Share("oldSectionKey") LocalLongRef oldSectionKey) {
            if (oldSectionKey.get() != this.currentSectionKey && this.realEntity != null)
                CommonHooks.onEntityEnterSection(this.realEntity, oldSectionKey.get(), this.currentSectionKey);
        }*/
    }
}
