package yagen.waitmydawn;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.ModList;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.compat.ISSCompat;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.mixin.RangedAttributeAccessor;
import yagen.waitmydawn.network.NetworkHandler;
import yagen.waitmydawn.registries.*;
import yagen.waitmydawn.util.SupportedMod;
import yagen.waitmydawn.util.YagenAttributesCreativeTab;

import java.util.Map;

import static yagen.waitmydawn.item.mod.armor_mod.GraceArmorMod.ATTRIBUTE_SET;

@Mod(YagensAttributes.MODID)
public class YagensAttributes {
    public static final String MODID = "yagens_attributes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public YagensAttributes(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(YagensAttributes::commonSetup);
        modEventBus.addListener(ModRegistry::registerRegistry);
        modEventBus.addListener(NetworkHandler::onRegister);
        modEventBus.addListener(CapabilitiesRegistry::registerCapabilities);

        YagenAttributesCreativeTab.register(modEventBus);
        MenuRegistry.register(modEventBus);
        ModRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        ComponentRegistry.register(modEventBus);
        BlockRegistry.register(modEventBus);
        YAttributes.register(modEventBus);
        MobEffectRegistry.register(modEventBus);
        LootRegistry.register(modEventBus);
        DataAttachmentRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s/%s-client.toml", YagensAttributes.MODID, YagensAttributes.MODID));
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s/%s-server.toml", YagensAttributes.MODID, YagensAttributes.MODID));

    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Map<ResourceLocation, Double> map = Map.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.max_health"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.max_absorption"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.armor"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.armor_toughness"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.attack_damage"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.attack_knockback"), 1_024D
            );
            for (Attribute attribute : BuiltInRegistries.ATTRIBUTE) {
                ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
                if (id != null && map.containsKey(id) && attribute instanceof RangedAttributeAccessor accessor) {
                    accessor.setMinValue(0);
                    accessor.setMaxValue(map.get(id));
                }
            }
            ATTRIBUTE_SET.put(Attributes.ARMOR.value(), 6.0);
            ATTRIBUTE_SET.put(Attributes.ARMOR_TOUGHNESS.value(), 2.0);
            if (ModList.get().isLoaded(SupportedMod.IRONS_SPELLBOOKS.getValue())) {
                ISSCompat.addAttributeSet();
            }
        });
    }
}
