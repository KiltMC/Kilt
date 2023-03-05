package xyz.bluspring.kilt.forgeinjects.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementList;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.AdvancementLoadFix;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(AdvancementList.class)
public class AdvancementListInject {
    @Shadow @Final private Set<Advancement> roots;

    @Inject(at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", shift = At.Shift.BEFORE), method = "add")
    public void kilt$buildForgeSortedTrees(Map<ResourceLocation, Advancement.Builder> map, CallbackInfo ci) {
        AdvancementLoadFix.buildSortedTrees(this.roots);
    }
}
