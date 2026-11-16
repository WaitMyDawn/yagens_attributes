package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import java.util.UUID;
import yagen.waitmydawn.yagens_attributes.core.*;

public record PanelRequest(int requestId, int view, int page, int action, UUID weapon, UUID mod, int slot,
                           ModInventory.Query query, String language, long endoAmount, ModInfo.Polarity polarity) implements CustomPacketPayload {
    public PanelRequest(int requestId, int view, int page, int action, UUID weapon, UUID mod, int slot, ModInventory.Query query, String language, long endoAmount) {
        this(requestId, view, page, action, weapon, mod, slot, query, language, endoAmount, ModInfo.Polarity.NONE);
    }
    public PanelRequest(int requestId, int view, int page, int action, UUID weapon, UUID mod, int slot, ModInventory.Query query, String language) {
        this(requestId, view, page, action, weapon, mod, slot, query, language, 0);
    }
    public PanelRequest(int requestId, int view, int page, int action, UUID weapon, UUID mod) {
        this(requestId, view, page, action, weapon, mod, -1, ModInventory.Query.DEFAULT, "en_us");
    }
    public static final int STORAGE = 0, WEAPON = 1;
    public static final int REFRESH = 0, INSTALL = 1, UNINSTALL = 2, TAKE = 3, SHARE = 4, WITHDRAW_ENDO = 5, UPGRADE = 6, WITHDRAW_MOD = 7, MOVE = 8, POLARIZE = 9, WITHDRAW_KUVA = 10, OPEN_CYCLE = 11, CYCLE = 12, KEEP_OLD = 13, KEEP_NEW = 14, DROP_MOD = 15, DROP_STACK = 16, TOGGLE_EXPERIENCE = 17, RECYCLE = 18, RECYCLE_ALL = 19, RELEASE = 20;
    public static final Type<PanelRequest> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "panel_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PanelRequest> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.requestId).writeVarInt(value.view).writeVarInt(value.page).writeVarInt(value.action);
        CombatCodecs.uuid(buffer, value.weapon); CombatCodecs.uuid(buffer, value.mod);
        buffer.writeVarInt(value.slot).writeUtf(value.query.search(), 80).writeEnum(value.query.category()).writeEnum(value.query.order()).writeUtf(value.language, 16);
        buffer.writeVarLong(value.endoAmount).writeEnum(value.polarity);
        writeFilter(buffer, value.query.filter());
    }, buffer -> {
        int requestId = buffer.readVarInt(), view = buffer.readVarInt(), page = buffer.readVarInt(), action = buffer.readVarInt();
        UUID weapon = CombatCodecs.uuid(buffer), mod = CombatCodecs.uuid(buffer);
        int slot = buffer.readVarInt(); String search = buffer.readUtf(80);
        var category = buffer.readEnum(ModInfo.Category.class); var order = buffer.readEnum(ModInventory.Order.class);
        String language = buffer.readUtf(16); long amount = buffer.readVarLong(); var polarity = buffer.readEnum(ModInfo.Polarity.class);
        return new PanelRequest(requestId, view, page, action, weapon, mod, slot, new ModInventory.Query(search, category, order, readFilter(buffer)), language, amount, polarity);
    });
    public static void writeFilter(RegistryFriendlyByteBuf buffer, ModFilter filter) {
        buffer.writeVarInt(filter.groups().size());
        for (var group : filter.groups()) { buffer.writeVarInt(group.size()); for (var tag : group) buffer.writeEnum(tag); }
    }
    public static ModFilter readFilter(RegistryFriendlyByteBuf buffer) {
        int count = buffer.readVarInt(), total = 0;
        if (count < 0 || count > ModFilter.MAX_GROUPS) throw new IllegalArgumentException("Invalid filter group count");
        var groups = new java.util.ArrayList<java.util.List<ModFilter.Tag>>();
        for (int i = 0; i < count; i++) {
            int size = buffer.readVarInt(); total += size;
            if (size < 1 || size > ModFilter.Tag.values().length || total > ModFilter.MAX_TAGS) throw new IllegalArgumentException("Invalid filter tag count");
            var tags = new java.util.ArrayList<ModFilter.Tag>();
            for (int j = 0; j < size; j++) tags.add(buffer.readEnum(ModFilter.Tag.class));
            groups.add(tags);
        }
        return new ModFilter(groups);
    }
    public boolean valid() { return requestId >= 0 && view >= STORAGE && view <= WEAPON && page >= 0 && page < 1_000_000 && action >= REFRESH && action <= RELEASE && slot >= -1 && slot < 64 && (action != MOVE || slot >= 0)
            && (action != RECYCLE && action != RECYCLE_ALL || view == STORAGE && mod != null)
            && (action != RELEASE || view == STORAGE && mod != null && weapon == null && slot == -1 && endoAmount == 0)
            && (action != TOGGLE_EXPERIENCE || view == WEAPON && weapon != null && mod == null)
            && (action == POLARIZE ? view == WEAPON && weapon != null && mod == null && slot >= 0 && PolarityRules.FORMA_CHOICES.contains(polarity) : polarity == ModInfo.Polarity.NONE); }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
