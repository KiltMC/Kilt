package xyz.bluspring.kilt.forgeinjects.client.renderer;

import net.minecraft.client.renderer.Sheets;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.render.SheetsInjection;

@Mixin(Sheets.class)
public class SheetsInject implements SheetsInjection {
}
