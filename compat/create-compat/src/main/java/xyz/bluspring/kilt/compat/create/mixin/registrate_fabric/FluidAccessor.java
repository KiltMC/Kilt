package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@IfModLoaded("registrate-fabric")
@Mixin(Fluid.class)
public interface FluidAccessor {

    @Mutable
    @Accessor
    void setBuiltInRegistryHolder(Holder.Reference<Fluid> holder);

}
