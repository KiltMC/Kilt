package xyz.bluspring.kilt.injects.world.level.block.entity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.block.entity.BlockEntityTypeInjection;

import java.util.Collections;
import java.util.Set;

@Mixin(BlockEntityType.class)
public abstract class BlockEntityTypeInject<T extends BlockEntity> implements BlockEntityTypeInjection<T> {
    @Shadow @Final private Set<Block> validBlocks;

    @Override
    public Set<Block> getValidBlocks() {
        return Collections.unmodifiableSet(this.validBlocks);
    }
}
