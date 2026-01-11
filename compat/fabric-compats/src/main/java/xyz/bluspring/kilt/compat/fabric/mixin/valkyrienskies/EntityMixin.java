package xyz.bluspring.kilt.compat.fabric.mixin.valkyrienskies;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.FluidType;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;

@IfModLoaded("valkyrienskies")
@Mixin(value = Entity.class, priority = 1101)
public class EntityMixin {

    @Dynamic("Valkyrien Skies will return this boolean from updateFluidHeightAndDoFluidPushing and bypass ModifyReturnValue")
    @Shadow(remap = false)
    private boolean valkyrienskies$fluidPushRet;

    @Dynamic("If this is not null we are running updateFluidHeightAndDoFluidPushing in the shipyard")
    @Shadow(remap = false)
    private AABB valkyrienskies$fluidPushAABB;

    @Unique
    private FluidType kilt$vsFluidType = null;

    @Unique
    private Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>> kilt$vsInterimCalcs = null;

    // Not technically necessary, but gives us some protection in case some other mod does something weird.
    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"))
    private void kilt$resetOnStart(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir) {
        kilt$vsFluidType = null;
        kilt$vsInterimCalcs = null;
    }

    // VS calls updateFluidHeightAndDoFluidPushing recursively on the shipyard,
    // we need to pass the fluid type from that one out.
    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D", ordinal = 0),
            method = "updateFluidHeightAndDoFluidPushing",
            order = 999
    )
    private void kilt$fixShipyardReturnValue(
            TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir,
            @Share(value = "fluidType", namespace = Kilt.MOD_ID) LocalRef<FluidType> fluidTypeRef,
            @Share(value = "interimCalcs", namespace = Kilt.MOD_ID) LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs
    ) {
        if (valkyrienskies$fluidPushAABB != null) {
            kilt$vsFluidType = fluidTypeRef.get();
            kilt$vsInterimCalcs = interimCalcs.get();
        }
    }

    @Dynamic("Implemented in EntityInject")
    @Shadow
    private boolean kilt$isCorrectFluidTypeForTag(
            TagKey<Fluid> fluidTag, FluidType fluidType,
            Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>> interimCalcs,
            boolean defaultReturn
    ) {
        throw new AssertionError();
    }

    // Valkyrien Skies uses cancellable instead of ModifyVariable, so this hack is necessary.
    // https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/206b5c9eb1ebf25bdd23e998f66dc2957aa6dfc3/fabric/src/main/java/org/valkyrienskies/mod/fabric/mixin/feature/water_in_ships_entity/MixinEntity.java#L179
    @Inject(
            method = "updateFluidHeightAndDoFluidPushing",
            at = @At(value = "RETURN", ordinal = 1),
            order = 999
    )
    private void kilt$fixVSReturnValue(
            TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir,
            @Share(value = "fluidType", namespace = Kilt.MOD_ID) LocalRef<FluidType> fluidTypeRef,
            @Share(value = "interimCalcs", namespace = Kilt.MOD_ID) LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs
    ) {
        valkyrienskies$fluidPushRet = kilt$isCorrectFluidTypeForTag(
                fluidTag,
                kilt$vsFluidType != null ? kilt$vsFluidType : fluidTypeRef.get(),
                kilt$vsInterimCalcs != null ? kilt$vsInterimCalcs : interimCalcs.get(),
                valkyrienskies$fluidPushRet
        );

        kilt$vsFluidType = null;
        kilt$vsInterimCalcs = null;
    }

}
