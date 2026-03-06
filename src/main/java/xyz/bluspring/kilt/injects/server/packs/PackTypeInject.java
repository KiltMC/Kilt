package xyz.bluspring.kilt.injects.server.packs;

import net.minecraft.server.packs.PackType;
import net.minecraft.util.StringRepresentable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(PackType.class)
public abstract class PackTypeInject implements StringRepresentable {
    @Override
    public String getSerializedName() {
        return ((Enum) (Object) this).name().toLowerCase();
    }
}
