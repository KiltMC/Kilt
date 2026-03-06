package xyz.bluspring.kilt.injects.world.entity.ai.village.poi;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.world.poi.PoiStateSet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Set;

@Mixin(PoiType.class)
public abstract class PoiTypeInject {
    @ModifyExpressionValue(method = "<init>", at = @At(value = "INVOKE", target = "Ljava/util/Set;copyOf(Ljava/util/Collection;)Ljava/util/Set;"))
    private <E> Set<E> kilt$wrapAsStateSet(Set<E> original) {
        return (Set<E>) new PoiStateSet((Set<BlockState>) original);
    }
}
