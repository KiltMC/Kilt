// TRACKED HASH: 0770f54a29d8a50372a2a26c9e0c7652bc5fa502
package xyz.bluspring.kilt.injects.world.entity;

import com.mojang.serialization.Codec;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.entity.MobCategoryInjection;

@NamedEnum
@Mixin(MobCategory.class)
public abstract class MobCategoryInject implements MobCategoryInjection, IExtensibleEnum {
    @Shadow @Final @Mutable
    public static Codec<MobCategory> CODEC;

    @Shadow public abstract String getName();

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(MobCategory.class);
    }
}