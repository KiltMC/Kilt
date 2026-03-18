package xyz.bluspring.kilt.injects.server.commands;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.commands.SetSpawnCommand;

@Mixin(SetSpawnCommand.class)
public abstract class SetSpawnCommandInject {
    /*@Definition(id = "resourceKey", local = @Local(type = ResourceKey.class))
    @Definition(id = "location", method = "Lnet/minecraft/resources/ResourceKey;location()Lnet/minecraft/resources/ResourceLocation;")
    @Definition(id = "toString", method = "Lnet/minecraft/resources/ResourceLocation;toString()Ljava/lang/String;")
    @Expression("resourceKey.location().toString()")
    @ModifyExpressionValue(method = "setSpawn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static String kilt$tryUseDimensionTranslation(String original, @Local(argsOnly = true) CommandSourceStack source) {
        return source.getLevel().kilt$getDescription(original);
    }*/
    // Kilt TODO: do we add support for this
}
