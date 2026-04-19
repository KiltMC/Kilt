// TRACKED HASH: c1c579e966fc78d57072df71b93acdf023e75c9e
package xyz.bluspring.kilt.injects.world.entity.vehicle;

import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum;
import net.neoforged.neoforge.common.extensions.IBoatExtension;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.vehicle.BoatInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;

@Mixin(Boat.class)
public abstract class BoatInject extends Entity implements IBoatExtension {
    @Shadow private int lerpSteps;
    @Shadow private double lerpX;
    @Shadow private double lerpY;
    @Shadow private double lerpZ;
    @Shadow private double lerpYRot;
    @Shadow private double lerpXRot;
    @Shadow public abstract Boat.Type getVariant();

    public BoatInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "getDropItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/item/Items;OAK_BOAT:Lnet/minecraft/world/item/Item;", opcode = Opcodes.GETSTATIC))
    private Item kilt$tryReturnCustomChestBoatItem(Operation<Item> original) {
        try {
            if (this.getVariant() != Boat.Type.OAK) {
                return this.getVariant().kilt$getBoatItem().get();
            }
        } catch (Throwable ignored) {}

        return original.call();
    }

    @WrapOperation(method = {"getWaterLevelAbove", "checkInWater", "isUnderwater"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatingInFluid(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return this.canBoatInFluid(instance);
        }

        return original.call(instance, tag);
    }

    @WrapOperation(method = "getGroundFriction", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getFriction()F"))
    public float kilt$useForgeFriction(Block instance, Operation<Float> original, @Local BlockState state, @Local BlockPos.MutableBlockPos mutableBlockPos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Block.class, "getFriction", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getFriction(state, this.level(), mutableBlockPos, (Boat) (Object) this);
        }

        return original.call(instance);
    }

    @WrapOperation(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfBoatIsInFluidBeforeState(FluidState instance, TagKey<Fluid> tag, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return !this.canBoatInFluid(instance);
        }

        return original.call(instance, tag);
    }

    @WrapOperation(method = "canAddPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/vehicle/Boat;isEyeInFluid(Lnet/minecraft/tags/TagKey;)Z"))
    public boolean kilt$checkIfCanBoatBeforePassenger(Boat instance, TagKey tagKey, Operation<Boolean> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), Boat.class, "canBoatInFluid", FluidState.class)) {
            return this.canBoatInFluid(this.getEyeInFluidType());
        }

        return original.call(instance, tagKey);
    }

    // a forge fix, might as well
    @Override
    protected void addPassenger(Entity passenger) {
        super.addPassenger(passenger);
        if (this.isControlledByLocalInstance() && this.lerpSteps > 0) {
            this.lerpSteps = 0;
            this.absMoveTo(this.lerpX, this.lerpY, this.lerpZ, (float) this.lerpYRot, (float) this.lerpXRot);
        }
    }

    @NamedEnum(1)
    @NetworkedEnum(NetworkedEnum.NetworkCheck.CLIENTBOUND)
    @Mixin(Boat.Type.class)
    public abstract static class TypeInject implements BoatInjection.TypeInjection {
        @Shadow @Final private Block planks;
        @Shadow @Final public static Boat.Type BAMBOO;

        @Unique private Supplier<Block> planksSupplier;
        @Unique Supplier<Item> boatItem = () -> Items.AIR;
        @Unique Supplier<Item> chestBoatItem = () -> Items.AIR;
        @Unique private Supplier<Item> stickItem = () -> Items.STICK;
        @Unique private boolean raft;

        private TypeInject(Block planks, String name) {}

        @CreateInitializer
        private TypeInject(Block planks, String name, boolean raft) {
            this(planks, name);
            this.raft = raft;
        }

        @CreateInitializer
        private TypeInject(Supplier<Block> planks, String name, Supplier<Item> boatItem, Supplier<Item> chestBoatItem, Supplier<Item> stickItem, boolean raft) {
            this(Blocks.AIR, name);
            this.planksSupplier = planks;
            this.boatItem = boatItem;
            this.chestBoatItem = chestBoatItem;
            this.stickItem = stickItem;
            this.raft = raft;
        }

        @Inject(method = "getPlanks", at = @At("HEAD"), cancellable = true)
        private void kilt$usePlanksSupplierIfPossible(CallbackInfoReturnable<Block> cir) {
            if (this.planks == Blocks.AIR) {
                cir.setReturnValue(this.planksSupplier.get());
            }
        }

        @Override
        public Item getSticks() {
            return this.stickItem.get();
        }

        @Override
        public boolean isRaft() {
            return this.raft;
        }

        @Override
        public void kilt$setRaft(boolean raft) {
            this.raft = raft;
        }

        @Override
        public Supplier<Item> kilt$getBoatItem() {
            return this.boatItem;
        }

        @Override
        public Supplier<Item> kilt$getChestBoatItem() {
            return this.chestBoatItem;
        }

        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(Boat.Type.class);
        }

        @Inject(method = "<clinit>", at = @At("TAIL"))
        private static void kilt$markBambooAsRaft(CallbackInfo ci) {
            BAMBOO.kilt$setRaft(true);
        }
    }
}