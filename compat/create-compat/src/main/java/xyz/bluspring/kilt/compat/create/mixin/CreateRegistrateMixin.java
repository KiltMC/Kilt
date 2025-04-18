package xyz.bluspring.kilt.compat.create.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.foundation.data.CreateRegistrate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.eventbus.api.IEventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@IfModLoaded("create")
@Mixin(CreateRegistrate.class)
public abstract class CreateRegistrateMixin extends AbstractRegistrateMixin<CreateRegistrate> {
    @Shadow @Nullable protected ResourceKey<CreativeModeTab> currentTab;

    @Unique
    public CreateRegistrate registerEventListeners(IEventBus bus) {
        return super.registerEventListeners(bus);
    }

    public CreateRegistrate setCreativeTab(CreativeModeTab tab) {
        this.currentTab = BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(tab).orElseThrow();
        return (CreateRegistrate) (Object) this;
    }
}
