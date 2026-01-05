package xyz.bluspring.kilt.compat.fabric.mixin.jei;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import mezz.jei.api.fabric.ingredients.fluids.IJeiFluidIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;

import java.util.Optional;

@IfModLoaded("jei")
@Mixin(FluidStack.class)
// We use the annotation instead of traditional interface injection since the jei getTag function collides with the FluidStack one.
// The other methods are overriden implicitly.
@Implements(value = @Interface(iface = IJeiFluidIngredient.class, prefix = "kilt$jei$"))
public abstract class FluidStackMixin {

    @Shadow
    public abstract CompoundTag getTag();

    @Intrinsic(displace = true)
    public @NotNull Optional<CompoundTag> kilt$jei$getTag() {
        return Optional.ofNullable(getTag());
    }

}
