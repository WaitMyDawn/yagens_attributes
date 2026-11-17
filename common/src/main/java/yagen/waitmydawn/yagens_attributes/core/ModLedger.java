package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/**
 * The ledger is the sole authority for ownership, including offline players. Server-thread only.
 */
public final class ModLedger {
    private final Runnable onChange;

    public ModLedger() {
        this(() -> {
        });
    }

    public ModLedger(Runnable onChange) {
        this.onChange = Objects.requireNonNull(onChange);
    }

    public record Entry(UUID id, UUID owner, UUID weapon, boolean shared, Map<String, Bonus> bonuses, ModInfo info,
                        int slot) {
        public Entry(UUID id, UUID owner, UUID weapon, boolean shared, Map<String, Bonus> bonuses) {
            this(id, owner, weapon, shared, bonuses, ModInfo.DEFAULT, -1);
        }

        public Entry {
            Objects.requireNonNull(id);
            Objects.requireNonNull(owner);
            Objects.requireNonNull(info);
            if (slot < -1 || slot >= 64 || weapon == null && slot != -1)
                throw new IllegalArgumentException("Invalid Mod slot");
            bonuses = Collections.unmodifiableMap(new LinkedHashMap<>(bonuses));
            if (!Stats.KEYS.containsAll(bonuses.keySet())) throw new IllegalArgumentException("Unknown stat");
        }
    }

    private final Map<UUID, Entry> entries = new LinkedHashMap<>();
    private final Map<UUID, Set<UUID>> byWeapon = new HashMap<>();
    private final Map<UUID, Set<UUID>> byOwner = new HashMap<>();
    private final Map<UUID, List<Map<String, Bonus>>> weaponCache = new HashMap<>();
    private record WeaponKey(String item, ModInfo.Category category) {}
    private final Map<UUID, Map<WeaponKey, List<Map<String, Bonus>>>> itemCache = new HashMap<>();

    public void add(Entry entry) {
        if (entries.containsKey(entry.id())) throw new IllegalArgumentException("Duplicate Mod UUID");
        if (entry.weapon() != null) {
            var occupied = new HashSet<Integer>();
            for (var mod : installed(entry.weapon())) occupied.add(mod.slot());
            int slot = entry.slot();
            if (slot == -1) {
                slot = 0;
                while (occupied.contains(slot)) slot++;
            }
            if (occupied.contains(slot) || slot >= 64)
                throw new IllegalArgumentException("Duplicate or invalid installed slot");
            entry = new Entry(entry.id(), entry.owner(), entry.weapon(), entry.shared(), entry.bonuses(), entry.info(), slot);
        }
        put(entry);
    }

    public Collection<Entry> all() {
        return List.copyOf(entries.values());
    }

    public List<Entry> owned(UUID owner) {
        return view(byOwner.getOrDefault(owner, Set.of()));
    }

    public List<Entry> installed(UUID weapon) {
        return view(byWeapon.getOrDefault(weapon, Set.of())).stream().sorted(Comparator.comparingInt(Entry::slot)).toList();
    }

    public List<Map<String, Bonus>> bonuses(UUID weapon) {
        if (!byWeapon.containsKey(weapon)) return List.of();
        return weaponCache.computeIfAbsent(weapon, id -> installed(id).stream().map(Entry::bonuses).toList());
    }
    public List<Map<String, Bonus>> bonuses(UUID weapon, String item) {
        return bonuses(weapon, item, null);
    }
    /** Rechecks current applicability so catalog changes cannot keep obsolete installed bonuses active. */
    public List<Map<String, Bonus>> bonuses(UUID weapon, String item, ModInfo.Category category) {
        if (!byWeapon.containsKey(weapon)) return List.of();
        var cache = itemCache.computeIfAbsent(weapon, ignored -> new HashMap<>());
        var key = new WeaponKey(item, category);
        if (!cache.containsKey(key) && cache.size() >= 8) cache.clear();
        return cache.computeIfAbsent(key, ignored -> installed(weapon).stream()
                .filter(entry -> entry.info().compatibleItem(item) && (category == null || entry.info().compatible(category)))
                .map(Entry::bonuses).toList());
    }

    public void install(UUID actor, UUID mod, UUID weapon) {
        install(actor, mod, weapon, ModInfo.Category.WEAPON, 64, -1);
    }

    public void install(UUID actor, UUID mod, UUID weapon, ModInfo.Category category, int capacity, int requestedSlot) {
        install(actor, mod, weapon, category, capacity, requestedSlot, Integer.MAX_VALUE, false);
    }

    public void install(UUID actor, UUID mod, UUID weapon, ModInfo.Category category, int capacity, int requestedSlot, int modCapacity, boolean creative) {
        install(actor, mod, weapon, category, capacity, requestedSlot, modCapacity, creative, Collections.nCopies(Math.max(0, Math.min(64, capacity)), ModInfo.Polarity.NONE));
    }

    public void install(UUID actor, UUID mod, UUID weapon, ModInfo.Category category, int capacity, int requestedSlot, int modCapacity, boolean creative, List<ModInfo.Polarity> polarities) {
        install(actor, mod, weapon, category, capacity, requestedSlot, modCapacity, creative, polarities, null);
    }
    public void install(UUID actor, UUID mod, UUID weapon, ModInfo.Category category, int capacity, int requestedSlot, int modCapacity, boolean creative, List<ModInfo.Polarity> polarities, String item) {
        Entry entry = require(mod);
        if (!entry.owner().equals(actor) || entry.weapon() != null)
            throw new IllegalArgumentException("Mod must be owned and unoccupied");
        if (!entry.info().compatible(category))
            throw new IllegalArgumentException("Mod category is incompatible with weapon");
        if (!entry.info().compatibleItem(item)) throw new IllegalArgumentException("gui.yagens_attributes.riven_wrong_weapon");
        if (installed(weapon).stream().anyMatch(other -> entry.info().conflicts(other.info(), category)))
            throw new IllegalArgumentException("gui.yagens_attributes.mod_conflict");
        if (capacity < 1 || capacity > 64) throw new IllegalArgumentException("Invalid slot count");
        var occupied = new HashSet<Integer>();
        for (Entry installed : installed(weapon)) occupied.add(installed.slot());
        int slot = requestedSlot;
        if (slot == -1) {
            slot = 0;
            while (slot < capacity && occupied.contains(slot)) slot++;
        }
        if (slot < 0 || slot >= capacity || occupied.contains(slot) || installed(weapon).size() >= capacity)
            throw new IllegalArgumentException("Selected slot is occupied or weapon is full");
        if (!creative && (long) usedCapacity(weapon, polarities) + PolarityRules.cost(entry.info(), PolarityRules.slot(polarities, slot)) > modCapacity)
            throw new IllegalArgumentException("Insufficient Mod capacity");
        replace(entry, new Entry(mod, actor, Objects.requireNonNull(weapon), entry.shared(), entry.bonuses(), entry.info(), slot));
    }

    /** Ownership authorizes remote release; holder permissions never grant this operation. */
    public void release(UUID actor, UUID mod) {
        Entry entry = require(mod);
        if (!entry.owner().equals(actor)) throw new IllegalArgumentException("gui.yagens_attributes.release_not_owner");
        if (entry.weapon() == null) throw new IllegalArgumentException("gui.yagens_attributes.release_not_installed");
        replace(entry, new Entry(mod, entry.owner(), null, entry.shared(), entry.bonuses(), entry.info(), -1));
    }

    public void uninstall(UUID actor, UUID mod, UUID heldWeapon, boolean take) {
        Entry entry = require(mod);
        if (!Objects.equals(entry.weapon(), heldWeapon) || heldWeapon == null)
            throw new IllegalArgumentException("Mod is not installed on the held weapon");
        if (!entry.owner().equals(actor) && !entry.shared())
            throw new IllegalArgumentException("Owner disallows access");
        replace(entry, new Entry(mod, take ? actor : entry.owner(), null, entry.shared(), entry.bonuses(), entry.info(), -1));
    }

    public void move(UUID actor, UUID mod, UUID heldWeapon, int targetSlot, int slots) {
        move(actor, mod, heldWeapon, targetSlot, slots, Integer.MAX_VALUE, false, List.of());
    }

    public void move(UUID actor, UUID mod, UUID heldWeapon, int targetSlot, int slots, int capacity, boolean creative, List<ModInfo.Polarity> polarities) {
        Entry source = require(mod);
        if (heldWeapon == null || !heldWeapon.equals(source.weapon()) || targetSlot < 0 || targetSlot >= slots || slots > 64)
            throw new IllegalArgumentException("Invalid weapon or destination slot");
        if (!source.owner().equals(actor) && !source.shared()) throw new IllegalArgumentException("Owner disallows moving this Mod");
        if (source.slot() == targetSlot) return;
        Entry target = installed(heldWeapon).stream().filter(entry -> entry.slot() == targetSlot).findFirst().orElse(null);
        if (target != null && !target.owner().equals(actor) && !target.shared()) throw new IllegalArgumentException("Owner disallows swapping the target Mod");
        int used = usedCapacity(heldWeapon, polarities);
        int next = PolarityRules.movedCapacity(used, source.info(), source.slot(), target == null ? null : target.info(), targetSlot, polarities);
        if (!creative && next > capacity && next > used) throw new IllegalArgumentException("Insufficient Mod capacity for move");
        // Ownership and index membership do not change. Publish both slots before notifying observers.
        entries.put(mod, new Entry(mod, source.owner(), heldWeapon, source.shared(), source.bonuses(), source.info(), targetSlot));
        if (target != null) entries.put(target.id(), new Entry(target.id(), target.owner(), heldWeapon, target.shared(), target.bonuses(), target.info(), source.slot()));
        weaponCache.remove(heldWeapon); itemCache.remove(heldWeapon); onChange.run();
    }

    public void share(UUID actor, UUID mod, boolean shared) {
        Entry entry = require(mod);
        if (!entry.owner().equals(actor)) throw new IllegalArgumentException("Only owner can change permission");
        replace(entry, new Entry(mod, actor, entry.weapon(), shared, entry.bonuses(), entry.info(), entry.slot()));
    }

    public void metadata(UUID mod, ModInfo info) {
        Entry entry = require(mod);
        if (entry.info().defined()) throw new IllegalArgumentException("Defined Mod metadata is controlled by its JSON definition");
        replace(entry, new Entry(mod, entry.owner(), entry.weapon(), entry.shared(), entry.bonuses(), info, entry.slot()));
    }

    public void refresh(UUID mod, ModInfo info, Map<String, Bonus> bonuses) {
        Entry entry = require(mod);
        replace(entry, new Entry(mod, entry.owner(), entry.weapon(), entry.shared(), bonuses, info, entry.slot()));
    }

    public void upgrade(UUID actor, UUID mod, ModInfo info, Map<String, Bonus> bonuses, int capacity, boolean creative, UUID heldWeapon) {
        upgrade(actor, mod, info, bonuses, capacity, creative, heldWeapon, List.of());
    }

    public void upgrade(UUID actor, UUID mod, ModInfo info, Map<String, Bonus> bonuses, int capacity, boolean creative, UUID heldWeapon, List<ModInfo.Polarity> polarities) {
        Entry entry = require(mod);
        if (!entry.owner().equals(actor) && (!entry.shared() || entry.weapon() == null || !entry.weapon().equals(heldWeapon)))
            throw new IllegalArgumentException("Owner disallows upgrading this Mod");
        if (!entry.info().defined() || !info.defined() || !entry.info().details().type().equals(info.details().type()) || !Objects.equals(entry.info().riven(), info.riven()) || info.level() != entry.info().level() + 1)
            throw new IllegalArgumentException("Invalid Mod upgrade");
        var polarity = PolarityRules.slot(polarities, entry.slot());
        if (entry.weapon() != null && !creative && (long) usedCapacity(entry.weapon(), polarities) - PolarityRules.cost(entry.info(), polarity) + PolarityRules.cost(info, polarity) > capacity)
            throw new IllegalArgumentException("Insufficient Mod capacity for upgrade");
        refresh(mod, info, bonuses);
    }

    public Entry withdraw(UUID actor, UUID mod) {
        Entry entry = require(mod);
        if (!entry.owner().equals(actor) || entry.weapon() != null) throw new IllegalArgumentException("Only owned, uninstalled Mods can be withdrawn");
        byOwner.get(actor).remove(mod);
        if (byOwner.get(actor).isEmpty()) byOwner.remove(actor);
        entries.remove(mod); onChange.run(); return entry;
    }

    public Entry entry(UUID id) {
        return require(id);
    }

    public int usedCapacity(UUID weapon) {
        return usedCapacity(weapon, List.of());
    }

    public int usedCapacity(UUID weapon, List<ModInfo.Polarity> polarities) {
        return installed(weapon).stream().mapToInt(entry -> PolarityRules.cost(entry.info(), PolarityRules.slot(polarities, entry.slot()))).sum();
    }

    public void validatePolarization(UUID actor, UUID weapon) {
        Objects.requireNonNull(actor); Objects.requireNonNull(weapon);
        if (installed(weapon).stream().anyMatch(entry -> !entry.owner().equals(actor) && !entry.shared()))
            throw new IllegalArgumentException("gui.yagens_attributes.polarize_locked_mod");
    }

    public void clearForPolarization(UUID actor, UUID weapon) {
        validatePolarization(actor, weapon);
        for (var entry : installed(weapon)) uninstall(actor, entry.id(), weapon, false);
    }

    public int slotCount(UUID weapon, int configured) {
        int count = configured;
        for (Entry entry : installed(weapon)) count = Math.max(count, entry.slot() + 1);
        return Math.min(64, count);
    }

    private Entry require(UUID id) {
        Entry entry = entries.get(id);
        if (entry == null) throw new IllegalArgumentException("Unknown Mod UUID");
        return entry;
    }

    private List<Entry> view(Set<UUID> ids) {
        return ids.stream().map(entries::get).toList();
    }

    private void replace(Entry old, Entry next) {
        byOwner.get(old.owner()).remove(old.id());
        if (byOwner.get(old.owner()).isEmpty()) byOwner.remove(old.owner());
        if (old.weapon() != null) {
            byWeapon.get(old.weapon()).remove(old.id());
            if (byWeapon.get(old.weapon()).isEmpty()) byWeapon.remove(old.weapon());
            weaponCache.remove(old.weapon());
            itemCache.remove(old.weapon());
        }
        put(next);
    }

    private void put(Entry entry) {
        entries.put(entry.id(), entry);
        byOwner.computeIfAbsent(entry.owner(), id -> new LinkedHashSet<>()).add(entry.id());
        if (entry.weapon() != null) {
            byWeapon.computeIfAbsent(entry.weapon(), id -> new LinkedHashSet<>()).add(entry.id());
            weaponCache.remove(entry.weapon());
            itemCache.remove(entry.weapon());
        }
        onChange.run();
    }
}
