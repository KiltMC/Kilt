package xyz.bluspring.kilt.injects.world.entity.raid;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.raid.RaidRaiderTypeInjection;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.entity.raid.Raider;

@Mixin(Raid.class)
public abstract class RaidInject {
    @WrapOperation(method = "spawnGroup", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/raid/Raid$RaiderType;entityType:Lnet/minecraft/world/entity/EntityType;"))
    private EntityType<? extends Raider> kilt$tryUseSupplier(Raid.RaiderType instance, Operation<EntityType<? extends Raider>> original) {


        return original.call(instance);
    }

    @Mixin(Raid.RaiderType.class)
    public abstract static class RaiderTypeInject implements RaidRaiderTypeInjection {
        @Unique Supplier<EntityType<? extends Raider>> entityTypeSupplier;
        @Unique boolean kilt$isNeo;

        @Inject(method = "<init>", at = @At("TAIL"))
        private void kilt$initTypeSupplier(String string, int i, EntityType entityType, int[] spawnsPerWaveBeforeBonus, CallbackInfo ci) {
            this.entityTypeSupplier = () -> entityType;
        }

        private RaiderTypeInject(EntityType<? extends Raider> entityType, int[] spawnsPerWave) {
        }

        @CreateInitializer
        private RaiderTypeInject(Supplier<EntityType<? extends Raider>> entityTypeSupplier, int[] spawnsPerWave) {
            this((EntityType<? extends Raider>) null, spawnsPerWave);
            this.entityTypeSupplier = entityTypeSupplier;
        }

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(Raid.RaiderType.class);
        }

        @Override
        public Supplier<EntityType<? extends Raider>> kilt$getEntityTypeSupplier() {
            return this.entityTypeSupplier;
        }

        @Override
        public boolean kilt$isNeo() {
            return this.kilt$isNeo;
        }
    }
}
