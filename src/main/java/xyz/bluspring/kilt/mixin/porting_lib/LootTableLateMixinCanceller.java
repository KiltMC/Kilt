package xyz.bluspring.kilt.mixin.porting_lib;

import com.bawnorton.mixinsquared.api.MixinCanceller;

import java.util.List;

public class LootTableLateMixinCanceller implements MixinCanceller {

    @Override
    public boolean shouldCancel(List<String> targetClassNames, String mixinClassName) {
        // TODO: remove this when Porting Lib fixes this
        return mixinClassName.contains("LootTableMixin_Late");
    }
}
