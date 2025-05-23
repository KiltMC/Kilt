package xyz.bluspring.kilt.mixin.compat.forge.decocraft;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.net.URL;
import java.util.regex.Pattern;

@Pseudo
@Mixin(targets = "com.razz.decocraft.utils.JsonParser")
public class JsonParserMixin {
    @Redirect(method = "parseDirectory", at = @At(value = "INVOKE", target = "Ljava/net/URL;getProtocol()Ljava/lang/String;"))
    private static String kilt$avoidDecocraftProtocolCrash(URL instance) {
        // Kilt: We don't use UnionFS due to the lack of ModLauncher, but because
        //       there's nothing particularly special about this code that requires Union,
        //       we can fairly safely force it to work anyway.
        return "union";
    }

    @Redirect(method = "parseDirectory", at = @At(value = "INVOKE", target = "Ljava/util/regex/Pattern;compile(Ljava/lang/String;)Ljava/util/regex/Pattern;"))
    private static Pattern kilt$useJarSpecificRegex(String regex) {
        return Pattern.compile("file:(.*)(!)(.*)");
    }
}
