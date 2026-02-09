package yagen.waitmydawn.network;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModData;
import yagen.waitmydawn.capabilities.ReservoirsInventoryHandler;
import yagen.waitmydawn.compat.ISSCompat;
import yagen.waitmydawn.config.ServerConfigs;
import yagen.waitmydawn.entity.ReservoirEntity;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.EntityRegistry;
import yagen.waitmydawn.util.SupportedMod;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record CreateReservoirPacket(int index) implements CustomPacketPayload {
    public static final Type<CreateReservoirPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "create_reservoir"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CreateReservoirPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeVarInt(pkt.index);
                    },
                    buf -> new CreateReservoirPacket(buf.readVarInt())
            );

    public static void handle(CreateReservoirPacket pkt, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            int DURATION = 20 * ServerConfigs.MOD_WARFRAME_RESERVOIRS_DURATION.get();
            float RANGE = ServerConfigs.MOD_WARFRAME_RESERVOIRS_RANGE.get().floatValue();
            ServerPlayer player = (ServerPlayer) ctx.player();
            Level level = player.level();
            ReservoirEntity reservoir = new ReservoirEntity(EntityRegistry.RESERVOIR.get(), level);
            ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
            ItemStack itemStack = ReservoirsInventoryHandler.getReservoirAtIndex(chest, pkt.index, player);

            if (itemStack.isEmpty()) return;
            boolean isModContainer = IModContainer.isModContainer(itemStack);
            boolean isSpellContainer = false;
            if (!isModContainer && ModList.get().isLoaded(SupportedMod.IRONS_SPELLBOOKS.getValue()))
                isSpellContainer = ISSCompat.isSpellContainer(itemStack);
            if (!isModContainer && !isSpellContainer) return;

            boolean isReservoirsAttributes = false;
            ComponentRegistry.ReservoirsAttributes reservoirsAttributes = chest.get(
                    ComponentRegistry.RESERVOIRS_ATTRIBUTES.get()
            );
            if (reservoirsAttributes != null) isReservoirsAttributes = true;
            double valueFactor = 1.0;
            double durationFactor = 1.0;
            double rangeFactor = 1.0;
            String typeId;
            if (pkt.index == 0) {
                typeId = "grandiflorum";
                if (isReservoirsAttributes)
                    durationFactor = durationFactor + 0.06 * reservoirsAttributes.duration();
            } else if (pkt.index == 1) {
                typeId = "pumilum";
                if (isReservoirsAttributes) valueFactor = valueFactor + 0.06 * reservoirsAttributes.strength();
            } else {
                typeId = "hydrangea";
                if (isReservoirsAttributes) rangeFactor = rangeFactor + 0.20 * reservoirsAttributes.range();
            }
            if (isModContainer) {
                Pattern p = Pattern.compile(
                        "'[^']*\\.([^.]+)_([^']+)'.*args=\\[([0-9.]+)]");
                ModData modData = IModContainer.get(itemStack).getModAtIndex(0);
                List<MutableComponent> uniqueInfo = modData.getMod().getUniqueInfo(modData.getLevel());
                for (var comp : uniqueInfo) {
                    String key = String.valueOf(comp);
                    Matcher m = p.matcher(key);
                    if (!m.find()) continue;
                    float val = Float.parseFloat(m.group(3));
                    if (m.group(0).contains("tooltips")) {
                        String attributeName = YagensAttributes.MODID + ":" + m.group(1);
                        Attribute attribute = BuiltInRegistries.ATTRIBUTE
                                .get(ResourceLocation.tryParse(attributeName));
                        if (attribute == null) {
                            attribute = BuiltInRegistries.ATTRIBUTE
                                    .get(ResourceLocation.tryParse("minecraft:generic." + m.group(1)));
                            if (attribute == null) {
                                attribute = BuiltInRegistries.ATTRIBUTE
                                        .get(ResourceLocation.tryParse("minecraft:player." + m.group(1)));
                                if (attribute == null) {
                                    continue;
                                }
                            }
                        }

                        AttributeModifier.Operation op = null;
                        switch (m.group(2)) {
                            case "add" -> op = AttributeModifier.Operation.ADD_VALUE;
                            case "addneg" -> {
                                op = AttributeModifier.Operation.ADD_VALUE;
                                val = -val;
                            }
                            case "multibase" -> {
                                op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                                val = val / 100;
                            }
                            case "multibaseneg" -> {
                                op = AttributeModifier.Operation.ADD_MULTIPLIED_BASE;
                                val = -val / 100;
                            }
                            case "multiply" -> {
                                op = AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL;
                                val = val / 100 - 1;
                            }
                        }
                        if (op == null) continue;
                        val = (float) (val * valueFactor);
                        ResourceLocation modifierId = ResourceLocation.fromNamespaceAndPath(
                                YagensAttributes.MODID,
                                typeId + "_" + m.group(1)
                        );
                        AttributeModifier modifier = new AttributeModifier(modifierId, val, op);
                        reservoir.addModifier(BuiltInRegistries.ATTRIBUTE.wrapAsHolder(attribute), modifier);
                    }
                }
            } else if (isSpellContainer) {
                reservoir.setString(ISSCompat.getReservoirName(itemStack, valueFactor));
                reservoir.setDuration(ISSCompat.getReservoirDuration(itemStack, player));
            }
            reservoir.setPos(player.getX(), player.getY() + 0.25, player.getZ());
            reservoir.setReservoirType(pkt.index);
            reservoir.setRange((float) (RANGE * rangeFactor));
            reservoir.setLifespan((int) (DURATION * durationFactor));
            if (isModContainer)
                reservoir.setDuration((int) (DURATION * durationFactor));

            level.addFreshEntity(reservoir);
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}