package yagen.waitmydawn.yagens_attributes.compat;

import net.neoforged.fml.ModList;

public final class CompatSetup {
    private CompatSetup() {}
    public static void register() {
        if (ModList.get().isLoaded("cataclysm")) CataclysmCompat.register();
        if (ModList.get().isLoaded("twilightforest")) TwilightForestCompat.register();
    }
}
