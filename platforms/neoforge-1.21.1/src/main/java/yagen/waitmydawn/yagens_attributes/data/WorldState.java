package yagen.waitmydawn.yagens_attributes.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import yagen.waitmydawn.yagens_attributes.core.*;
import java.util.*;

public final class WorldState extends SavedData {
    public record Visibility(boolean seeOthers, boolean shareOwn) {
        public static final Visibility DEFAULT = new Visibility(true, true);
    }
    public final ModLedger ledger = new ModLedger(this::setDirty);
    private final Map<UUID, Map<String, Bonus>> playerBonuses = new HashMap<>();
    private final Map<UUID, Visibility> visibility = new HashMap<>();
    private final Map<UUID, WeaponProgress> weaponProgress = new HashMap<>();
    private final Map<UUID, Long> endo = new HashMap<>();
    private final Map<UUID, Long> kuva = new HashMap<>();
    private final Map<UUID, RivenRoll> cycles = new HashMap<>();
    public long kuva(UUID player) { return kuva.getOrDefault(player, 0L); }
    public void kuva(UUID player, long value) {
        if (value < 0) throw new IllegalArgumentException("Negative Kuva balance");
        if (value == 0) kuva.remove(player); else kuva.put(Objects.requireNonNull(player), value); setDirty();
    }
    public void creditKuva(UUID player, long amount) { kuva(player, EndoCurrency.credit(kuva(player), amount)); }
    public RivenRoll cycle(UUID mod) { return cycles.get(mod); }
    public void cycle(UUID mod, RivenRoll value) { if (value == null) cycles.remove(mod); else cycles.put(mod, value); setDirty(); }
    private ModCatalog appliedCatalog;
    private Map<net.minecraft.resources.ResourceLocation, WeaponProfile> appliedWeapons;

    public static WorldState get(MinecraftServer server) {
        var state = server.overworld().getDataStorage().computeIfAbsent(new Factory<>(WorldState::new, WorldState::load, null), "yagens_attributes_state");
        state.refreshCatalog(ModDefinitions.INSTANCE.catalog(), CombatData.WEAPONS.profiles()); return state;
    }
    public void refreshCatalog(ModCatalog catalog) {
        refreshCatalog(catalog, Map.of());
    }
    public void refreshCatalog(ModCatalog catalog, Map<net.minecraft.resources.ResourceLocation, WeaponProfile> weapons) {
        if (catalog == ModCatalog.EMPTY || catalog == appliedCatalog && weapons == appliedWeapons) return;
        for (var entry : ledger.all()) {
            if (entry.info().defined()) {
                try {
                    var roll = entry.info().riven();
                    var card = roll == null ? catalog.card(entry.info(), entry.info().level()) : catalog.rivenCard(yagen.waitmydawn.yagens_attributes.setup.RivenService.current(roll, weapons), entry.info().level());
                    ledger.refresh(entry.id(), card.info(), card.bonuses());
                }
                catch (IllegalArgumentException exception) { ledger.refresh(entry.id(), entry.info(), Map.of()); }
            } else ledger.refresh(entry.id(), catalog.legacyInfo(entry.info()), entry.bonuses());
        }
        appliedCatalog = catalog;
        appliedWeapons = weapons;
    }

    public Map<String, Bonus> bonuses(UUID player) { return playerBonuses.getOrDefault(player, Map.of()); }
    public Visibility visibility(UUID player) { return visibility.getOrDefault(player, Visibility.DEFAULT); }
    public void visibility(UUID player, Visibility value) { visibility.put(player, value); setDirty(); }
    public WeaponProgress progress(UUID weapon) { return weaponProgress.getOrDefault(weapon, WeaponProgress.ZERO); }
    public void progress(UUID weapon, WeaponProgress progress) { weaponProgress.put(Objects.requireNonNull(weapon), Objects.requireNonNull(progress)); setDirty(); }
    public void retireWeapon(UUID weapon) {
        if (weaponProgress.remove(weapon) != null) setDirty();
        ledger.detachWeapon(weapon);
    }

    public long endo(UUID player) { return endo.getOrDefault(player, 0L); }
    public void endo(UUID player, long value) {
        Objects.requireNonNull(player);
        if (value < 0) throw new IllegalArgumentException("Negative Endo balance");
        if (value == 0) endo.remove(player); else endo.put(player, value);
        setDirty();
    }
    public void creditEndo(UUID player, long amount) { endo(player, EndoCurrency.credit(endo(player), amount)); }

    public void bonus(UUID player, String stat, Bonus bonus) {
        if (!Stats.KEYS.contains(stat)) throw new IllegalArgumentException("Unknown stat");
        var next = new LinkedHashMap<>(bonuses(player));
        if (bonus.equals(Bonus.NONE)) next.remove(stat); else next.put(stat, bonus);
        var ordered = new LinkedHashMap<String, Bonus>(); for (String key : Stats.KEYS) if (next.containsKey(key)) ordered.put(key, next.get(key));
        playerBonuses.put(player, Collections.unmodifiableMap(ordered));
        setDirty();
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putInt("schema", 7);
        var pending = new ListTag();
        cycles.forEach((id, roll) -> { var entry = new CompoundTag(); entry.putUUID("mod", id); entry.putString("roll", RivenJson.roll(roll)); pending.add(entry); });
        tag.put("cycles", pending);
        ListTag mods = new ListTag();
        for (ModLedger.Entry entry : ledger.all()) {
            CompoundTag mod = new CompoundTag();
            mod.putUUID("id", entry.id()); mod.putUUID("owner", entry.owner());
            if (entry.weapon() != null) mod.putUUID("weapon", entry.weapon());
            mod.putBoolean("shared", entry.shared());
            mod.putString("bonuses", JsonData.encodeBonuses(entry.bonuses()));
            mod.putString("name", entry.info().name()); mod.putString("category", entry.info().category().name());
            mod.putString("rarity", entry.info().rarity().name()); mod.putString("polarity", entry.info().polarity().name());
            mod.putInt("slot", entry.slot());
            mod.putInt("capacity_cost", entry.info().capacityCost());
            mod.putString("mod_details", ModJson.details(entry.info().details()));
            mods.add(mod);
        }
        tag.put("mods", mods);
        ListTag players = new ListTag();
        var ids = new HashSet<>(playerBonuses.keySet()); ids.addAll(visibility.keySet());
        ids.addAll(endo.keySet());
        ids.addAll(kuva.keySet());
        for (UUID id : ids) {
            CompoundTag player = new CompoundTag(); player.putUUID("id", id);
            player.putString("bonuses", JsonData.encodeBonuses(bonuses(id)));
            player.putLong("endo", endo(id));
            player.putLong("kuva", kuva(id));
            player.putBoolean("see_others", visibility(id).seeOthers());
            player.putBoolean("share_own", visibility(id).shareOwn()); players.add(player);
        }
        tag.put("players", players);
        ListTag weapons = new ListTag();
        weaponProgress.forEach((id, progress) -> {
            CompoundTag weapon = new CompoundTag(); weapon.putUUID("id", id);
            weapon.putInt("level", progress.level()); weapon.putInt("experience", progress.experience()); weapons.add(weapon);
        });
        tag.put("weapons", weapons);
        return tag;
    }

    static WorldState load(CompoundTag tag, HolderLookup.Provider registries) {
        int schema = tag.getInt("schema");
        if (schema < 1 || schema > 7) throw new IllegalStateException("Unsupported Yagen's Attributes data schema");
        WorldState state = new WorldState();
        for (Tag value : tag.getList("mods", Tag.TAG_COMPOUND)) {
            CompoundTag mod = (CompoundTag) value;
            UUID weapon = mod.hasUUID("weapon") ? mod.getUUID("weapon") : null;
            ModInfo info = schema == 1 ? ModInfo.DEFAULT : new ModInfo(mod.getString("name"), ModInfo.Category.valueOf(mod.getString("category")), ModInfo.Rarity.valueOf(mod.getString("rarity")), ModInfo.Polarity.valueOf(mod.getString("polarity")), schema < 3 ? 1 : mod.getInt("capacity_cost"), schema < 5 ? null : ModJson.details(mod.getString("mod_details")));
            int slot = weapon == null ? -1 : schema == 1 ? state.ledger.installed(weapon).size() : mod.getInt("slot");
            if (weapon != null && slot < 0) slot = state.ledger.installed(weapon).size();
            state.ledger.add(new ModLedger.Entry(mod.getUUID("id"), mod.getUUID("owner"), weapon,
                    mod.getBoolean("shared"), JsonData.decodeBonuses(mod.getString("bonuses")), info, slot));
        }
        for (Tag value : tag.getList("players", Tag.TAG_COMPOUND)) {
            CompoundTag player = (CompoundTag) value;
            UUID id = player.getUUID("id");
            if (schema >= 7) {
                if (!player.contains("kuva", Tag.TAG_LONG) || player.getLong("kuva") < 0) throw new IllegalStateException("Invalid Kuva balance");
                if (player.getLong("kuva") > 0) state.kuva.put(id, player.getLong("kuva"));
            }
            if (schema >= 4) {
                if (!player.contains("endo", Tag.TAG_LONG) || player.getLong("endo") < 0) throw new IllegalStateException("Invalid Endo balance");
                if (player.getLong("endo") > 0) state.endo.put(id, player.getLong("endo"));
            }
            var decoded = JsonData.decodeBonuses(player.getString("bonuses")); var ordered = new LinkedHashMap<String, Bonus>();
            for (String key : Stats.KEYS) if (decoded.containsKey(key)) ordered.put(key, decoded.get(key));
            state.playerBonuses.put(id, Collections.unmodifiableMap(ordered));
            state.visibility.put(id, new Visibility(player.getBoolean("see_others"), player.getBoolean("share_own")));
        }
        if (schema >= 3) for (Tag value : tag.getList("weapons", Tag.TAG_COMPOUND)) {
            CompoundTag weapon = (CompoundTag) value;
            UUID id = weapon.getUUID("id");
            if (state.weaponProgress.put(id, new WeaponProgress(weapon.getInt("level"), weapon.getInt("experience"))) != null)
                throw new IllegalStateException("Duplicate weapon progress UUID");
        }
        if (schema >= 7) for (Tag value : tag.getList("cycles", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) value; UUID id = entry.getUUID("mod"); var roll = RivenJson.roll(entry.getString("roll"));
            var original = state.ledger.entry(id).info().riven();
            if (original == null || original.cycles() != roll.cycles() || !original.weapon().equals(roll.weapon()) || original.polarity() != roll.polarity()
                    || state.cycles.put(id, roll) != null) throw new IllegalStateException("Invalid pending Riven cycle");
        }
        state.setDirty(schema < 7);
        return state;
    }
}
