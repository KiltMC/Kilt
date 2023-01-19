package xyz.bluspring.kilt.forgeinjects.core;

import net.minecraft.core.Holder;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.HolderReferenceInjection;

@Mixin(Holder.Reference.class)
public class HolderReferenceInject implements HolderReferenceInjection {
    @Shadow @Final private Holder.Reference.Type type;

    @Override
    @NotNull
    public Holder.Reference.Type getType() {
        return this.type;
    }
}
