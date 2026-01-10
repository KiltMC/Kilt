package xyz.bluspring.kilt.compat.fabric.mixin.valkyrienskies;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import io.github.fabricators_of_create.porting_lib.fluids.PortingLibFluids;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@IfModLoaded("valkyrienskies")
@Mixin(value = Entity.class, priority = 999)
public class EntityMixin {

    @Unique
    private static final String KILT$ENTITY_INJECT = "xyz.bluspring.kilt.forgeinjects.world.entity.EntityInject";

    @Unique
    private static Field kilt$vsFluidAABBField = null;

    @Unique
    private static Field kilt$vsFluidPushRetField = null;

    // Field name is dynamically decorated by mixinextras.
    // This was the best way I could think of to find it reliably.
    @Unique
    private static Field kilt$getVSFluidPushRetField() {
        if (kilt$vsFluidPushRetField == null) {
            for (var field : Entity.class.getDeclaredFields()) {
                if (field.getType() == Boolean.TYPE && field.getName().endsWith("valkyrienskies$fluidPushRet")) {
                    kilt$vsFluidPushRetField = field;
                    field.setAccessible(true);
                    break;
                }
            }
        }
        return kilt$vsFluidPushRetField;
    }

    @Unique
    private static Field kilt$getVsFluidAABBField() {
        if (kilt$vsFluidAABBField == null) {
            for (var field : Entity.class.getDeclaredFields()) {
                if (field.getType() == AABB.class && field.getName().endsWith("valkyrienskies$fluidPushAABB")) {
                    kilt$vsFluidAABBField = field;
                    field.setAccessible(true);
                    break;
                }
            }
        }
        return kilt$vsFluidAABBField;
    }

    @Unique
    private FluidType kilt$vsFluidType = null;

    @Unique
    private Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>> kilt$vsInterimCalcs = null;

    // Not technically necessary, but gives us some protection in case some other mod does something weird.
    @Inject(method = "updateFluidHeightAndDoFluidPushing", at = @At("HEAD"))
    private void resetOnStart(TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir) {
        kilt$vsFluidType = null;
        kilt$vsInterimCalcs = null;
    }

    // VS calls updateFluidHeightAndDoFluidPushing recursively on the shipyard,
    // we need to pass the fluid type from that one out.
    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/phys/Vec3;length()D", ordinal = 0),
            method = "updateFluidHeightAndDoFluidPushing"
    )
    private void kilt$fixShipyardReturnValue(
            TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir,
            @Share(value = "fluidType", namespace = KILT$ENTITY_INJECT) LocalRef<FluidType> fluidTypeRef,
            @Share(value = "interimCalcs", namespace = KILT$ENTITY_INJECT) LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs
    ) {
        try {
            var valkFluidAABB = kilt$getVsFluidAABBField();
            if (valkFluidAABB != null) {
                var fluidAABB = (AABB) valkFluidAABB.get(this);
                if (fluidAABB != null) {
                    kilt$vsFluidType = fluidTypeRef.get();
                    kilt$vsInterimCalcs = interimCalcs.get();
                }
            }
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }


    // Valkyrien Skies uses cancellable instead of ModifyVariable, so this hack is necessary.
    // https://github.com/ValkyrienSkies/Valkyrien-Skies-2/blob/206b5c9eb1ebf25bdd23e998f66dc2957aa6dfc3/fabric/src/main/java/org/valkyrienskies/mod/fabric/mixin/feature/water_in_ships_entity/MixinEntity.java#L179
    @Inject(
            method = "updateFluidHeightAndDoFluidPushing",
            at = @At(value = "RETURN", ordinal = 1)
    )
    private void kilt$fixVSReturnValue(
            TagKey<Fluid> fluidTag, double motionScale, CallbackInfoReturnable<Boolean> cir,
            @Share(value = "fluidType", namespace = KILT$ENTITY_INJECT) LocalRef<FluidType> fluidTypeRef,
            @Share(value = "interimCalcs", namespace = KILT$ENTITY_INJECT) LocalRef<Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>>> interimCalcs
    ) {
        try {
            var valkPushRet = kilt$getVSFluidPushRetField();
            if (valkPushRet != null) {
                var pushRet = (boolean) valkPushRet.get(this);
                var correctPushRet = isCorrectFluidTag(
                        fluidTag,
                        kilt$vsFluidType != null ? kilt$vsFluidType : fluidTypeRef.get(),
                        kilt$vsInterimCalcs != null ? kilt$vsInterimCalcs : interimCalcs.get(),
                        pushRet
                );
                if (pushRet != correctPushRet) {
                    valkPushRet.set(this, correctPushRet);
                }
            }

            kilt$vsFluidType = null;
            kilt$vsInterimCalcs = null;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private boolean isCorrectFluidTag(
            TagKey<Fluid> fluidTag, FluidType fluidType,
            Object2ObjectMap<FluidType, MutableTriple<Double, Vec3, Integer>> interimCalcs,
            boolean defaultReturn
    ) {
        if (fluidTag == FluidTags.WATER) {
            if (fluidType != null && !fluidType.isAir())
                return fluidType == ForgeMod.WATER_TYPE.get() || fluidType == PortingLibFluids.WATER_TYPE;
            else if (interimCalcs != null)
                return interimCalcs.containsKey(ForgeMod.WATER_TYPE.get());
        } else if (fluidTag == FluidTags.LAVA) {
            if (fluidType != null && !fluidType.isAir())
                return fluidType == ForgeMod.LAVA_TYPE.get() || fluidType == PortingLibFluids.LAVA_TYPE;
            else if (interimCalcs != null)
                return interimCalcs.containsKey(ForgeMod.LAVA_TYPE.get());
        }
        return defaultReturn;
    }

}
