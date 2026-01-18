package yagen.waitmydawn.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.DamageType;
import yagen.waitmydawn.api.attribute.DamageTypeUtils;
import yagen.waitmydawn.api.mods.*;
import yagen.waitmydawn.item.Mod;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class TooltipsUtils {

    public static List<Component> formatModTooltip(ItemStack stack, Player player) {
        if (stack.getItem() instanceof Mod && IModContainer.isModContainer(stack)) {
            var modList = IModContainer.get(stack);
            if (modList.isEmpty()) {
                return List.of();
            }

            var modData = modList.getModAtIndex(0);
            var mod = modData.getMod();
            var modLevel = modData.getLevel();

            var levelText = Component.literal(String.valueOf(modData.getLevel()));

            String modPolarity = mod.getModPolarity();
            var uniqueInfo = mod.getUniqueInfo(modLevel, player);
            if (uniqueInfo.isEmpty()) {
                uniqueInfo = RivenMod.getRivenUniqueInfo(stack, modLevel);
                modPolarity = stack.getOrDefault(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), "Riven");
            }
            var title = Component.translatable("tooltip.yagens_attributes.level", levelText)
                    .append(" ")
                    .append(Component.translatable("tooltip.yagens_attributes.capacity_cost", mod.getBaseCapacityCost() + modData.getLevel()).withStyle(ChatFormatting.RED))
                    .append(" ")
                    .append(Component.translatable("tooltip.yagens_attributes.polarity", modPolarity).withStyle(ChatFormatting.GOLD))
                    .append(" ")
                    .append(Component.translatable("tooltip.yagens_attributes.rarity", mod.getRarity().getDisplayName()).withStyle(mod.getRarity().getDisplayName().getStyle()))
                    .withStyle(ChatFormatting.GRAY);


            List<Component> lines = new ArrayList<>();
            lines.add(Component.literal(" ").append(title));
            uniqueInfo.forEach((line) -> lines.add(Component.literal(" ").append(line.withStyle(line.getStyle().applyTo(getStyleFor(player, mod))))));

            lines.add(Component.empty());
            //lines.add(whenInItem);

            return lines;
        }
        return List.of();
    }

    private static final Style INFO_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_GREEN);
    //private static final Style OBFUSCATED_STYLE = AbstractMod.ELDRITCH_OBFUSCATED_STYLE.applyTo(INFO_STYLE);

    public static List<FormattedCharSequence> createModDescriptionTooltip(AbstractMod mod, Font font) {
        Player player = MinecraftInstanceHelper.instance.player();
        var name = mod.getDisplayName(player);
        var description = font.split(Component.translatable(String.format("%s.guide", mod.getComponentId())).withStyle(ChatFormatting.GRAY), 180);
        var hoverText = new ArrayList<FormattedCharSequence>();
        hoverText.add(FormattedCharSequence.forward(name.getString(), name.getStyle().withUnderlined(true)));
        if (!mod.obfuscateStats(player)) {
            hoverText.addAll(description);
        }
        return hoverText;
    }

    public static Style getStyleFor(Player player, AbstractMod mod) {
        //return mod.obfuscateStats(player) ? OBFUSCATED_STYLE : INFO_STYLE;
        return INFO_STYLE;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void updateDamageTooltip(ItemTooltipEvent event) {
        if (!IModContainer.isModContainer(event.getItemStack())) return;
        Map<DamageType, Float> weaponMap = DamageTypeUtils.getDamageTypes(event.getItemStack());
        if (weaponMap == null || weaponMap.isEmpty()) return;
        event.getToolTip().add(Component.translatable("tooltip.yagens_attributes.damagecompose"));
        for (var entry : weaponMap.entrySet()) {
            String type = switch (entry.getKey()) {
                case PUNCTURE -> "damagetype.yagens_attributes.puncture_tooltip";
                case SLASH -> "damagetype.yagens_attributes.slash_tooltip";
                case IMPACT -> "damagetype.yagens_attributes.impact_tooltip";
                case COLD -> "damagetype.yagens_attributes.cold_tooltip";
                case HEAT -> "damagetype.yagens_attributes.heat_tooltip";
                case TOXIN -> "damagetype.yagens_attributes.toxin_tooltip";
                case ELECTRICITY -> "damagetype.yagens_attributes.electricity_tooltip";
                case CORROSIVE -> "damagetype.yagens_attributes.corrosive_tooltip";
                case VIRAL -> "damagetype.yagens_attributes.viral_tooltip";
                case GAS -> "damagetype.yagens_attributes.gas_tooltip";
                case MAGNETIC -> "damagetype.yagens_attributes.magnetic_tooltip";
                case RADIATION -> "damagetype.yagens_attributes.radiation_tooltip";
                case BLAST -> "damagetype.yagens_attributes.blast_tooltip";
            };
            if (entry.getValue() <= 0) continue;
            event.getToolTip().add(Component.translatable(type, String.format("%.1f", entry.getValue())).withStyle(ChatFormatting.GRAY));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void updateLevelTooltip(ItemTooltipEvent event) {
        if (!IModContainer.isModContainer(event.getItemStack()) || event.getItemStack().is(ItemRegistry.MOD)) return;
        ComponentRegistry.UpgradeData data = ComponentRegistry.getUpgrade(event.getItemStack());
        if (data.level() < 30)
            event.getToolTip().add(Component.translatable(
                    "tooltip.yagens_attributes.item_upgrade_info",
                    data.level(), data.exp() * 100 / data.nextLevelExpNeed()));
        else
            event.getToolTip().add(Component.translatable(
                    "tooltip.yagens_attributes.item_upgrade_info_max_level",
                    data.level()));
    }

    @SubscribeEvent
    public static void updateRivenTypeTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemRegistry.MOD)) return;
        if (!IModContainer.get(stack).getModAtIndex(0).getMod().getUniqueInfo(1, null).isEmpty()) return;
        Item rivenType = stack.get(ComponentRegistry.RIVEN_TYPE.get());
        if (rivenType == null) return;

        MutableComponent line = Component.translatable("tooltip.yagens_attributes.riven_type",
                        rivenType.getDescription())
                .withStyle(Style.EMPTY.withColor(0x8A2BE2));
        event.getToolTip().add(line);
    }

    @SubscribeEvent
    public static void updateGraceAbilityTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        if (!stack.is(ItemRegistry.MOD)) return;
        if (!IModContainer.get(stack).getModAtIndex(0).getMod().getModName().equals("grace_armor_mod")) return;
        Attribute attribute = GraceArmorMod.getGraceAbility(stack);
        if (attribute == Attributes.MOVEMENT_SPEED.value()) return;

        MutableComponent line = Component.literal(" ").append(Component.translatable("functions.yagens_attributes.grace_armor_mod.2",
                        Component.translatable(attribute.getDescriptionId())))
                .withStyle(INFO_STYLE);
        List<Component> tooltip = event.getToolTip();
        for (int i = 0; i < tooltip.size(); i++)
            if (tooltip.get(i).getString().trim().isEmpty()) {
                tooltip.add(i, line);
                return;
            }
    }
}
