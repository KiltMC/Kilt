// TRACKED HASH: a4d34a5fd04e3ce764b01db9dddb850217a82d89
package xyz.bluspring.kilt.injects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.neoforged.fml.common.asm.enumextension.ExtensionInfo;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;
import net.neoforged.fml.common.asm.enumextension.NamedEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.blaze3d.vertex.VertexFormatElementInjection;

@Mixin(VertexFormatElement.class)
public abstract class VertexFormatElementInject implements VertexFormatElementInjection {
    @CreateStatic
    private static int findNextId() {
        return VertexFormatElementInjection.findNextId();
    }

    @NamedEnum
    @Mixin(VertexFormatElement.Usage.class)
    public abstract static class UsageInject implements IExtensibleEnum {
        @CreateStatic
        private static ExtensionInfo getExtensionInfo() {
            return ExtensionInfo.nonExtended((Class) VertexFormatElement.Usage.class);
        }
    }
}