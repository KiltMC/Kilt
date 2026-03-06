package xyz.bluspring.kilt.injects.world.item.component;

import net.minecraft.world.item.component.FireworkExplosion;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IndexedEnum;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import net.neoforged.fml.common.asm.enumextension.NetworkedEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

@Mixin(FireworkExplosion.class)
public abstract class FireworkExplosionInject {
    @IndexedEnum
    @NamedEnum(1)
    @NetworkedEnum(NetworkedEnum.NetworkCheck.BIDIRECTIONAL)
    @Mixin(FireworkExplosion.Shape.class)
    public abstract static class ShapeInject {
        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended(FireworkExplosion.Shape.class);
        }
    }
}
