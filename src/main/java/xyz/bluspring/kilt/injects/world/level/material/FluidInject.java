// TRACKED HASH: 8052166e9529780ad90ee8b00eda7d0ee8ffc2ca
package xyz.bluspring.kilt.injects.world.level.material;

import java.util.function.Function;

import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.extensions.IFluidExtension;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.material.FluidInjection;
import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.FluidWorkaround;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.InsideBlockEffectType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

@Implements(@Interface(iface = FluidWorkaround.class, prefix = "kilt$i$"))
@Mixin(Fluid.class)
public abstract class FluidInject implements IFluidExtension, FluidWorkaround, FluidInjection {
    @Unique private @Nullable FluidResource defaultResource;

    @Inject(method = "entityInside", at = @At("HEAD"))
    private void kilt$checkCanFluidExtinguish(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier, CallbackInfo ci) {
        if (entity.canFluidExtinguish(this.getFluidType())) {
            effectApplier.apply(InsideBlockEffectType.EXTINGUISH);
        }
    }

    // Kilt: skipping toString impl

    @Unique private FluidType forgeFluidType;

    @NotNull
    @Override
    public FluidType getFluidType() {
        // Kilt: We pray that this works
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(this.getClass(), Fluid.class, "getFluidType", FluidType.class)) {
            return this.getFluidType();
        }

        if (forgeFluidType == null)
            forgeFluidType = CommonHooks.getVanillaFluidType((Fluid) (Object) this);

        return forgeFluidType;
    }

    @Override
    public FluidResource computeDefaultResource(Function<Fluid, FluidResource> resourceConstructor) {
        if (this.defaultResource == null)
            this.defaultResource = resourceConstructor.apply((Fluid) (Object) this);

        return this.defaultResource;
    }

    @Intrinsic
    public FluidType kilt$i$getFluidType() {
        return this.getFluidType();
    }
}
