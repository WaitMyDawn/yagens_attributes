package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.item.EndoItem;
import yagen.waitmydawn.yagens_attributes.item.ModItem;
import yagen.waitmydawn.yagens_attributes.item.MaterialItem;
import yagen.waitmydawn.yagens_attributes.item.UnknownRivenItem;
import yagen.waitmydawn.yagens_attributes.core.RivenRoll;

public final class ModItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(YagensAttributes.MODID);
    public static final DeferredItem<UnknownRivenItem> UNKNOWN_MELEE_RIVEN = REGISTER.register("unknown_melee_riven", () -> new UnknownRivenItem(new Item.Properties().rarity(Rarity.EPIC), RivenRoll.Pool.MELEE));
    public static final DeferredItem<UnknownRivenItem> UNKNOWN_RANGED_RIVEN = REGISTER.register("unknown_ranged_riven", () -> new UnknownRivenItem(new Item.Properties().rarity(Rarity.EPIC), RivenRoll.Pool.RANGED));
    public static final DeferredItem<EndoItem> ENDO = REGISTER.register("endo", () -> new EndoItem(new Item.Properties()));
    public static final DeferredItem<ModItem> MOD = REGISTER.register("mod", () -> new ModItem(new Item.Properties()));
    public static final DeferredItem<MaterialItem> FORMA = REGISTER.register("forma", () -> new MaterialItem(new Item.Properties().rarity(Rarity.RARE), "item.yagens_attributes.forma.description"));
    public static final DeferredItem<MaterialItem> OROKIN_CATALYST = REGISTER.register("orokin_catalyst", () -> new MaterialItem(new Item.Properties().rarity(Rarity.RARE), "item.yagens_attributes.orokin_catalyst.description"));
    public static final DeferredItem<MaterialItem> KUVA = REGISTER.register("kuva", () -> new yagen.waitmydawn.yagens_attributes.item.KuvaItem(new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> RAW_GALLIUM = REGISTER.registerSimpleItem("raw_gallium");
    public static final DeferredItem<Item> GALLIUM = REGISTER.registerSimpleItem("gallium");
    public static final DeferredItem<BlockItem> GALLIUM_ORE = REGISTER.registerSimpleBlockItem(ModBlocks.GALLIUM_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_GALLIUM_ORE = REGISTER.registerSimpleBlockItem(ModBlocks.DEEPSLATE_GALLIUM_ORE);

    private ModItems() {
    }

}
