package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;

public record ModCatalogPayload(String json) implements CustomPacketPayload {
    public ModCatalogPayload { if (json.length() > 250000) throw new IllegalArgumentException("Mod catalog too large"); }
    public static final Type<ModCatalogPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "mod_catalog"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ModCatalogPayload> CODEC = StreamCodec.of((buffer, value) -> buffer.writeUtf(value.json, 250000), buffer -> new ModCatalogPayload(buffer.readUtf(250000)));
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
