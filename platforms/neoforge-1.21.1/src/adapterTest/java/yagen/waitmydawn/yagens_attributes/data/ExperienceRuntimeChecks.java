package yagen.waitmydawn.yagens_attributes.data;

import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;
import yagen.waitmydawn.yagens_attributes.registries.ModItems;
import yagen.waitmydawn.yagens_attributes.setup.ExperiencePayments;
import yagen.waitmydawn.yagens_attributes.setup.KuvaInteractions;
import java.util.UUID;

final class ExperienceRuntimeChecks {
    private static int checks;
    private ExperienceRuntimeChecks() {}
    static int run(ServerLevel level, JsonObject report) {
        var player = FakePlayerFactory.get(level, new GameProfile(UUID.fromString("9ecfce52-4244-4b43-bbe0-b5c4d16c9f71"), "[ExperienceChecks]"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.getInventory().clearContent(); player.getInventory().selected = 0;
        player.setPos(32, 100, 32);
        anvil(player); enchanting(level, player); kuva(level, player);
        report.addProperty("experience_kuva_checks", checks);
        return checks;
    }
    private static void setPoints(ServerPlayer player, long points) {
        var state = ExperiencePoints.state(points);
        player.setExperienceLevels(state.level()); player.experienceProgress = state.progress();
        player.totalExperience = 0;
    }
    private static AnvilMenu repair(ServerPlayer player, int priorWork) {
        var menu = new AnvilMenu(20, player.getInventory());
        var sword = new ItemStack(Items.DIAMOND_SWORD); sword.setDamageValue(100); sword.set(DataComponents.REPAIR_COST, priorWork);
        menu.getSlot(0).set(sword); menu.getSlot(1).set(new ItemStack(Items.DIAMOND));
        return menu;
    }
    private static void take(ServerPlayer player, AnvilMenu menu) {
        var result = menu.getSlot(2).remove(1); menu.getSlot(2).onTake(player, result);
    }
    private static void anvil(ServerPlayer player) {
        long start = ExperiencePoints.total(50) + 13; setPoints(player, start);
        var menu = repair(player, 6);
        check(menu.getCost() == 7 && menu.getSlot(2).hasItem());
        check(menu.getSlot(2).mayPickup(player));
        take(player, menu);
        check(ExperiencePayments.balance(player) == start - 91 && player.totalExperience == start - 91);
        check(menu.getSlot(0).getItem().isEmpty() && menu.getSlot(1).getItem().isEmpty());

        setPoints(player, ExperiencePoints.total(100)); menu = repair(player, 63);
        check(menu.getCost() == 64 && menu.getSlot(2).hasItem());
        check(menu.getSlot(2).mayPickup(player)); start = ExperiencePayments.balance(player);
        take(player, menu); check(ExperiencePayments.balance(player) == start - ExperiencePoints.total(64));

        setPoints(player, 90); menu = repair(player, 6); check(!menu.getSlot(2).mayPickup(player));
        setPoints(player, 91); check(menu.getSlot(2).mayPickup(player)); take(player, menu);
        check(player.experienceLevel == 0 && player.experienceProgress == 0 && player.totalExperience == 0);

        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); setPoints(player, 10); menu = repair(player, 63);
        check(menu.getSlot(2).mayPickup(player)); take(player, menu); check(ExperiencePayments.balance(player) == 10);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        menu = repair(player, 0); menu.getSlot(1).set(new ItemStack(Items.STICK));
        check(!menu.getSlot(2).hasItem());
    }
    private static void enchanting(ServerLevel level, ServerPlayer player) {
        for (int[] offer : new int[][] {{30, 2, 306}, {24, 1, 149}, {9, 0, 23}}) {
            long start = ExperiencePoints.total(50) + 11; setPoints(player, start);
            var menu = new EnchantmentMenu(21, player.getInventory(), ContainerLevelAccess.create(level, new BlockPos(32, 100, 32)));
            menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD)); menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 8));
            menu.costs[offer[1]] = offer[0];
            check(menu.clickMenuButton(player, offer[1]));
            check(menu.getSlot(0).getItem().isEnchanted());
            check(ExperiencePayments.balance(player) == start - offer[2]);
            check(menu.getSlot(1).getItem().getCount() == 8 - offer[1] - 1);
        }
        var menu = new EnchantmentMenu(22, player.getInventory(), ContainerLevelAccess.create(level, new BlockPos(32, 100, 32)));
        menu.getSlot(0).set(new ItemStack(Items.DIAMOND_SWORD)); menu.getSlot(1).set(new ItemStack(Items.LAPIS_LAZULI, 8));
        menu.costs[2] = 30; setPoints(player, ExperiencePoints.total(29));
        check(!menu.clickMenuButton(player, 2)); check(ExperiencePayments.balance(player) == ExperiencePoints.total(29));
        check(!menu.getSlot(0).getItem().isEnchanted() && menu.getSlot(1).getItem().getCount() == 8);
        player.gameMode.changeGameModeForPlayer(GameType.CREATIVE); setPoints(player, 11);
        check(menu.clickMenuButton(player, 2)); check(ExperiencePayments.balance(player) == 11);
        check(menu.getSlot(1).getItem().getCount() == 8);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
    }
    private static void kuva(ServerLevel level, ServerPlayer player) {
        var sword = new ItemStack(Items.DIAMOND_SWORD); sword.set(DataComponents.REPAIR_COST, 31); sword.setDamageValue(123);
        sword.set(DataComponents.CUSTOM_NAME, Component.literal("Retained metadata"));
        var expected = sword.copy(); expected.set(DataComponents.REPAIR_COST, 0);
        player.setItemInHand(InteractionHand.MAIN_HAND, sword);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.KUVA.get(), 8));
        check(player.gameMode.useItem(player, level, sword, InteractionHand.MAIN_HAND).consumesAction());
        check(ItemStack.isSameItemSameComponents(expected, player.getMainHandItem()));
        check(player.getOffhandItem().getCount() == 7);
        check(!KuvaInteractions.clean(player, InteractionHand.MAIN_HAND)); check(player.getOffhandItem().getCount() == 7);
        sword.set(DataComponents.REPAIR_COST, 15);
        check(!KuvaInteractions.clean(player, InteractionHand.OFF_HAND)); check(sword.get(DataComponents.REPAIR_COST) == 15);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.FORMA.get()));
        check(!KuvaInteractions.clean(player, InteractionHand.MAIN_HAND)); check(sword.get(DataComponents.REPAIR_COST) == 15);
        player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(ModItems.KUVA.get(), 7));
        var block = new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, player.blockPosition(), new net.minecraft.world.phys.BlockHitResult(player.position(), net.minecraft.core.Direction.UP, player.blockPosition(), false));
        NeoForge.EVENT_BUS.post(block); check(block.isCanceled() && block.getCancellationResult() == InteractionResult.CONSUME);
        check(sword.get(DataComponents.REPAIR_COST) == 0 && player.getOffhandItem().getCount() == 6);
        var target = net.minecraft.world.entity.EntityType.COW.create(level);
        sword.set(DataComponents.REPAIR_COST, 3);
        var specific = new PlayerInteractEvent.EntityInteractSpecific(player, InteractionHand.MAIN_HAND, target, net.minecraft.world.phys.Vec3.ZERO);
        NeoForge.EVENT_BUS.post(specific); check(specific.isCanceled()); check(player.getOffhandItem().getCount() == 5);
        var general = new PlayerInteractEvent.EntityInteract(player, InteractionHand.MAIN_HAND, target);
        NeoForge.EVENT_BUS.post(general); check(!general.isCanceled()); check(player.getOffhandItem().getCount() == 5);
        sword.set(DataComponents.REPAIR_COST, 7); player.gameMode.changeGameModeForPlayer(GameType.CREATIVE);
        check(KuvaInteractions.clean(player, InteractionHand.MAIN_HAND)); check(player.getOffhandItem().getCount() == 5);
        sword.set(DataComponents.REPAIR_COST, 7); player.gameMode.changeGameModeForPlayer(GameType.SPECTATOR);
        check(!KuvaInteractions.clean(player, InteractionHand.MAIN_HAND)); check(sword.get(DataComponents.REPAIR_COST) == 7);
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        var book = new ItemStack(Items.BOOK); book.set(DataComponents.REPAIR_COST, 3); player.setItemInHand(InteractionHand.MAIN_HAND, book);
        check(KuvaInteractions.clean(player, InteractionHand.MAIN_HAND)); check(book.get(DataComponents.REPAIR_COST) == 0);
        check(player.getOffhandItem().getCount() == 4);
    }
    private static void check(boolean value) { checks++; if (!value) throw new AssertionError("XP/Kuva runtime check " + checks); }
}
