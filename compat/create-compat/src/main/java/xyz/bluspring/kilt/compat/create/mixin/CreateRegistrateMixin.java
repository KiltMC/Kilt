package xyz.bluspring.kilt.compat.create.mixin;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.tterrag.registrate.AbstractRegistrate;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateForgeExtension;

@IfModLoaded("create")
@Mixin(CreateRegistrate.class)
public abstract class CreateRegistrateMixin extends AbstractRegistrate<CreateRegistrate> implements AbstractRegistrateForgeExtension<CreateRegistrate> {
    @Shadow @Nullable protected ResourceKey<CreativeModeTab> currentTab;

    protected CreateRegistrateMixin(String modid) {
        super(modid);
    }

    /*@Unique
    public CreateRegistrate registerEventListeners(IEventBus bus) {
        return super.registerEventListeners(bus);
    }*/

    public CreateRegistrate setCreativeTab(RegistryObject<CreativeModeTab> tab) {
        this.currentTab = tab.getKey();
        return (CreateRegistrate) (Object) this;
    }
}
