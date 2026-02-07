package xyz.bluspring.kilt.injects.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.extensions.IBlockEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.level.block.entity.BlockEntityInjection;

@Extends(AttachmentHolder.class)
@Mixin(BlockEntity.class)
public abstract class BlockEntityInject implements BlockEntityInjection, IBlockEntityExtension {

}
