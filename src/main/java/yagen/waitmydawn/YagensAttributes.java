package yagen.waitmydawn;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import yagen.waitmydawn.api.attribute.YAttributes;
import yagen.waitmydawn.api.events.*;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.config.ClientConfigs;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.item.mod.SelfModItems;
import yagen.waitmydawn.mixin.RangedAttributeAccessor;
import yagen.waitmydawn.network.NetworkHandler;
import yagen.waitmydawn.registries.*;
import yagen.waitmydawn.render.LevelRender;
import yagen.waitmydawn.setup.ClientSetup;
import yagen.waitmydawn.util.TooltipsUtils;
import yagen.waitmydawn.util.YagenAttributesModCreativeTab;

import java.util.Map;

@Mod(YagensAttributes.MODID)
public class YagensAttributes {
    public static final String MODID = "yagens_attributes";
    public static final Logger LOGGER = LogUtils.getLogger();

    public YagensAttributes(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(YagensAttributes::commonSetup);
        modEventBus.addListener(ModRegistry::registerRegistry);
        modEventBus.addListener(ClientSetup::clientSetup);
        modEventBus.addListener(NetworkHandler::onRegister);

        YagenAttributesModCreativeTab.register(modEventBus);
        SelfModItems.register(modEventBus);
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

        NeoForge.EVENT_BUS.register(AttackEventHandler.class);
        NeoForge.EVENT_BUS.register(TooltipsUtils.class);
        NeoForge.EVENT_BUS.register(XpChangeEvent.class);
        NeoForge.EVENT_BUS.register(BowShootEvent.class);
        NeoForge.EVENT_BUS.register(LivingEntityDeathEvent.class);
        NeoForge.EVENT_BUS.register(PlayerInteractionEvent.class);
        NeoForge.EVENT_BUS.register(EntityLevelBonusEvent.class);
        NeoForge.EVENT_BUS.register(LevelRender.class);
//        NeoForge.EVENT_BUS.register(AutoEntityTypeClassifier.class);

        modContainer.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, String.format("%s/%s-client.toml", YagensAttributes.MODID,YagensAttributes.MODID));
        modContainer.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, String.format("%s/%s-server.toml", YagensAttributes.MODID,YagensAttributes.MODID));

    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            Map<ResourceLocation, Double> map = Map.of(
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.max_health"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.armor"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.armor_toughness"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.attack_damage"), 2_147_483_647D,
                    ResourceLocation.fromNamespaceAndPath("minecraft", "generic.attack_knockback"), 1_024D
            );
            for (Attribute attribute : BuiltInRegistries.ATTRIBUTE){
                ResourceLocation id = BuiltInRegistries.ATTRIBUTE.getKey(attribute);
                if (id != null && map.containsKey(id) && attribute instanceof RangedAttributeAccessor accessor) {
                    accessor.setMinValue(0);
                    accessor.setMaxValue(map.get(id));
                }
            }
        });
    }
}
