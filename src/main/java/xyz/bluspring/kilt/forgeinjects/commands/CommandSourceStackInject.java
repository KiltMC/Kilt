package xyz.bluspring.kilt.forgeinjects.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.extensions.IForgeCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackInject implements IForgeCommandSourceStack {
}
