package xyz.bluspring.kilt.injects.network.chat;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.brigadier.tree.RootCommandNode;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.chat.SignableCommand;

@Mixin(SignableCommand.class)
public abstract class SignableCommandInject {
    @Definition(id = "getRootNode", method = "Lcom/mojang/brigadier/context/CommandContextBuilder;getRootNode()Lcom/mojang/brigadier/tree/CommandNode;")
    @Expression("?.getRootNode() != ?.getRootNode()")
    @WrapOperation(method = "of", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$checkIsRootCommandNode(Object left, Object right, Operation<Boolean> original) {
        return !(left instanceof RootCommandNode<?>) && original.call(left, right);
    }
}
