package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

public final class RemoteReleaseTests {
    private static int checks;
    public static void run() {
        UUID owner = UUID.randomUUID(), holder = UUID.randomUUID(), weapon = UUID.randomUUID();
        int[] changes = {0}; var ledger = new ModLedger(() -> changes[0]++);
        var info = new ModInfo("Irregular instrument", ModInfo.Category.MELEE, ModInfo.Rarity.RARE, ModInfo.Polarity.NARAMON);
        var bonuses = Map.of(Stats.DAMAGE, new Bonus(0, .9, List.of(), 0));
        UUID free = UUID.randomUUID(), reserved = UUID.randomUUID(), other = UUID.randomUUID();
        ledger.add(new ModLedger.Entry(free, owner, null, false, bonuses, info, -1));
        ledger.add(new ModLedger.Entry(reserved, owner, weapon, true, bonuses, info, 3));
        ledger.add(new ModLedger.Entry(other, holder, weapon, true, Map.of(Stats.DAMAGE, Bonus.NONE), info, 0));
        check(ledger.bonuses(weapon).size() == 2); check(ledger.bonuses(weapon, "example:irregular").size() == 2);
        var groups = ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, null, e -> e.info().name());
        check(groups.size() == 2); check(groups.stream().mapToInt(ModInventory.Group::quantity).sum() == 2);
        check(groups.stream().anyMatch(g -> g.representative().id().equals(reserved) && g.available() == 0 && g.quantity() == 1));
        int before = changes[0];
        rejects(() -> ledger.release(holder, reserved)); rejects(() -> ledger.release(owner, other));
        rejects(() -> ledger.release(owner, free)); rejects(() -> ledger.release(owner, UUID.randomUUID()));
        check(changes[0] == before); check(ledger.entry(reserved).weapon().equals(weapon));
        ledger.release(owner, reserved); check(changes[0] == before + 1);
        var released = ledger.entry(reserved);
        check(released.id().equals(reserved) && released.owner().equals(owner));
        check(released.weapon() == null && released.slot() == -1 && released.shared());
        check(released.info().equals(info) && released.bonuses().equals(bonuses));
        check(ledger.bonuses(weapon).size() == 1 && ledger.bonuses(weapon, "example:irregular").size() == 1);
        check(ledger.installed(weapon).get(0).id().equals(other)); check(ledger.owned(owner).size() == 2);
        groups = ModInventory.browse(ledger.owned(owner), ModInventory.Query.DEFAULT, null, e -> e.info().name());
        check(groups.size() == 1 && groups.get(0).quantity() == 2 && groups.get(0).available() == 2);
        rejects(() -> ledger.release(owner, reserved));
        ledger.install(owner, reserved, weapon, ModInfo.Category.SWORD, 8, 4);
        ledger.uninstall(holder, reserved, weapon, true);
        ledger.install(holder, reserved, weapon, ModInfo.Category.SWORD, 8, 4);
        rejects(() -> ledger.release(owner, reserved));
        ledger.release(holder, reserved); check(ledger.entry(reserved).owner().equals(holder));
        System.out.println("Passed " + checks + " remote ownership, cache invalidation and reservation grouping checks");
    }
    private static void check(boolean value) { if (!value) throw new AssertionError("Remote release invariant"); checks++; }
    private static void rejects(Runnable action) {
        try { action.run(); } catch (IllegalArgumentException expected) { checks++; return; }
        throw new AssertionError("Expected rejected release");
    }
}
