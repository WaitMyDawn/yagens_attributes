package yagen.waitmydawn.api.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.*;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.config.WeaponStatsManager;
import yagen.waitmydawn.item.weapon.IceAndFireCEItem;
import yagen.waitmydawn.item.weapon.IronsSpellbooksItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.item.weapon.TwilightForestItem;
import yagen.waitmydawn.util.SupportedMod;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultDamageTypeRegistry {
    public static Map<Item, Map<DamageType, Float>> VANILLA_DAMAGE_TYPES = new HashMap<>();

    public static void clear() {
        VANILLA_DAMAGE_TYPES.clear();
    }

    // default map, cant add new damage type if it doesn't exist
    public static final Map<DamageType, Float> SWORD_DISTR = Map.ofEntries(
            Map.entry(DamageType.PUNCTURE, 0.5f),
            Map.entry(DamageType.SLASH, 0.5f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> PICKAXE_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.6f),
            Map.entry(DamageType.PUNCTURE, 0.2f),
            Map.entry(DamageType.SLASH, 0.2f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> AXE_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.2f),
            Map.entry(DamageType.PUNCTURE, 0.2f),
            Map.entry(DamageType.SLASH, 0.6f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> SHOVEL_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.2f),
            Map.entry(DamageType.PUNCTURE, 0.6f),
            Map.entry(DamageType.SLASH, 0.2f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> HOE_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.4f),
            Map.entry(DamageType.PUNCTURE, 0.4f),
            Map.entry(DamageType.SLASH, 0.2f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> TRIDENT_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.2f),
            Map.entry(DamageType.PUNCTURE, 0.75f),
            Map.entry(DamageType.SLASH, 0.05f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> MACE_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 1f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );
    public static final Map<DamageType, Float> BOW_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0.1f),
            Map.entry(DamageType.PUNCTURE, 0.8f),
            Map.entry(DamageType.SLASH, 0.1f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 0f),
            Map.entry(DamageType.BLAST, 0f)
    );

    // deprecated maybe
    public static Map<DamageType, Float> simpleDamageMap(float impact,
                                                         float puncture,
                                                         float slash,
                                                         float cold,
                                                         float toxin,
                                                         float electricity,
                                                         float heat,
                                                         float corrosive,
                                                         float viral,
                                                         float gas,
                                                         float radiation,
                                                         float magnetic,
                                                         float blast) {
        Map<DamageType, Float> map = new LinkedHashMap<>();
        float[] values = {impact, puncture, slash, cold, toxin, electricity,
                heat, corrosive, viral, gas, radiation, magnetic, blast};
        DamageType[] types = {
                DamageType.IMPACT, DamageType.PUNCTURE, DamageType.SLASH,
                DamageType.COLD, DamageType.TOXIN, DamageType.ELECTRICITY,
                DamageType.HEAT, DamageType.CORROSIVE, DamageType.VIRAL,
                DamageType.GAS, DamageType.RADIATION, DamageType.MAGNETIC,
                DamageType.BLAST
        };

        for (int i = 0; i < values.length; i++) {
            if (values[i] != -1f) {
                map.put(types[i], values[i]);
            }
        }
        return Map.copyOf(map);
    }

    public static final Item[] SWORDS =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof SwordItem)
                    .toArray(Item[]::new);

    public static final Item[] AXES =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof AxeItem)
                    .toArray(Item[]::new);

    public static final Item[] PICKAXES =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof PickaxeItem)
                    .toArray(Item[]::new);

    public static final Item[] SHOVELS =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof ShovelItem)
                    .toArray(Item[]::new);

    public static final Item[] HOES =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof HoeItem)
                    .toArray(Item[]::new);

    public static final Item[] TRIDENTS =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof TridentItem)
                    .toArray(Item[]::new);
    public static final Item[] MACES =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof MaceItem)
                    .toArray(Item[]::new);
    public static final Item[] BOWS =
            BuiltInRegistries.ITEM.stream()
                    .filter(item -> item instanceof BowItem || item instanceof CrossbowItem)
                    .toArray(Item[]::new);

    public static float getTotalAttackDamage(Item item) {
        if (item instanceof BowItem) return 10f;
        if (item instanceof CrossbowItem) return 10f;
        if (ModList.get().isLoaded("cataclysm")) {
            if (item == LEndersCataclysmItem.CURSED_BOW.get() ||
                    item == LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() ||
                    item == LEndersCataclysmItem.LASER_GATLING.get() ||
                    item == LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() ||
                    item == LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get())
                return 10f;
        }

        ItemStack stack = new ItemStack(item);
        double damage = 0.0;

        for (var entry : stack.getAttributeModifiers().modifiers()) {
            if (entry.slot() == EquipmentSlotGroup.MAINHAND &&
                    entry.attribute() == Attributes.ATTACK_DAMAGE &&
                    entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                damage += entry.modifier().amount();
            }
        }
        return (float) damage;
    }

    public static void registerBatch(Item[] items, Map<DamageType, Float> distribution) {
        for (Item item : items) {
            registerSingle(item, distribution);
        }
    }

    // deprecated maybe
    public static void registerTag(TagKey<Item> tag, Map<DamageType, Float> dist) {
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            registerSingle(item.value(), dist);
        }
    }

    public static void registerSingle(Item item, Map<DamageType, Float> dist) {
        if (item == null) return;//|| VANILLA_DAMAGE_TYPES.containsKey(item)
        float total = getTotalAttackDamage(item);
        Map<DamageType, Float> map = new HashMap<>();
        dist.forEach((k, v) -> {
            if (v != -1f)
                map.put(k, v * total);
        });
        VANILLA_DAMAGE_TYPES.put(item, map);
    }

    public static Map<DamageType, Float> get(Item item) {
        return VANILLA_DAMAGE_TYPES.get(item);
    }
}