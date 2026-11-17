package yagen.waitmydawn.yagens_attributes.compat;

import twilightforest.init.TFItems;
import net.minecraft.world.item.Item;
import yagen.waitmydawn.yagens_attributes.core.AttackKind;

final class TwilightForestCompat {
    private TwilightForestCompat() {}
    static void register() {
        NativeWeapons.register(TFItems.TRIPLE_BOW.get(), new NativeWeapons.Adapter(AttackKind.RANGED, true, true, 0, 0, stack -> 5));
        for (Item item : new Item[]{TFItems.ENDER_BOW.get(), TFItems.ICE_BOW.get(), TFItems.SEEKER_BOW.get()})
            NativeWeapons.register(item, new NativeWeapons.Adapter(AttackKind.RANGED, false, false, 0, 0, stack -> 6));
        NativeWeapons.register(TFItems.BLOCK_AND_CHAIN.get(), new NativeWeapons.Adapter(AttackKind.MELEE, true, false, 0, 0, stack -> 10));
    }
}
