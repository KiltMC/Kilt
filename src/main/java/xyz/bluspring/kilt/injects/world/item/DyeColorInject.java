package xyz.bluspring.kilt.injects.world.item;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.tags.ItemTagsInjection;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.MapColor;

@Mixin(DyeColor.class)
public abstract class DyeColorInject implements DyeColorInjection {
    @Unique private TagKey<Item> dyesTag;
    @Unique private TagKey<Item> dyedTag;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createItemTags(String id, int name, int textureDiffuseColor, String mapColor, int fireworkColor, MapColor textColor, int par7, int par8, CallbackInfo ci) {
        this.dyesTag = ItemTagsInjection.create(Identifier.fromNamespaceAndPath("c", "dyes/" + name));
        this.dyedTag = ItemTagsInjection.create(Identifier.fromNamespaceAndPath("c", "dyed/" + name));
    }

    @Override
    public TagKey<Item> getTag() {
        return this.dyesTag;
    }

    @Override
    public TagKey<Item> getDyedTag() {
        return dyedTag;
    }

    @CreateStatic
    private static DyeColor getColor(ItemStack stack) {
        return DyeColorInjection.getColor(stack);
    }
}
