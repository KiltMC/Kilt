package xyz.bluspring.kilt.injects.server.commands;

import java.util.Collection;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;

@Mixin(DataPackCommand.class)
public abstract class DataPackCommandInject {
    @ModifyExpressionValue(method = "listEnabledPacks", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;getSelectedPacks()Ljava/util/Collection;"))
    private static Collection<Pack> kilt$filterHiddenPacks(Collection<Pack> original) {
        return original.stream().filter(p -> !p.isHidden()).toList();
    }
}
