package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.core.Holder;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@IfModLoaded("registrate-fabric")
@Mixin(Fluid.class)
public interface FluidAccessor {

    @Mutable
    @Accessor
    void setBuiltInRegistryHolder(Holder.Reference<Fluid> holder);

    @Invoker
    void callCreateFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder);

    @Mutable
    @Accessor
    void setStateDefinition(StateDefinition<Fluid, FluidState> stateDefinition);

}
