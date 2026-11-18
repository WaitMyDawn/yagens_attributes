package yagen.waitmydawn.yagens_attributes;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;
import org.slf4j.Logger;
import yagen.waitmydawn.yagens_attributes.config.ClientConfigs;
import yagen.waitmydawn.yagens_attributes.config.ServerConfigs;
import yagen.waitmydawn.yagens_attributes.core.ModIdentity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import yagen.waitmydawn.yagens_attributes.mixin.RangedAttributeAccessor;
import yagen.waitmydawn.yagens_attributes.network.DamagePayload;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.setup.PanelService;
import yagen.waitmydawn.yagens_attributes.setup.WeaponInspectionService;
import net.minecraft.server.level.ServerPlayer;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;
import yagen.waitmydawn.yagens_attributes.registries.RangedAttachments;
import yagen.waitmydawn.yagens_attributes.registries.ShieldAttributes;
import yagen.waitmydawn.yagens_attributes.setup.ChargeService;
import java.util.Map;

@Mod(YagensAttributes.MODID)
public final class YagensAttributes {
    public static final String MODID = ModIdentity.ID;
    public static final Logger LOGGER = LogUtils.getLogger();

    public YagensAttributes(IEventBus bus, ModContainer container) {
        Components.REGISTER.register(bus);
        ModItems.REGISTER.register(bus);
        yagen.waitmydawn.yagens_attributes.registries.LootTypes.ENTRIES.register(bus);
        yagen.waitmydawn.yagens_attributes.registries.LootTypes.MODIFIERS.register(bus);
        yagen.waitmydawn.yagens_attributes.registries.ModCreativeTabs.REGISTER.register(bus);
        RangedAttachments.REGISTER.register(bus);
        yagen.waitmydawn.yagens_attributes.registries.StatusParticles.REGISTER.register(bus);
        ShieldAttributes.REGISTER.register(bus);
        bus.addListener(ShieldAttributes::attributes);
        bus.addListener(yagen.waitmydawn.yagens_attributes.datagen.ModDataProvider::gather);
        bus.addListener(yagen.waitmydawn.yagens_attributes.datagen.LootDataProvider::gather);
        bus.addListener(YagensAttributes::commonSetup);
        bus.addListener(YagensAttributes::network);
        container.registerConfig(ModConfig.Type.CLIENT, ClientConfigs.SPEC, ModIdentity.CLIENT_CONFIG);
        container.registerConfig(ModConfig.Type.SERVER, ServerConfigs.SPEC, ModIdentity.SERVER_CONFIG);
    }

    private static void network(RegisterPayloadHandlersEvent event) {
        event.registrar("29")
                .playToClient(StatusEffectPayload.TYPE, StatusEffectPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(WeaponBuffsPayload.TYPE, WeaponBuffsPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(ComboPayload.TYPE, ComboPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(StatusMarkersPayload.TYPE, StatusMarkersPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(ModCatalogPayload.TYPE, ModCatalogPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(ShieldPayload.TYPE, ShieldPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(ChargePayload.TYPE, ChargePayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToServer(ChargeRequest.TYPE, ChargeRequest.CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) ChargeService.handle(player, payload);
                }))
                .playToClient(DamagePayload.TYPE, DamagePayload.CODEC, (payload, context) -> context.enqueueWork(() -> DamagePayload.receive(payload)))
                .playToClient(ProfilePayload.TYPE, ProfilePayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(PanelPayload.TYPE, PanelPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(TooltipPayload.TYPE, TooltipPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToClient(ProgressPayload.TYPE, ProgressPayload.CODEC, (payload, context) -> context.enqueueWork(() -> ClientPackets.receive(payload)))
                .playToServer(TooltipRequest.TYPE, TooltipRequest.CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) WeaponInspectionService.handle(player, payload);
                }))
                .playToServer(PanelRequest.TYPE, PanelRequest.CODEC, (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer player) PanelService.handle(player, payload);
                }));
    }

    public static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            yagen.waitmydawn.yagens_attributes.compat.CompatSetup.register();
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
        });
    }
}
