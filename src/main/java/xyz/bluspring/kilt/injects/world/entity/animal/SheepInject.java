package xyz.bluspring.kilt.injects.world.entity.animal;

import net.minecraft.world.entity.animal.Sheep;
import net.neoforged.neoforge.common.IShearable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Sheep.class)
public abstract class SheepInject implements IShearable {
    // Kilt: Handled by Porting Lib
}
