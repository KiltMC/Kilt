package xyz.bluspring.kilt.forgeinjects.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeableHorseArmorItem;
import net.minecraft.world.item.HorseArmorItem;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

@Mixin(DyeableHorseArmorItem.class)
public abstract class DyeableHorseArmorItemInject extends HorseArmorItem {
    public DyeableHorseArmorItemInject(int protection, String identifier, Properties properties) {
        super(protection, identifier, properties);
    }

    @CreateInitializer
    public DyeableHorseArmorItemInject(int protection, ResourceLocation identifier, Properties properties) {
        super(protection, identifier.toString(), properties);
    }
}
