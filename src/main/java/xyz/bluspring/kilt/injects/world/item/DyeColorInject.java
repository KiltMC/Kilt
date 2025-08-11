// TRACKED HASH: 790222e880a2fb721fe089f49d812d7025836f0a
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.ItemTagsInjection;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

@Mixin(DyeColor.class)
public class DyeColorInject implements DyeColorInjection {
    @Unique @Final @Mutable
    private TagKey<Item> tag;

    @CreateStatic
    private static DyeColor getColor(ItemStack stack) {
        return DyeColorInjection.getColor(stack);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initDyeTag(String string, int i, int id, String name, int textureDefuseColor, MapColor mapColor, int fireworkColor, int textColor, CallbackInfo ci) {
        this.tag = ItemTagsInjection.create(new ResourceLocation("forge", "dyes/" + name));
    }

    @Override
    public TagKey<Item> getTag() {
        return tag;
    }
}