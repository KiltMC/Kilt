// TRACKED HASH: a1abe0905490e921e76982419057b041bcbc9268
package xyz.bluspring.kilt.forgeinjects.commands;

import net.minecraft.commands.CommandSourceStack;
import net.minecraftforge.common.extensions.IForgeCommandSourceStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CommandSourceStack.class)
public class CommandSourceStackInject implements IForgeCommandSourceStack {
}