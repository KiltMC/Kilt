package xyz.bluspring.kilt.mixin.world.item;

import net.minecraft.world.item.UseAnim;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(UseAnim.class)
public interface UseAnimAccessor {
    @Invoker("<init>")
    static UseAnim createUseAnim(String name, int size) {
        throw new IllegalStateException();
    }

    @Accessor("$VALUES")
    static UseAnim[] getValues() {
        throw new IllegalStateException();
    }

    @Accessor("$VALUES")
    static void setValues(UseAnim[] values) {
        throw new IllegalStateException();
    }
}
