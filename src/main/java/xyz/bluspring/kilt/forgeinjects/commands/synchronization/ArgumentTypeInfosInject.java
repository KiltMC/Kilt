package xyz.bluspring.kilt.forgeinjects.commands.synchronization;

import com.mojang.brigadier.arguments.ArgumentType;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.commands.synchronization.ArgumentTypeInfos;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.commands.synchronization.ArgumentTypeInfosInjection;

@Mixin(ArgumentTypeInfos.class)
public class ArgumentTypeInfosInject implements ArgumentTypeInfosInjection {
    @CreateStatic
    private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>, I extends ArgumentTypeInfo<A, T>> I registerByClass(Class<A> infoClass, I argumentTypeInfo) {
        return ArgumentTypeInfosInjection.registerByClass(infoClass, argumentTypeInfo);
    }
}
