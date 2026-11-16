package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.network.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;

@EventBusSubscriber(modid = YagensAttributes.MODID)
public final class PanelService {
    private record Budget(int tick, int requests) {}
    private static final Map<ServerPlayer, Budget> BUDGETS = new WeakHashMap<>();
    private PanelService() {}

    @SubscribeEvent public static void sync(OnDatapackSyncEvent event) {
        var entries = new ArrayList<>(CombatData.WEAPONS.profiles().entrySet());
        event.getRelevantPlayers().forEach(player -> {
            WorldState.get(player.server);
            PacketDistributor.sendToPlayer(player, new ModCatalogPayload(ModJson.GSON.toJson(ModJson.catalog(ModDefinitions.INSTANCE.catalog()))));
            for (int start = 0; start < Math.max(1, entries.size()); start += ProfilePayload.CHUNK_SIZE) {
                int end = Math.min(entries.size(), start + ProfilePayload.CHUNK_SIZE);
                var chunk = new LinkedHashMap<net.minecraft.resources.ResourceLocation, WeaponProfile>();
                for (int i = start; i < end; i++) chunk.put(entries.get(i).getKey(), entries.get(i).getValue());
                PacketDistributor.sendToPlayer(player, new ProfilePayload(start == 0, end == entries.size(), Map.copyOf(chunk)));
            }
        });
    }

    public static void handle(ServerPlayer player, PanelRequest request) {
        if (!request.valid()) return;
        int tick = player.server.getTickCount();
        Budget previous = BUDGETS.get(player);
        int used = previous == null || tick - previous.tick() >= 20 ? 0 : previous.requests();
        if (used >= 20) return;
        BUDGETS.put(player, new Budget(used == 0 ? tick : previous.tick(), used + 1));
        String error = "";
        WorldState state = WorldState.get(player.server);
        ItemStack held = player.getMainHandItem();
        WeaponProfile profile = CombatData.WEAPONS.weapon(held);
        UUID weapon = WeaponEligibility.operable(held, profile) ? Components.identify(held) : null;
        profile = WeaponPolarityData.profile(held, profile, state.ledger);
        try {
            if (request.action() == PanelRequest.RELEASE) state.ledger.release(player.getUUID(), request.mod());
            else if (request.action() == PanelRequest.RECYCLE || request.action() == PanelRequest.RECYCLE_ALL) {
                ModTransactions.recycle(player, state, request.mod(), request.endoAmount(), request.action() == PanelRequest.RECYCLE_ALL);
            }
            else if (request.action() == PanelRequest.TOGGLE_EXPERIENCE) {
                validateWeapon(request.weapon(), weapon);
                held.set(Components.ABSORB_EXPERIENCE.get(), !Components.absorbsExperience(held));
                player.getInventory().setChanged(); player.containerMenu.broadcastChanges(); player.inventoryMenu.broadcastChanges();
            }
            else if (request.action() == PanelRequest.WITHDRAW_ENDO) EndoTransactions.withdraw(player, state, request.endoAmount());
            else if (request.action() == PanelRequest.WITHDRAW_KUVA) EndoTransactions.withdraw(player, state, request.endoAmount(), true);
            else if (request.action() >= PanelRequest.OPEN_CYCLE && request.action() <= PanelRequest.KEEP_NEW) {
                RivenService.editable(player, state, request.mod());
                if (request.action() == PanelRequest.CYCLE) RivenService.cycle(player, state, request.mod());
                else if (request.action() == PanelRequest.KEEP_OLD || request.action() == PanelRequest.KEEP_NEW) RivenService.choose(player, state, request.mod(), request.action() == PanelRequest.KEEP_NEW);
            }
            else if (request.action() == PanelRequest.DROP_MOD || request.action() == PanelRequest.DROP_STACK) ModTransactions.drop(player, state, request.mod(), request.action() == PanelRequest.DROP_STACK);
            else if (request.action() == PanelRequest.POLARIZE) ModTransactions.polarize(player, state, request.weapon(), request.slot(), request.polarity());
            else if (request.action() == PanelRequest.UPGRADE || request.action() == PanelRequest.WITHDRAW_MOD) {
                if (request.mod() == null) throw new IllegalArgumentException("No Mod selected");
                if (request.action() == PanelRequest.UPGRADE) ModTransactions.upgrade(player, state, request.mod(), weapon);
                else ModTransactions.withdraw(player, state, request.mod());
            }
            else if (request.action() != PanelRequest.REFRESH) {
                validateWeapon(request.weapon(), weapon);
                if (request.mod() == null) throw new IllegalArgumentException("No Mod selected");
                switch (request.action()) {
                    case PanelRequest.INSTALL -> {
                        var entry = state.ledger.entry(request.mod());
                        if (entry.info().defined()) ModDefinitions.INSTANCE.catalog().card(entry.info(), entry.info().level());
                        state.ledger.install(player.getUUID(), request.mod(), weapon, profile.category(), profile.slots().size(), request.slot(), Components.capacity(held, state.progress(weapon)), player.isCreative(), profile.slots(), net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString());
                    }
                    case PanelRequest.UNINSTALL -> state.ledger.uninstall(player.getUUID(), request.mod(), weapon, false);
                    case PanelRequest.TAKE -> state.ledger.uninstall(player.getUUID(), request.mod(), weapon, true);
                    case PanelRequest.MOVE -> state.ledger.move(player.getUUID(), request.mod(), weapon, request.slot(), profile.slots().size(), Components.capacity(held, state.progress(weapon)), player.isCreative(), profile.slots());
                    case PanelRequest.SHARE -> {
                        var entry = state.ledger.installed(weapon).stream().filter(mod -> mod.id().equals(request.mod())).findFirst().orElseThrow(() -> new IllegalArgumentException("Mod is no longer installed"));
                        state.ledger.share(player.getUUID(), entry.id(), !entry.shared());
                    }
                    default -> throw new IllegalArgumentException("Unknown panel action");
                }
            }
        } catch (IllegalArgumentException exception) { error = exception.getMessage(); }
        profile = WeaponPolarityData.profile(held, profile, state.ledger);

        Map<String, Double> resolved = Map.of();
        if (profile != null) {
            try { resolved = WeaponInspectionService.describe(player, held, profile).resolved(); }
            catch (IllegalArgumentException exception) {
                // A committed transaction must not be reported as failed because unrelated stats overflow.
                if (request.action() == PanelRequest.REFRESH && error.isEmpty()) error = exception.getMessage();
            }
        }
        List<ModLedger.Entry> owned = state.ledger.owned(player.getUUID()).stream().filter(entry -> request.view() != PanelRequest.WEAPON || entry.info().compatibleItem(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString())).toList();
        var collation = java.text.Collator.getInstance(request.language().equals("zh_cn") ? Locale.SIMPLIFIED_CHINESE : Locale.ENGLISH);
        List<ModInventory.Group> entries = ModInventory.browse(owned, request.query(), request.view() == PanelRequest.WEAPON ? profile == null ? ModInfo.Category.ALL : profile.category() : null,
                entry -> ModNames.name(request.language(), entry), collation::compare);
        int pageSize = request.view() == PanelRequest.WEAPON ? yagen.waitmydawn.yagens_attributes.core.ui.PanelGeometry.COMPATIBLE_PAGE_SIZE : PanelPayload.PAGE_SIZE;
        int pages = Math.max(1, (entries.size() + pageSize - 1) / pageSize);
        int page = Math.min(request.page(), pages - 1);
        var mods = entries.subList(page * pageSize, Math.min(entries.size(), (page + 1) * pageSize)).stream()
                .map(group -> PanelPayload.ModView.of(group.representative(), group.name(), group.quantity(), group.available())).toList();
        var installed = weapon == null ? List.<PanelPayload.ModView>of() : state.ledger.installed(weapon).stream()
                .map(entry -> PanelPayload.ModView.of(entry, ModNames.name(request.language(), entry), 1, 0)).toList();
        int slots = profile == null ? 0 : state.ledger.slotCount(weapon, profile.slots().size());
        Component name = held.isEmpty() ? Component.translatable("gui.yagens_attributes.no_weapon") : held.getHoverName().copy();
        var ownedRivens = state.ledger.owned(player.getUUID()).stream().filter(entry -> entry.info().riven() != null).toList();
        String itemId = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(held.getItem()).toString();
        boolean hasWeaponRiven = ownedRivens.stream().anyMatch(entry -> entry.info().compatibleItem(itemId)) || installed.stream().anyMatch(entry -> entry.info().riven() != null && entry.info().compatibleItem(itemId));
        boolean hasRiven = !ownedRivens.isEmpty() || installed.stream().anyMatch(entry -> entry.info().riven() != null);
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            var item = player.getInventory().getItem(i);
            if (item.getItem() instanceof yagen.waitmydawn.yagens_attributes.item.UnknownRivenItem) hasRiven = true;
            var roll = item.get(Components.RIVEN_ROLL.get());
            if (roll != null) { hasRiven = true; if (roll.weapon().equals(itemId)) hasWeaponRiven = true; }
        }
        var cycleViews = new ArrayList<PanelPayload.ModView>();
        if (request.mod() != null) try {
            var entry = RivenService.editable(player, state, request.mod());
            cycleViews.add(PanelPayload.ModView.of(entry, ModNames.name(request.language(), entry), 1, entry.weapon() == null ? 1 : 0));
            var candidate = state.cycle(entry.id());
            if (candidate != null) {
                var card = ModDefinitions.INSTANCE.catalog().rivenCard(RivenService.current(candidate, CombatData.WEAPONS.profiles()), entry.info().level());
                cycleViews.add(PanelPayload.ModView.of(new ModLedger.Entry(entry.id(), entry.owner(), entry.weapon(), entry.shared(), card.bonuses(), card.info(), entry.slot()), ModNames.name(request.language(), entry), 1, 0));
            }
        } catch (IllegalArgumentException ignored) { }
        PacketDistributor.sendToPlayer(player, new PanelPayload(request.requestId(), request.view(), page, pages, entries.size(), name, weapon, profile, resolved, mods, installed, slots,
                entries.stream().mapToInt(ModInventory.Group::quantity).sum(), held.copy(), state.progress(weapon), state.ledger.usedCapacity(weapon, profile == null ? List.of() : profile.slots()), player.isCreative(), state.endo(player.getUUID()), EndoTransactions.availableSpace(player), error, state.kuva(player.getUUID()), EndoTransactions.availableSpace(player, true), hasRiven, hasWeaponRiven, !cycleViews.isEmpty() && state.cycle(request.mod()) != null, List.copyOf(cycleViews)));
    }

    public static void validateWeapon(UUID expected, UUID actual) {
        if (expected == null || !expected.equals(actual)) throw new IllegalArgumentException("Held weapon changed; refresh the panel");
    }

    public static Map<String, Double> resolve(WeaponProfile profile, List<Map<String, Bonus>> sources) {
        return resolve(profile, sources, 5);
    }
    public static Map<String, Double> resolve(WeaponProfile profile, List<Map<String, Bonus>> sources, double comboDuration) {
        var result = new HashMap<String, Double>();
        result.put(Stats.CRITICAL_CHANCE, Math.min(100, Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, sources)));
        result.put(Stats.CRITICAL_DAMAGE, Math.max(1, Stats.resolve(profile.criticalDamage(), Stats.CRITICAL_DAMAGE, sources)));
        result.put(Stats.STATUS_CHANCE, Math.min(64, Stats.resolve(profile.statusChance(), Stats.STATUS_CHANCE, sources)));
        result.put(Stats.STATUS_DURATION, Math.min(3600, Stats.resolve(profile.statusDuration(), Stats.STATUS_DURATION, sources)));
        result.put(Stats.DAMAGE, Stats.resolve(1, Stats.DAMAGE, sources));
        result.put(Stats.FIRE_RATE, RangedStats.value(profile.fireRate(), Stats.FIRE_RATE, sources));
        result.put(Stats.MULTISHOT, RangedStats.value(profile.multishot(), Stats.MULTISHOT, sources));
        result.put(Stats.COMBO_DURATION, Stats.resolve(comboDuration, Stats.COMBO_DURATION, sources));
        result.put(Stats.COMBO_COUNT_CHANCE, Math.min(64, Stats.resolve(1, Stats.COMBO_COUNT_CHANCE, sources)));
        result.put(Stats.ATTACK_SPEED, Stats.resolve(1, Stats.ATTACK_SPEED, sources));
        result.put(Stats.MELEE_RANGE, Stats.resolve(0, Stats.MELEE_RANGE, sources));
        Combat.components(profile.baseDamage(), profile, sources).forEach((type, value) -> result.put("damage." + type.key(), value));
        return Map.copyOf(result);
    }
}
