package yagen.waitmydawn.yagens_attributes.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public record PanelPayload(int requestId, int view, int page, int pages, int total, Component weaponName, UUID weaponId,
                           WeaponProfile profile, Map<String, Double> resolved, List<ModView> mods,
                           List<ModView> installed, int slotCount, int copies, ItemStack weaponStack,
                           WeaponProgress progress, int usedCapacity, boolean creative, long endo, int endoSpace, String error,
                           long kuva, int kuvaSpace, boolean hasRiven, boolean hasWeaponRiven, boolean pendingCycle, List<ModView> cycleViews) implements CustomPacketPayload {
    public PanelPayload(int requestId, int view, int page, int pages, int total, Component weaponName, UUID weaponId, WeaponProfile profile, Map<String, Double> resolved, List<ModView> mods, List<ModView> installed, int slotCount, int copies, ItemStack weaponStack, WeaponProgress progress, int usedCapacity, boolean creative, long endo, int endoSpace, String error) {
        this(requestId, view, page, pages, total, weaponName, weaponId, profile, resolved, mods, installed, slotCount, copies, weaponStack, progress, usedCapacity, creative, endo, endoSpace, error, 0, 0, false, false, false, List.of());
    }
    public static final int PAGE_SIZE = yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.STORAGE_PAGE_SIZE;
    public record ModifierView(double addin, double addition, String multiply, double addout, double inputAddin, double inputAddition, boolean multiplyNegative) {
        public ModifierView(double addin, double addition, String multiply, double addout, double inputAddin, double inputAddition) { this(addin, addition, multiply, addout, inputAddin, inputAddition, false); }
        public ModifierView(double addin, double addition, String multiply, double addout) { this(addin, addition, multiply, addout, 0, 0); }
        public ModifierView(double addin, double addition, String multiply, double addout, double inputAddin) { this(addin, addition, multiply, addout, inputAddin, 0); }
        public static ModifierView of(Bonus bonus) {
            StringJoiner factors = new StringJoiner(" × ");
            for (int i = 0; i < Math.min(8, bonus.multiply().size()); i++) factors.add(new java.math.BigDecimal(String.format(Locale.ROOT, "%.4g", bonus.multiply().get(i))).stripTrailingZeros().toString());
            if (bonus.multiply().size() > 8) factors.add("… (+" + (bonus.multiply().size() - 8) + ")");
            return new ModifierView(bonus.addin(), bonus.addition(), factors.toString(), bonus.addout(), bonus.inputAddin(), bonus.inputAddition(), bonus.multiply().stream().anyMatch(value -> value == 0) || bonus.multiply().stream().filter(value -> value > 0).mapToDouble(Math::log).sum() < 0);
        }
    }
    public record ModView(UUID id, UUID owner, UUID weapon, boolean shared, Map<String, ModifierView> bonuses,
                          ModInfo info, String name, int slot, int quantity, int available) {
        public static ModView of(ModLedger.Entry entry) {
            return of(entry, entry.info().name(), 1, entry.weapon() == null ? 1 : 0);
        }
        public static ModView of(ModLedger.Entry entry, String name, int quantity, int available) {
            var result = new HashMap<String, ModifierView>();
            entry.bonuses().forEach((key, bonus) -> result.put(key, ModifierView.of(bonus)));
            return new ModView(entry.id(), entry.owner(), entry.weapon(), entry.shared(), Map.copyOf(result), entry.info(), name, entry.slot(), quantity, available);
        }
    }
    public static final Type<PanelPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "panel"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PanelPayload> CODEC = StreamCodec.of((buffer, value) -> {
        buffer.writeVarInt(value.requestId).writeVarInt(value.view).writeVarInt(value.page).writeVarInt(value.pages).writeVarInt(value.total);
        ComponentSerialization.STREAM_CODEC.encode(buffer, value.weaponName);
        CombatCodecs.uuid(buffer, value.weaponId);
        buffer.writeBoolean(value.profile != null);
        if (value.profile != null) CombatCodecs.profile(buffer, value.profile);
        CombatCodecs.resolved(buffer, value.resolved);
        writeMods(buffer, value.mods); writeMods(buffer, value.installed);
        buffer.writeVarInt(value.slotCount).writeVarInt(value.copies);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, value.weaponStack);
        CombatCodecs.progress(buffer, value.progress); buffer.writeVarInt(value.usedCapacity).writeBoolean(value.creative);
        writeEndo(buffer, value.endo, value.endoSpace);
        buffer.writeUtf(value.error, 256); writeEndo(buffer, value.kuva, value.kuvaSpace); buffer.writeBoolean(value.hasRiven).writeBoolean(value.hasWeaponRiven).writeBoolean(value.pendingCycle); writeMods(buffer, value.cycleViews);
    }, buffer -> {
        int requestId = buffer.readVarInt(), view = buffer.readVarInt(), page = buffer.readVarInt(), pages = buffer.readVarInt(), total = buffer.readVarInt();
        Component weaponName = ComponentSerialization.STREAM_CODEC.decode(buffer);
        UUID weaponId = CombatCodecs.uuid(buffer);
        WeaponProfile profile = buffer.readBoolean() ? CombatCodecs.profile(buffer) : null;
        var resolved = CombatCodecs.resolved(buffer);
        var mods = readMods(buffer, PAGE_SIZE);
        var installed = readMods(buffer, 64);
        int slots = CombatCodecs.count(buffer, 64), copies = buffer.readVarInt();
        ItemStack stack = ItemStack.OPTIONAL_STREAM_CODEC.decode(buffer);
        var progress = CombatCodecs.progress(buffer); int used = buffer.readVarInt(); boolean creative = buffer.readBoolean();
        if (used < 0 || used > 80_000_000) throw new IllegalArgumentException("Invalid capacity");
        var endo = readEndo(buffer);
        String error = buffer.readUtf(256); var kuva = readEndo(buffer); boolean hasRiven = buffer.readBoolean(), hasWeaponRiven = buffer.readBoolean(), pendingCycle = buffer.readBoolean(); var cycles = readMods(buffer, 2);
        return new PanelPayload(requestId, view, page, pages, total, weaponName, weaponId, profile, resolved, mods, installed, slots, copies, stack, progress, used, creative, endo.balance(), endo.space(), error, kuva.balance(), kuva.space(), hasRiven, hasWeaponRiven, pendingCycle, cycles);
    });

    public record EndoView(long balance, int space) {
        public EndoView { validateEndo(balance, space); }
    }
    public static EndoView readEndo(RegistryFriendlyByteBuf buffer) { return new EndoView(buffer.readVarLong(), buffer.readVarInt()); }
    public static void validateEndo(long balance, int space) {
        if (balance < 0 || space < 0 || space > 1_000_000) throw new IllegalArgumentException("Invalid Endo summary");
    }
    public static void writeEndo(RegistryFriendlyByteBuf buffer, long balance, int space) {
        validateEndo(balance, space); buffer.writeVarLong(balance).writeVarInt(space);
    }

    public static void writeMods(RegistryFriendlyByteBuf buffer, List<ModView> mods) {
        buffer.writeVarInt(mods.size());
        for (ModView mod : mods) {
            buffer.writeUUID(mod.id).writeUUID(mod.owner); CombatCodecs.uuid(buffer, mod.weapon);
            buffer.writeBoolean(mod.shared); CombatCodecs.bonuses(buffer, mod.bonuses);
            PanelModCodec.write(buffer, mod.info);
            buffer.writeUtf(mod.name, 80).writeVarInt(mod.slot).writeVarInt(mod.quantity).writeVarInt(mod.available);
        }
    }

    public static List<ModView> readMods(RegistryFriendlyByteBuf buffer, int maximum) {
        int count = CombatCodecs.count(buffer, maximum);
        List<ModView> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UUID id = buffer.readUUID(), owner = buffer.readUUID(), target = CombatCodecs.uuid(buffer);
            boolean shared = buffer.readBoolean(); var bonuses = CombatCodecs.bonuses(buffer);
            var info = PanelModCodec.read(buffer);
            String name = buffer.readUtf(80); int slot = buffer.readVarInt(), quantity = buffer.readVarInt(), available = buffer.readVarInt();
            if (slot < -1 || slot >= 64 || quantity < 1 || available < 0 || available > quantity) throw new IllegalArgumentException("Invalid Mod summary");
            result.add(new ModView(id, owner, target, shared, bonuses, info, name, slot, quantity, available));
        }
        return List.copyOf(result);
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
