package xyz.bluspring.kilt.forgeinjects.data.loot;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.data.loot.BlockLootSubProviderInjection;

import java.util.Iterator;

@Mixin(BlockLootSubProvider.class)
public abstract class BlockLootSubProviderInject implements BlockLootSubProviderInjection {
    @Override
    public Iterable<Block> getKnownBlocks() {
        return BuiltInRegistries.BLOCK;
    }

    @WrapOperation(method = "generate(Ljava/util/function/BiConsumer;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/DefaultedRegistry;iterator()Ljava/util/Iterator;"))
    private Iterator<Block> kilt$useGetKnownBlocks(DefaultedRegistry<Block> instance, Operation<Iterator<Block>> original) {
        var knownBlocks = this.getKnownBlocks();

        // fallback to a modded one if needed.
        if (knownBlocks == BuiltInRegistries.BLOCK)
            return original.call(instance);

        return knownBlocks.iterator();
    }
}
