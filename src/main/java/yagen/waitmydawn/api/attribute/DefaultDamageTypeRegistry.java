package yagen.waitmydawn.api.attribute;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.item.weapon.IceAndFireCEItem;
import yagen.waitmydawn.item.weapon.IronsSpellbooksItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DefaultDamageTypeRegistry {
    private static final Map<Item, Map<DamageType, Float>> VANILLA_DAMAGE_TYPES = new HashMap<>();

    // default map, cant add new damage type if it doesn't exist
    private static final Map<DamageType, Float> SWORD_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> PICKAXE_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> AXE_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> SHOVEL_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> HOE_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> TRIDENT_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> MACE_DISTR = Map.ofEntries(
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
    private static final Map<DamageType, Float> BOW_DISTR = Map.ofEntries(
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

    private static final Map<DamageType, Float> TEST_DISTR = Map.ofEntries(
            Map.entry(DamageType.IMPACT, 0f),
            Map.entry(DamageType.PUNCTURE, 0f),
            Map.entry(DamageType.SLASH, 0f),
            Map.entry(DamageType.COLD, 0f),
            Map.entry(DamageType.TOXIN, 0f),
            Map.entry(DamageType.ELECTRICITY, 0f),
            Map.entry(DamageType.HEAT, 0f),
            Map.entry(DamageType.CORROSIVE, 0f),
            Map.entry(DamageType.VIRAL, 0f),
            Map.entry(DamageType.GAS, 0f),
            Map.entry(DamageType.RADIATION, 0f),
            Map.entry(DamageType.MAGNETIC, 1f),
            Map.entry(DamageType.BLAST, 0f)
    );

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

    private static final Item[] SWORDS = {
//            Items.WOODEN_SWORD, Items.STONE_SWORD, Items.IRON_SWORD,
//            Items.GOLDEN_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD
    };
    private static final Item[] PICKAXES = {
//            Items.WOODEN_PICKAXE, Items.STONE_PICKAXE, Items.IRON_PICKAXE,
//            Items.GOLDEN_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE
    };
    private static final Item[] AXES = {
//            Items.WOODEN_AXE, Items.STONE_AXE, Items.IRON_AXE,
//            Items.GOLDEN_AXE, Items.DIAMOND_AXE, Items.NETHERITE_AXE
    };
    private static final Item[] SHOVELS = {
//            Items.WOODEN_SHOVEL, Items.STONE_SHOVEL, Items.IRON_SHOVEL,
//            Items.GOLDEN_SHOVEL, Items.DIAMOND_SHOVEL, Items.NETHERITE_SHOVEL
    };
    private static final Item[] HOES = {
//            Items.WOODEN_HOE, Items.STONE_HOE, Items.IRON_HOE,
//            Items.GOLDEN_HOE, Items.DIAMOND_HOE, Items.NETHERITE_HOE
    };
    private static final Item[] TRIDENTS = {
            Items.TRIDENT
    };
    private static final Item[] MACES = {
            Items.MACE
    };
    private static final Item[] BOWS = {
            Items.BOW, Items.CROSSBOW
    };

    public static float getTotalAttackDamage(Item item) {
        if (item == Items.BOW) return 10f;      // return 1f to fix
        if (item == Items.CROSSBOW) return 10f;
        if (ModList.get().isLoaded("irons_spellbooks")) {
            if (item == IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get()) return 10f;
        }
        if (ModList.get().isLoaded("cataclysm")) {
            if (item == LEndersCataclysmItem.CURSED_BOW.get() ||
                    item == LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() ||
                    item == LEndersCataclysmItem.LASER_GATLING.get() ||
                    item == LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() ||
                    item == LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get())
                return 10f;
        }
        if (ModList.get().isLoaded("iceandfire")) {
            if (item == IceAndFireCEItem.DRAGONBONE_BOW.get()) return 10f;
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

    private static void registerBatch(Item[] items, Map<DamageType, Float> distribution) {
        for (Item item : items) {
            float total = getTotalAttackDamage(item);
            Map<DamageType, Float> dist = new HashMap<>();
            for (var entry : distribution.entrySet()) {
                dist.put(entry.getKey(), entry.getValue() * total);
            }
            VANILLA_DAMAGE_TYPES.put(item, dist);
        }
    }

    private static void registerTag(TagKey<Item> tag, Map<DamageType, Float> dist) {
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            registerSingle(item.value(), dist);
        }
    }

    private static void registerSingle(Item item, Map<DamageType, Float> dist) {
        if (item == null) return;//|| VANILLA_DAMAGE_TYPES.containsKey(item)
        float total = getTotalAttackDamage(item);
        Map<DamageType, Float> map = new HashMap<>();
        dist.forEach((k, v) -> map.put(k, v * total));
        VANILLA_DAMAGE_TYPES.put(item, map);
    }

    static {
        registerTag(ItemTags.SWORDS, SWORD_DISTR);
        registerTag(ItemTags.PICKAXES, PICKAXE_DISTR);
        registerTag(ItemTags.AXES, AXE_DISTR);
        registerTag(ItemTags.SHOVELS, SHOVEL_DISTR);
        registerTag(ItemTags.HOES, HOE_DISTR);
        // besides tay
        registerBatch(TRIDENTS, TRIDENT_DISTR);
        registerBatch(MACES, MACE_DISTR);
        registerBatch(BOWS, BOW_DISTR);
        // override
        //registerSingle(Items.DIAMOND_SWORD, TEST_DISTR);

        /**
         * Iron's Spells 'n Spellbooks
         */
        if (ModList.get().isLoaded("irons_spellbooks")) {
            if (IronsSpellbooksItem.KEEPER_FLAMBERGE.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.KEEPER_FLAMBERGE.get(), simpleDamageMap(-1f, 0.35f, 0.35f, 0, 0, 0, 0.3f, 0, 0, 0, 0, 0, 0));

            if (IronsSpellbooksItem.MAGEHUNTER.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.MAGEHUNTER.get(), simpleDamageMap(0.2f, 0.4f, 0.4f, 0, 0, 0, 0f, 0, 0, 0, 0, 0, 0));

            if (IronsSpellbooksItem.SPELLBREAKER.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.SPELLBREAKER.get(), simpleDamageMap(0.1f, 0.3f, 0.3f, 0, 0, 0.3f, 0f, 0, 0, 0, 0, 0, 0));

            if (IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.LEGIONNAIRE_FLAMBERGE.get(), simpleDamageMap(-1f, 0.25f, 0.25f, 0f, 0f, 0f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.AMETHYST_RAPIER.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.AMETHYST_RAPIER.get(), simpleDamageMap(-1f, 1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.MISERY.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.MISERY.get(), simpleDamageMap(-1f, 0.35f, 0.35f, 0f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.AUTOLOADER_CROSSBOW.get(), simpleDamageMap(0.1f, 0.8f, 0.1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.HELLRAZOR.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.HELLRAZOR.get(), simpleDamageMap(-1f, 0.05f, 0.25f, 0f, 0f, 0f, 0.7f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.DECREPIT_SCYTHE.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.DECREPIT_SCYTHE.get(), simpleDamageMap(-1f, 0.1f, 0.4f, 0f, 0f, 0f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IronsSpellbooksItem.ICE_GREATSWORD.get() != Items.AIR)
                registerSingle(IronsSpellbooksItem.ICE_GREATSWORD.get(), simpleDamageMap(0.1f, 0.15f, 0.15f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
        }
        /**
         * L_Ender's Cataclysm
         */
        if (ModList.get().isLoaded("cataclysm")) {
            if (LEndersCataclysmItem.THE_INCINERATOR.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.THE_INCINERATOR.get(), simpleDamageMap(0.1f, 0.2f, 0.3f, 0f, 0f, 0f, 0.4f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.WRATH_OF_THE_DESERT.get(), simpleDamageMap(0.1f, 0.8f, 0.1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get(), simpleDamageMap(0.8f, 0.2f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get(), simpleDamageMap(0.6f, 0.2f, -1f, 0f, 0f, 0f, 0.2f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.GAUNTLET_OF_GUARD.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.GAUNTLET_OF_GUARD.get(), simpleDamageMap(0.8f, 0.2f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.MEAT_SHREDDER.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.MEAT_SHREDDER.get(), simpleDamageMap(-1f, 0.1f, 0.9f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get(), simpleDamageMap(-1f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.2f, 0.8f));

            if (LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get(), simpleDamageMap(-1f, -1f, -1f, 0f, 0.2f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.8f));

            if (LEndersCataclysmItem.CURSED_BOW.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.CURSED_BOW.get(), simpleDamageMap(0.1f, 0.8f, 0.1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.LASER_GATLING.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.LASER_GATLING.get(), simpleDamageMap(0.1f, 0.2f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.7f, 0f, 0f));

            if (LEndersCataclysmItem.THE_ANNIHILATOR.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.THE_ANNIHILATOR.get(), simpleDamageMap(0.8f, -1f, -1f, 0.2f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.SOUL_RENDER.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.SOUL_RENDER.get(), simpleDamageMap(0.2f, 0.3f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.CERAUNUS.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.CERAUNUS.get(), simpleDamageMap(0.5f, -1f, -1f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.ASTRAPE.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.ASTRAPE.get(), simpleDamageMap(0.1f, 0.2f, 0.3f, 0f, 0f, 0.4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.ANCIENT_SPEAR.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.ANCIENT_SPEAR.get(), simpleDamageMap(0.1f, 0.3f, 0.3f, 0f, 0f, 0f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.TIDAL_CLAWS.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.TIDAL_CLAWS.get(), simpleDamageMap(0.2f, 0.4f, -1f, 0f, 0.4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.THE_IMMOLATOR.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.THE_IMMOLATOR.get(), simpleDamageMap(0.6f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.4f));

            if (LEndersCataclysmItem.VOID_FORGE.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.VOID_FORGE.get(), simpleDamageMap(0.8f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0.2f, 0f));

            if (LEndersCataclysmItem.INFERNAL_FORGE.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.INFERNAL_FORGE.get(), simpleDamageMap(0.8f, -1f, -1f, 0f, 0f, 0f, 0.2f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.ATHAME.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.ATHAME.get(), simpleDamageMap(-1f, -1f, 1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.CORAL_BARDICHE.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.CORAL_BARDICHE.get(), simpleDamageMap(0.2f, 0.75f, 0.05f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (LEndersCataclysmItem.CORAL_SPEAR.get() != Items.AIR)
                registerSingle(LEndersCataclysmItem.CORAL_SPEAR.get(), simpleDamageMap(0.2f, 0.75f, 0.05f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
        }

        if (ModList.get().isLoaded("iceandfire")) {
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_FIRE_SWORD.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_FIRE_AXE.get(), simpleDamageMap(0.08f, 0.08f, 0.24f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_PICKAXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_FIRE_PICKAXE.get(), simpleDamageMap(0.24f, 0.08f, 0.08f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_SHOVEL.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_FIRE_SHOVEL.get(), simpleDamageMap(0.08f, 0.24f, 0.08f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_FIRE_HOE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_FIRE_HOE.get(), simpleDamageMap(0.16f, 0.16f, 0.08f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_ICE_SWORD.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_ICE_AXE.get(), simpleDamageMap(0.08f, 0.08f, 0.24f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_PICKAXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_ICE_PICKAXE.get(), simpleDamageMap(0.24f, 0.08f, 0.08f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_SHOVEL.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_ICE_SHOVEL.get(), simpleDamageMap(0.08f, 0.24f, 0.08f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_ICE_HOE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_ICE_HOE.get(), simpleDamageMap(0.16f, 0.16f, 0.08f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SWORD.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_AXE.get(), simpleDamageMap(0.08f, 0.08f, 0.24f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_PICKAXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_PICKAXE.get(), simpleDamageMap(0.24f, 0.08f, 0.08f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SHOVEL.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_SHOVEL.get(), simpleDamageMap(0.08f, 0.24f, 0.08f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_HOE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONSTEEL_LIGHTNING_HOE.get(), simpleDamageMap(0.16f, 0.16f, 0.08f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONBONE_SWORD_FIRE.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONBONE_SWORD_ICE.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONBONE_SWORD_LIGHTNING.get(), simpleDamageMap(-1f, 0.2f, 0.2f, 0f, 0f, 0.6f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DEATHWORM_GAUNTLET_YELLOW.get(), simpleDamageMap(0.1f, 0.3f, 0.3f, 0f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DEATHWORM_GAUNTLET_WHITE.get(), simpleDamageMap(0.1f, 0.3f, 0.3f, 0f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DEATHWORM_GAUNTLET_RED.get(), simpleDamageMap(0.1f, 0.3f, 0.3f, 0f, 0.3f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));

            if (IceAndFireCEItem.COCKATRICE_SCEPTER.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.COCKATRICE_SCEPTER.get(), simpleDamageMap(-1f, 0.2f, -1f, 0f, 0.8f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.AMPHITHERE_MACUAHUITL.get(), simpleDamageMap(0.6f, 0.2f, 0.2f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TIDE_TRIDENT.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TIDE_TRIDENT.get(), simpleDamageMap(0.2f, 0.4f, 0.4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_AXE.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_AXE.get(), simpleDamageMap(0.4f, 0.2f, 0.4f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_COLUMN.get(), simpleDamageMap(1f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_HAMMER.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_HAMMER.get(), simpleDamageMap(1f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_TRUNK.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_TRUNK.get(), simpleDamageMap(1f, -1f, -1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FOREST.get(), simpleDamageMap(0.5f, -1f, -1f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_COLUMN_FROST.get(), simpleDamageMap(0.5f, -1f, -1f, 0f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.TROLL_WEAPON_TRUNK_FROST.get(), simpleDamageMap(0.5f, -1f, -1f, 0f, 0.5f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
            if (IceAndFireCEItem.DRAGONBONE_BOW.get() != Items.AIR)
                registerSingle(IceAndFireCEItem.DRAGONBONE_BOW.get(), simpleDamageMap(0.1f, 0.8f, 0.1f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f, 0f));
        }
    }


    public static Map<DamageType, Float> get(Item item) {
        return VANILLA_DAMAGE_TYPES.get(item);
    }
}