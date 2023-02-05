package xyz.bluspring.kilt.forgeinjects.commands.synchronization;

import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.commands.synchronization.ArgumentTypeInfosInjection;

@Mixin(ArgumentTypeInfos.class)
public class ArgumentTypeInfosInject implements ArgumentTypeInfosInjection {
}
