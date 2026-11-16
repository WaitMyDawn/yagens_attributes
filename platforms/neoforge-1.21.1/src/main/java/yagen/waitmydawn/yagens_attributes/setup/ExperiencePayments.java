package yagen.waitmydawn.yagens_attributes.setup;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import yagen.waitmydawn.yagens_attributes.core.ExperiencePoints;

public final class ExperiencePayments {
    private ExperiencePayments() {}
    public static long balance(Player player) { return ExperiencePoints.balance(player.experienceLevel, player.experienceProgress); }
    public static boolean affordable(Player player, long points) {
        return player.hasInfiniteMaterials() || ExperiencePoints.affordable(balance(player), points);
    }
    public static void pay(Player player, long points) {
        if (player.hasInfiniteMaterials() || points == 0) return;
        long available = balance(player);
        if (!ExperiencePoints.affordable(available, points)) throw new IllegalStateException("Insufficient experience points");
        long remaining = available - points;
        var state = ExperiencePoints.state(remaining);
        // Purchase debits bypass award hooks so earned-XP routing cannot refund or redirect payment.
        if (player instanceof ServerPlayer serverPlayer) serverPlayer.setExperienceLevels(state.level());
        else player.experienceLevel = state.level();
        player.experienceProgress = state.progress();
        player.totalExperience = (int) Math.min(Integer.MAX_VALUE, remaining);
    }
}
