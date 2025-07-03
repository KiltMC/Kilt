package xyz.bluspring.kilt.forgeinjects;

import net.minecraft.FileUtil;
import org.spongepowered.asm.mixin.Mixin;

import java.util.regex.Pattern;

@Mixin(FileUtil.class)
public abstract class FileUtilInject {
    //private static final Pattern RESERVED_WINDOWS_FILENAMES_NEOFORGE = Pattern.compile(".*\\.|(?:CON|PRN|AUX|NUL|CLOCK\\$|CONIN\\$|CONOUT\\$|(?:COM|LPT)[¹²³0-9])(?:\\..*)?", 2);
}
