// TRACKED HASH: 230230582e2edceefcbd96d5a8ae8fde240fa88d
package xyz.bluspring.kilt.forgeinjects.client.renderer;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.client.renderer.SheetsInjection;

@Mixin(Sheets.class)
public class SheetsInject implements SheetsInjection {
    @CreateStatic
    private static void addWoodType(WoodType woodType) {
        SheetsInjection.addWoodType(woodType);
    }

    @WrapOperation(method = "createSignMaterial", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$useResourceNamespace(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) WoodType type) {
        var loc = new ResourceLocation(type.name());
        return new ResourceLocation(loc.getNamespace(), "entity/signs/" + loc.getPath());
    }

    @WrapOperation(method = "createHangingSignMaterial", at = @At(value = "NEW", target = "(Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))
    private static ResourceLocation kilt$useResourceNamespaceForHanging(String location, Operation<ResourceLocation> original, @Local(argsOnly = true) WoodType type) {
        var loc = new ResourceLocation(type.name());
        return new ResourceLocation(loc.getNamespace(), "entity/signs/hanging/" + loc.getPath());
    }
}