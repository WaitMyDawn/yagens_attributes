package yagen.waitmydawn.yagens_attributes.client;

import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.*;
import yagen.waitmydawn.yagens_attributes.core.ModCatalog;
import yagen.waitmydawn.yagens_attributes.data.ModJson;
import yagen.waitmydawn.yagens_attributes.network.ModCatalogPayload;
import yagen.waitmydawn.yagens_attributes.registries.ModCreativeTabs;
import yagen.waitmydawn.yagens_attributes.setup.ModTransactions;
import java.util.*;

public final class ClientMods {
    private static ModCatalog catalog = ModCatalog.EMPTY;
    private static List<ItemStack> icons = List.of();
    private ClientMods() {}
    public static ModCatalog catalog() { return catalog; }
    public static void clear() { catalog = ModCatalog.EMPTY; icons = List.of(); }
    public static void accept(ModCatalogPayload payload) {
        var prepared = ModJson.catalog(JsonParser.parseString(payload.json()).getAsJsonObject());
        catalog = prepared;
        icons = prepared.creative().stream().filter(card -> card.level() == 0).map(card -> ModTransactions.stack(card.id(), 0)).toList();
        WeaponInspectionCache.clear();
        var minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null && minecraft.getConnection() != null) {
            var parameters = new CreativeModeTab.ItemDisplayParameters(minecraft.level.enabledFeatures(), minecraft.player.canUseGameMasterBlocks(), minecraft.getConnection().registryAccess());
            ModCreativeTabs.MODS.get().buildContents(parameters); CreativeModeTabs.searchTab().buildContents(parameters);
        }
    }
    public static ItemStack icon() {
        if (icons.isEmpty()) return ItemStack.EMPTY;
        long tick = Minecraft.getInstance().level == null ? 0 : Minecraft.getInstance().level.getGameTime();
        return icons.get((int) Math.floorMod(tick / 40, icons.size()));
    }
}
