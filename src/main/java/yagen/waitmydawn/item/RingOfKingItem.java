package yagen.waitmydawn.item;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.fml.ModList;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.mission.MissionHandler;
import yagen.waitmydawn.capabilities.PlayerBossData;
import yagen.waitmydawn.compat.ISSCompat;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.DataAttachmentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;
import yagen.waitmydawn.util.SupportedMod;

import java.util.*;

public class RingOfKingItem extends Item implements ICurioItem {

    public RingOfKingItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        LivingEntity entity = slotContext.entity();
        if(findEquippedRing(entity).isPresent()) return false;
        if (stack.has(ComponentRegistry.OWNER)) {
            UUID ownerUUID = stack.get(ComponentRegistry.OWNER);

            if (entity instanceof Player player) {
                return player.getUUID().equals(ownerUUID);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onEquip(SlotContext slotContext, ItemStack newStack, ItemStack stack) {
        if (slotContext.entity() instanceof ServerPlayer player) {
            ResourceLocation advancementId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "assassination_endo");
            var serverAdvancements = player.server.getAdvancements();
            var advancementHolder = serverAdvancements.get(advancementId);

            if (advancementHolder != null) {
                var progress = player.getAdvancements().getOrStartProgress(advancementHolder);

                if (progress.isDone()) {
                    bindToPlayer(stack, player);
                }
            }
        }
    }

    @Override
    public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
            SlotContext slotContext, ResourceLocation id, ItemStack stack) {
        Multimap<Holder<Attribute>, AttributeModifier> modifiers = LinkedHashMultimap.create();

        if ("ring".equals(slotContext.identifier())) {
            ItemAttributeModifiers stored = stack.get(ComponentRegistry.STORED_MODIFIERS);
            if (stored != null) {
                for (ItemAttributeModifiers.Entry entry : stored.modifiers()) {
                    modifiers.put(entry.attribute(), entry.modifier());
                }
            }
        }

        return modifiers;
    }
    public static List<RingAttribute> attributes = new ArrayList<>();

    public static void bindToPlayer(ItemStack stack, ServerPlayer player) {
        if (!stack.has(ComponentRegistry.OWNER.get())) {
            stack.set(ComponentRegistry.OWNER.get(), player.getUUID());

            attributes.add(new RingAttribute(Attributes.ATTACK_DAMAGE, 10, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.ATTACK_DAMAGE, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.ATTACK_DAMAGE, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(Attributes.ARMOR, 10, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.ARMOR, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.ARMOR, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(Attributes.ARMOR_TOUGHNESS, 6, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.ARMOR_TOUGHNESS, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.ARMOR_TOUGHNESS, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(Attributes.MOVEMENT_SPEED, 0.1, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.MOVEMENT_SPEED, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.MOVEMENT_SPEED, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(Attributes.MAX_HEALTH, 20, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.MAX_HEALTH, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.MAX_HEALTH, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(Attributes.LUCK, 10, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(Attributes.LUCK, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(Attributes.LUCK, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(YAttributes.MAX_SHIELD, 40, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(YAttributes.MAX_SHIELD, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(YAttributes.MAX_SHIELD, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(YAttributes.SHIELD_REGEN, 1, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(YAttributes.SHIELD_REGEN, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(YAttributes.SHIELD_REGEN, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(YAttributes.MAX_ENERGY, 100, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(YAttributes.MAX_ENERGY, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(YAttributes.MAX_ENERGY, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            attributes.add(new RingAttribute(YAttributes.ENERGY_REGEN, 1, AttributeModifier.Operation.ADD_VALUE));
            attributes.add(new RingAttribute(YAttributes.ENERGY_REGEN, 1, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            attributes.add(new RingAttribute(YAttributes.ENERGY_REGEN, 0.5, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            if (ModList.get().isLoaded(SupportedMod.IRONS_SPELLBOOKS.getValue()))
                ISSCompat.addRingAttributes(attributes);

            addRingAttributes(stack, attributes, 6);
        }
    }

    public static void addRingAttributes(ItemStack stack, List<RingAttribute> potentialAttributes, int countToPick) {
        if (stack.isEmpty() || potentialAttributes.isEmpty() || countToPick <= 0) {
            return;
        }

        List<RingAttribute> pool = new ArrayList<>(potentialAttributes);
        Collections.shuffle(pool);

        int limit = Math.min(countToPick, pool.size());
        List<RingAttribute> selectedAttributes = pool.subList(0, limit);

        ItemAttributeModifiers currentModifiers = stack.getOrDefault(ComponentRegistry.STORED_MODIFIERS, ItemAttributeModifiers.EMPTY);

        Random random = new Random();

        for (RingAttribute entry : selectedAttributes) {
            double randomMultiplier = 0.5 + (random.nextDouble() * 0.5);
            double finalAmount = entry.amount() * randomMultiplier;

            ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID,
                    "ring_" + entry.attribute().getKey().location().getPath() + "_" + entry.operation.id());

            AttributeModifier modifier = new AttributeModifier(modifierId, finalAmount, entry.operation());

            currentModifiers = currentModifiers.withModifierAdded(
                    entry.attribute(),
                    modifier,
                    EquipmentSlotGroup.ANY
            );
        }

        stack.set(ComponentRegistry.STORED_MODIFIERS, currentModifiers);
    }

    public static void addRingAttribute(
            ItemStack stack,
            Holder<Attribute> attribute,
            double amount,
            AttributeModifier.Operation operation) {
        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "ring_"
                + attribute.getKey().location().getPath());

        AttributeModifier modifier = new AttributeModifier(modifierId, amount, operation);

        ItemAttributeModifiers current = stack.getOrDefault(ComponentRegistry.STORED_MODIFIERS, ItemAttributeModifiers.EMPTY);
        ItemAttributeModifiers updated = current.withModifierAdded(attribute, modifier, EquipmentSlotGroup.ANY);
        stack.set(ComponentRegistry.STORED_MODIFIERS, updated);
    }

    public record RingAttribute(Holder<Attribute> attribute,
                                double amount,
                                AttributeModifier.Operation operation) {
    }

    public static boolean isOwner(ItemStack stack, Player player) {
        UUID owner = stack.get(ComponentRegistry.OWNER.get());
        return owner != null && owner.equals(player.getUUID());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, @NotNull TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.yagens_attributes.ring_of_king.1").withStyle(ChatFormatting.LIGHT_PURPLE));

        UUID ownerUUID = stack.get(ComponentRegistry.OWNER);
        Player ownerPlayer = null;
        if (ownerUUID != null && context.level() != null) {
            ownerPlayer = Objects.requireNonNull(context.level()).getPlayerByUUID(ownerUUID);
        }
        if (stack.has(ComponentRegistry.OWNER) && ownerUUID != null) {
            String displayName = ownerUUID.toString();
            if (stack.get(ComponentRegistry.OWNER_STRING.get()) != null) {
                displayName = stack.get(ComponentRegistry.OWNER_STRING.get());
            } else if (ownerPlayer != null) {
                displayName = ownerPlayer.getGameProfile().getName();
                stack.set(ComponentRegistry.OWNER_STRING.get(), displayName);
            }
            tooltipComponents.add(Component.translatable("item.yagens_attributes.ring_of_king.2"
                            ,displayName)
                    .withStyle(ChatFormatting.GOLD));

        } else {
            tooltipComponents.add(Component.translatable("item.yagens_attributes.ring_of_king.3")
                    .withStyle(ChatFormatting.DARK_RED));
            tooltipComponents.add(Component.translatable("item.yagens_attributes.ring_of_king.4")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }

    public static Optional<ItemStack> findEquippedRing(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .flatMap(handler -> handler.findFirstCurio(ItemRegistry.RING_OF_KING.get()))
                .map(SlotResult::stack);
    }
}
