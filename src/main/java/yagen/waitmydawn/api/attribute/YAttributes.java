package yagen.waitmydawn.api.attribute;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import yagen.waitmydawn.YagensAttributes;

@EventBusSubscriber(modid = YagensAttributes.MODID, bus = EventBusSubscriber.Bus.MOD)
public class YAttributes {
    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, YagensAttributes.MODID);
    public static final DeferredHolder<Attribute, Attribute> CRITICAL_CHANCE =
            ATTRIBUTES.register("critical_chance",
                    () -> new PercentageAttribute("attribute.yagens_attributes.critical_chance",
                            0.0D, 0.0D, 1024.0D)
                            .setSyncable(true));
    public static final DeferredHolder<Attribute, Attribute> CRITICAL_DAMAGE =
            ATTRIBUTES.register("critical_damage",
                    () -> new PercentageAttribute("attribute.yagens_attributes.critical_damage", 2D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STATUS_CHANCE =
            ATTRIBUTES.register("status_chance",
                    () -> new PercentageAttribute("attribute.yagens_attributes.status_chance", 0.0D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> STATUS_DURATION =
            ATTRIBUTES.register("status_duration",
                    () -> new RangedAttribute("attribute.yagens_attributes.status_duration", 6.0D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> LIFE_STEAL =
            ATTRIBUTES.register("life_steal",
                    () -> new PercentageAttribute("attribute.yagens_attributes.life_steal", 0.0D, 0.0D, 32.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> MULTISHOT =
            ATTRIBUTES.register("multishot",
                    () -> new RangedAttribute("attribute.yagens_attributes.multishot", 1.0D, 0.0D, 32.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> FIRE_RATE =
            ATTRIBUTES.register("firerate",
                    () -> new RangedAttribute("attribute.yagens_attributes.firerate", 1.0D, 0.0D, 8.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> COMBO_BONUS_CC =
            ATTRIBUTES.register("combo_bonus_cc",
                    () -> new PercentageAttribute("attribute.yagens_attributes.combo_bonus_cc", 0D, 0.0D, 8.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> COMBO_BONUS_SC =
            ATTRIBUTES.register("combo_bonus_sc",
                    () -> new PercentageAttribute("attribute.yagens_attributes.combo_bonus_sc", 0D, 0.0D, 8.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> COMBO_DURATION =
            ATTRIBUTES.register("combo_duration",
                    () -> new RangedAttribute("attribute.yagens_attributes.combo_duration", 6.0D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> SHOOT_SPREAD =
            ATTRIBUTES.register("shoot_spread",
                    () -> new RangedAttribute("attribute.yagens_attributes.shoot_spread", 1.0D, 0.0D, 16.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ARMOR_PENETRATION =
            ATTRIBUTES.register("armor_penetration",
                    () -> new RangedAttribute("attribute.yagens_attributes.armor_penetration", 0.0D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ARMOR_TOUGHNESS_PENETRATION =
            ATTRIBUTES.register("armor_toughness_penetration",
                    () -> new RangedAttribute("attribute.yagens_attributes.armor_toughness_penetration", 0.0D, 0.0D, 1024.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ARMOR_PENETRATION_PERCENT =
            ATTRIBUTES.register("armor_penetration_percent",
                    () -> new PercentageAttribute("attribute.yagens_attributes.armor_penetration_percent", 0.0D, 0.0D, 1.0D)
                            .setSyncable(true));

    public static final DeferredHolder<Attribute, Attribute> ARMOR_TOUGHNESS_PENETRATION_PERCENT =
            ATTRIBUTES.register("armor_toughness_penetration_percent",
                    () -> new PercentageAttribute("attribute.yagens_attributes.armor_toughness_penetration_percent", 0.0D, 0.0D, 1.0D)
                            .setSyncable(true));

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute)));
    }

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

}
