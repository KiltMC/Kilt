package xyz.bluspring.kilt.mixin.compat.ferritecore;

import com.bawnorton.mixinsquared.api.MixinCanceller;
import xyz.bluspring.kilt.Kilt;

import java.util.List;

public class FerriteMrlCanceller implements MixinCanceller {
    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        // Forcefully disable FerriteCore's MRL optimization, due to a weird bug related to Kilt.
        // At some point we'll figure out what's wrong with it.
        if (mixinClassName.contains("ferritecore") && mixinClassName.contains("ModelResourceLocationMixin")) {
            Kilt.Companion.getLogger().warn("FerriteCore has been detected, we are forcefully disabling their ModelResourceLocation optimization!");
            Kilt.Companion.getLogger().warn("For more info, please view https://github.com/KiltMC/Kilt/issues/6");
            return true;
        }

        return false;
    }
}
