package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.DataMapHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(HoneycombItem.class)
public abstract class HoneycombItemInject extends Item {
    public HoneycombItemInject(Properties properties) {
        super(properties);
    }

    @ModifyExpressionValue(method = "getWaxed", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ofNullable(Ljava/lang/Object;)Ljava/util/Optional;"))
    private static <T> Optional<T> kilt$tryGetFromDataMap(Optional<T> original, @Local(argsOnly = true) BlockState state) {
        if (original.isEmpty()) {
            return (Optional<T>) Optional.ofNullable(DataMapHooks.getBlockWaxed(state.getBlock()));
        }

        return original;
    }
}
