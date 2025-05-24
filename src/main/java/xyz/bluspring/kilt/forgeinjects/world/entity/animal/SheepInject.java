package xyz.bluspring.kilt.forgeinjects.world.entity.animal;

import net.minecraft.world.entity.animal.Sheep;
import net.minecraftforge.common.IForgeShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Sheep.class)
public abstract class SheepInject implements IForgeShearable {
    // Kilt: Handled by Porting Lib
}
