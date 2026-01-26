package yagen.waitmydawn.api.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.api.mods.ModSlot;
import yagen.waitmydawn.item.weapon.IceAndFireCEItem;
import yagen.waitmydawn.item.weapon.LEndersCataclysmItem;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.api.registry.ModRegistry;

import java.util.*;

public class ModCompat {
    public static boolean isWeaponToolOrArmor(ItemStack itemStack) {
        Item item = itemStack.getItem();
        boolean defaultTagItem = item instanceof TieredItem
                || item instanceof ProjectileWeaponItem
                || item instanceof TridentItem
                || (itemStack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST)
                || item instanceof MaceItem
                || item instanceof ShieldItem;
        if (ModList.get().isLoaded("cataclysm")) {
            return defaultTagItem || isCataclysmTool(item) || isCataclysmArmor(item) || isCataclysmShield(item);
        }
        return defaultTagItem;
    }

    public static int validLocation(Item item) {
        if (item instanceof TieredItem || item instanceof MaceItem || item instanceof TridentItem || item instanceof ProjectileWeaponItem)
            return 1;
        else if (item instanceof ArmorItem)
            return 2;
        else if (item instanceof ShieldItem)
            return 3;
        else if (isCataclysmTool(item))
            return 1;
        else if (isCataclysmArmor(item))
            return 2;
        else if (isCataclysmShield(item))
            return 3;
        else
            return 0;
    }

    public static boolean isCataclysmTool(Item item) {
        return
                item == LEndersCataclysmItem.THE_INCINERATOR.get() ||
                        item == LEndersCataclysmItem.THE_ANNIHILATOR.get() ||
                        item == LEndersCataclysmItem.SOUL_RENDER.get() ||
                        item == LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() ||
                        item == LEndersCataclysmItem.GAUNTLET_OF_MAELSTROM.get() ||
                        item == LEndersCataclysmItem.GAUNTLET_OF_BULWARK.get() ||
                        item == LEndersCataclysmItem.GAUNTLET_OF_GUARD.get() ||
                        item == LEndersCataclysmItem.CERAUNUS.get() ||
                        item == LEndersCataclysmItem.ASTRAPE.get() ||
                        item == LEndersCataclysmItem.ANCIENT_SPEAR.get() ||
                        item == LEndersCataclysmItem.TIDAL_CLAWS.get() ||
                        item == LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() ||
                        item == LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get() ||
                        item == LEndersCataclysmItem.LASER_GATLING.get() ||
                        item == LEndersCataclysmItem.MEAT_SHREDDER.get() ||
                        item == LEndersCataclysmItem.CORAL_SPEAR.get() ||
                        item == LEndersCataclysmItem.CORAL_BARDICHE.get() ||
                        item == LEndersCataclysmItem.THE_IMMOLATOR.get();
    }

    public static boolean isCataclysmArmor(Item item) {
        return false;
    }

    public static boolean isCataclysmShield(Item item) {
        if (item == LEndersCataclysmItem.BULWARK_OF_THE_FLAME.get()) return true;
        return false;
    }

    public static boolean isSpecialBow(Item item) {
        return item == LEndersCataclysmItem.ASTRAPE.get() ||
                item == IceAndFireCEItem.TIDE_TRIDENT.get() ||
                item == Items.ARROW ||
                item == LEndersCataclysmItem.ANCIENT_SPEAR.get() ||
                item == LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get() ||
                item == LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() ||
                item == LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() ||
                item == LEndersCataclysmItem.CURSED_BOW.get() ||
                item == LEndersCataclysmItem.LASER_GATLING.get() ||
                item == LEndersCataclysmItem.CORAL_SPEAR.get() ||
                item == LEndersCataclysmItem.CORAL_BARDICHE.get();
    }

    public static boolean isCancelAttackDamage(Item item) {
        return
                item == Items.ARROW ||
                        item == LEndersCataclysmItem.WITHER_ASSAULT_SHOULDER_WEAPON.get() ||
                        item == LEndersCataclysmItem.VOID_ASSAULT_SHOULDER_WEAPON.get() ||
                        item == LEndersCataclysmItem.WRATH_OF_THE_DESERT.get() ||
                        item == LEndersCataclysmItem.CURSED_BOW.get() ||
                        item == LEndersCataclysmItem.LASER_GATLING.get();
    }

    public static ItemStack ensureModContainer(ItemStack stack, int maxSlots) {
        if (!IModContainer.isModContainer(stack) && isWeaponToolOrArmor(stack)) {
            if (stack.getItem() instanceof ArmorItem armor && armor.getEquipmentSlot() == EquipmentSlot.CHEST)
                maxSlots = 12;
            IModContainer container = IModContainer.create(maxSlots, false,
                    stack.getItem() instanceof ArmorItem);
            //if (level.isClientSide()) return stack;
            if (stack.get(ComponentRegistry.DEFAULT_ITEM_ATTRIBUTES.get()) == null) {
                ItemAttributeModifiers defaultModifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
                if (defaultModifiers == ItemAttributeModifiers.EMPTY) {
                    defaultModifiers = stack.getItem().getDefaultAttributeModifiers(stack);// change 4
                }
                stack.set(ComponentRegistry.DEFAULT_ITEM_ATTRIBUTES.get(), new ComponentRegistry.DefaultItemAttributes(defaultModifiers));
            }
            // create empty polarities
            ComponentRegistry.setPolarities(stack, new ArrayList<>(Collections.nCopies(maxSlots, "")));
            IModContainer.set(stack, container);
        }
        return stack;
    }

    public static boolean isValidWarframeAbility(ItemStack stack) {
        if (!IModContainer.isModContainer(stack)) return false;
        int abilityCount = 0;
        var container = IModContainer.get(stack);
        for (ModSlot slot : container.getActiveMods())
            if (slot.getMod().getRarity() == ModRarity.WARFRAME)
                abilityCount++;
        return abilityCount <= 4;
    }

    public static boolean hasReservoirs(ItemStack stack) {
        if (!IModContainer.isModContainer(stack)) return false;
        var container = IModContainer.get(stack);
        for (ModSlot slot : container.getActiveMods())
            if (slot.getMod().getModName().equals("reservoirs_armor_mod"))
                return true;
        return false;
    }

    public static boolean isModInItemStack(ItemStack stack, AbstractMod abstractMod) {
        if (!IModContainer.isModContainer(stack)) return false;
        for (ModSlot slot : IModContainer.get(stack).getActiveMods()) {
            if (slot.getMod().getModName().equals(abstractMod.getModName())) {
                return true;
            }
        }
        return false;
    }

    public static int ModLevelInItemStack(ItemStack stack, AbstractMod abstractMod) {
        if (!IModContainer.isModContainer(stack)) return 0;
        for (ModSlot slot : IModContainer.get(stack).getActiveMods()) {
            if (slot.getMod().getModName().equals(abstractMod.getModName())) {
                return slot.getLevel();
            }
        }
        return 0;
    }

    public static List<MobEffectInstance> getHarmfulEffects(LivingEntity entity) {
        return entity.getActiveEffects()
                .stream()
                .filter(e -> e.getEffect().value().getCategory() == MobEffectCategory.HARMFUL)
                .toList();
    }

    public static final Map<ModRarity, List<AbstractMod>> TRANSFORM_POOL_BY_RARITY = new EnumMap<>(ModRarity.class);

    static {
        Arrays.stream(ModRarity.values())
                .filter(r -> r != ModRarity.RIVEN)
                .forEach(r -> TRANSFORM_POOL_BY_RARITY.put(r, new ArrayList<>()));
        for (AbstractMod mod : ModRegistry.getEnabledMods()) {
            ModRarity rarity = mod.getRarity();
            if (rarity == ModRarity.RIVEN) continue;
            TRANSFORM_POOL_BY_RARITY.computeIfAbsent(rarity, k -> new ArrayList<>()).add(mod);
        }
    }
}
