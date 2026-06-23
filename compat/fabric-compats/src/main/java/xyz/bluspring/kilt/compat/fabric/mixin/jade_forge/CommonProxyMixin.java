package xyz.bluspring.kilt.compat.fabric.mixin.jade_forge;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import snownee.jade.api.Accessor;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.util.CommonProxy;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Pseudo
@Mixin(value = CommonProxy.class, remap = false)
public abstract class CommonProxyMixin {
    @CreateStatic
    private static IItemHandler findItemHandler(Accessor<?> accessor) {
        if (accessor instanceof BlockAccessor blockAccessor) {
            return accessor.getLevel().getCapability(
                Capabilities.ItemHandler.BLOCK,
                blockAccessor.getPosition(),
                blockAccessor.getBlockState(),
                blockAccessor.getBlockEntity(),
                null);
        } else if (accessor instanceof EntityAccessor entityAccessor) {
            return entityAccessor.getEntity().getCapability(Capabilities.ItemHandler.ENTITY);
        }
        return null;
    }
}
