package xyz.bluspring.kilt.mixin;

import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(WoodType.class)
public interface WoodTypeAccessor {
    @Invoker("<init>")
    static WoodType createWoodType(String string, BlockSetType setType) {
        throw new UnsupportedOperationException();
    }
}
