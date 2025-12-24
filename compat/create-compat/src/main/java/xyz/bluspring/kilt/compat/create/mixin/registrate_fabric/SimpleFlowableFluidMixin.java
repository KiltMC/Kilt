package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.compat.create.extensions.SimpleFlowableFluidPropertiesExtension;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.Extends;

import java.util.function.Supplier;

@IfModLoaded("registrate-fabric")
@Mixin(SimpleFlowableFluid.class)
@Extends(value = ForgeFlowingFluid.class, override = true)
public abstract class SimpleFlowableFluidMixin extends Fluid {
    protected SimpleFlowableFluidMixin(SimpleFlowableFluid.Properties properties) {}

//    @Unique
//    private static SimpleFlowableFluid.Properties mapProperties(ForgeFlowingFluid.Properties properties) {
//        SimpleFlowableFluid.Properties simpleFluidProperties = new SimpleFlowableFluid.Properties(
//                properties.kilt$getStill(), properties.kilt$getFlowing()
//        );
//        simpleFluidProperties.bucket(properties.kilt$getBucket());
//        simpleFluidProperties.block(properties.kilt$getBlock());
//        simpleFluidProperties.levelDecreasePerBlock(properties.kilt$getLevelDecreasePerBlock());
//        simpleFluidProperties.blastResistance(properties.kilt$getBlastResistance());
//        simpleFluidProperties.tickRate(properties.kilt$getTickRate());
//
//        return simpleFluidProperties;
//    }

    @CreateInitializer
    public SimpleFlowableFluidMixin(ForgeFlowingFluid.Properties properties) {
        kilt$mixin$superCall(properties);
    }

    private void kilt$mixin$superCall(ForgeFlowingFluid.Properties properties) {}

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$setProperties(SimpleFlowableFluid.Properties properties, CallbackInfo ci) {
        var forgeFlowingFluid = (ForgeFlowingFluidAccessor)this;
        var propertiesAccessor = (SimpleFlowableFluidPropertiesAccessor) properties;
        forgeFlowingFluid.kilt$flowing(propertiesAccessor.getFlowing());
        forgeFlowingFluid.kilt$still(propertiesAccessor.getStill());
        forgeFlowingFluid.kilt$bucket(propertiesAccessor.getBucket());
        forgeFlowingFluid.kilt$block(propertiesAccessor.getBlock());
        forgeFlowingFluid.kilt$slopeFindDistance(propertiesAccessor.getFlowSpeed());
        forgeFlowingFluid.kilt$levelDecreasePerBlock(propertiesAccessor.getLevelDecreasePerBlock());
        forgeFlowingFluid.kilt$explosionResistance(propertiesAccessor.getBlastResistance());
        forgeFlowingFluid.kilt$tickRate(propertiesAccessor.getTickRate());
        @Nullable
        var fluidType = ((SimpleFlowableFluidPropertiesExtension) properties).kilt$getFluidType();
        if (fluidType != null) {
            forgeFlowingFluid.kilt$fluidType(fluidType);
        } else {
//            forgeFlowingFluid.kilt$fluidType(() -> ForgeHooks.getVanillaFluidType((Fluid) (Object) this));
        }
    }

    @IfModLoaded("registrate-fabric")
    @Mixin(SimpleFlowableFluid.Properties.class)
    @Extends(value = ForgeFlowingFluid.Properties.class, override = true)
    public static class PropertiesMixin implements SimpleFlowableFluidPropertiesExtension {
        @Unique
        private Supplier<? extends FluidType> kilt$fluidType;

        @Override
        public @Nullable Supplier<? extends @NotNull FluidType> kilt$getFluidType() {
            return kilt$fluidType;
        }

        @Override
        public void kilt$setFluidType(@NotNull Supplier<? extends @NotNull FluidType> fluidType) {
            kilt$fluidType = fluidType;
        }
    }
}

