package yagen.waitmydawn.yagens_attributes.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.*;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import yagen.waitmydawn.yagens_attributes.core.*;
import yagen.waitmydawn.yagens_attributes.data.*;
import yagen.waitmydawn.yagens_attributes.registries.Components;
import java.util.*;
import static net.minecraft.commands.Commands.*;

public final class ModCommands {
    private ModCommands() {}

    public static void register(com.mojang.brigadier.CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("ya")
            .then(literal("status").requires(source -> source.hasPermission(2))
                .then(argument("targets", EntityArgument.entities())
                .then(argument("type", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(DamageType.values()).map(DamageType::key), builder))
                .then(argument("base", DoubleArgumentType.doubleArg(0, 1000000000))
                .then(argument("seconds", DoubleArgumentType.doubleArg(.05, 3600))
                .then(argument("stacks", IntegerArgumentType.integer(1, 64)).executes(guard(ctx -> {
                    var type = DamageType.parse(StringArgumentType.getString(ctx, "type"));
                    double base = DoubleArgumentType.getDouble(ctx, "base"); int duration = (int) Math.ceil(DoubleArgumentType.getDouble(ctx, "seconds") * 20);
                    UUID owner = ctx.getSource().getEntity() == null ? new UUID(0, 0) : ctx.getSource().getEntity().getUUID();
                    int total = 0;
                    for (var target : EntityArgument.getEntities(ctx, "targets")) if (target instanceof net.minecraft.world.entity.LivingEntity living) {
                        boolean applied = false;
                        for (int i = 0; i < IntegerArgumentType.getInteger(ctx, "stacks"); i++)
                            applied |= yagen.waitmydawn.yagens_attributes.api.StatusApi.apply(living, type, owner, base, duration);
                        if (applied) total++;
                    }
                    tell(ctx, "Applied status to " + total + " living entities"); return total;
                }))))))))
            .then(literal("mods").executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                var state = WorldState.get(player.server);
                tell(ctx, "Owned Mods:");
                for (var mod : state.ledger.owned(player.getUUID())) tell(ctx, describe(mod));
                UUID weapon = player.getMainHandItem().get(Components.WEAPON_ID.get());
                if (weapon != null) {
                    tell(ctx, "Held weapon " + weapon + ":");
                    for (var mod : state.ledger.installed(weapon)) tell(ctx, describe(mod));
                }
                return 1;
            })))
            .then(literal("stats").executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                var profile = CombatData.WEAPONS.weapon(player.getMainHandItem());
                if (profile == null) throw new IllegalArgumentException("Held item has no weapon profile");
                var sources = bonuses(player);
                tell(ctx, "CC=" + Stats.resolve(profile.criticalChance(), Stats.CRITICAL_CHANCE, sources)
                        + " CD=" + Stats.resolve(profile.criticalDamage(), Stats.CRITICAL_DAMAGE, sources)
                        + " SC=" + Stats.resolve(profile.statusChance(), Stats.STATUS_CHANCE, sources)
                        + " duration=" + Stats.resolve(profile.statusDuration(), Stats.STATUS_DURATION, sources) + "s"
                        + " fire_rate=" + RangedStats.value(profile.fireRate(), Stats.FIRE_RATE, sources)
                        + " multishot=" + RangedStats.value(profile.multishot(), Stats.MULTISHOT, sources));
                tell(ctx, "Distribution=" + profile.distribution());
                tell(ctx, "Modifiers=" + sources);
                return 1;
            })))
            .then(literal("polarize").then(argument("slot", IntegerArgumentType.integer(1, 64))
                .then(argument("polarity", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(PolarityRules.FORMA_CHOICES.stream().map(ModInfo.Polarity::key), builder))
                .executes(guard(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    var held = player.getMainHandItem();
                    if (!WeaponEligibility.operable(held, CombatData.WEAPONS.weapon(held))) throw new IllegalArgumentException("gui.yagens_attributes.polarize_invalid");
                    var weapon = Components.identify(held);
                    var polarity = ModInfo.Polarity.valueOf(StringArgumentType.getString(ctx, "polarity").toUpperCase(Locale.ROOT));
                    yagen.waitmydawn.yagens_attributes.setup.ModTransactions.polarize(player, WorldState.get(player.server), weapon, IntegerArgumentType.getInteger(ctx, "slot") - 1, polarity);
                    ctx.getSource().sendSuccess(() -> Component.translatable("gui.yagens_attributes." + (player.isCreative() ? "polarize_success_creative" : "polarize_success")), false); return 1;
                })))))
            .then(literal("install").then(argument("mod", StringArgumentType.word()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                if (CombatData.WEAPONS.weapon(player.getMainHandItem()) == null) throw new IllegalArgumentException("Held item has no weapon profile");
                WorldState state = WorldState.get(player.server);
                UUID weapon = Components.identify(player.getMainHandItem());
                var profile = WeaponPolarityData.profile(player.getMainHandItem(), CombatData.WEAPONS.weapon(player.getMainHandItem()), state.ledger);
                var entry = state.ledger.entry(id(ctx));
                if (entry.info().defined()) ModDefinitions.INSTANCE.catalog().card(entry.info(), entry.info().level());
                state.ledger.install(player.getUUID(), id(ctx), weapon, profile.category(), profile.slots().size(), -1, Components.capacity(player.getMainHandItem(), state.progress(weapon)), player.isCreative(), profile.slots(), net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString());
                state.setDirty(); tell(ctx, "Mod installed"); return 1;
            }))))
            .then(literal("release").then(argument("mod", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    var player = ctx.getSource().getPlayerOrException();
                    return SharedSuggestionProvider.suggest(WorldState.get(player.server).ledger.owned(player.getUUID()).stream().filter(mod -> mod.weapon() != null).map(mod -> mod.id().toString()), builder);
                }).executes(guard(ctx -> {
                    var player = ctx.getSource().getPlayerOrException();
                    WorldState.get(player.server).ledger.release(player.getUUID(), id(ctx));
                    ctx.getSource().sendSuccess(() -> Component.translatable("gui.yagens_attributes.release_success"), false); return 1;
                }))))
            .then(literal("uninstall").then(argument("mod", StringArgumentType.word()).executes(guard(ctx -> remove(ctx, false)))))
            .then(literal("take").then(argument("mod", StringArgumentType.word()).executes(guard(ctx -> remove(ctx, true)))))
            .then(literal("upgrade").then(argument("mod", StringArgumentType.word()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                yagen.waitmydawn.yagens_attributes.setup.ModTransactions.upgrade(player, WorldState.get(player.server), id(ctx), player.getMainHandItem().get(Components.WEAPON_ID.get()));
                tell(ctx, "Mod upgraded"); return 1;
            }))))
            .then(literal("withdrawmod").then(argument("mod", StringArgumentType.word()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                yagen.waitmydawn.yagens_attributes.setup.ModTransactions.withdraw(player, WorldState.get(player.server), id(ctx));
                tell(ctx, "Mod withdrawn to inventory"); return 1;
            }))))
            .then(literal("share").then(argument("mod", StringArgumentType.word()).then(argument("allow", BoolArgumentType.bool()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                WorldState state = WorldState.get(player.server);
                state.ledger.share(player.getUUID(), id(ctx), BoolArgumentType.getBool(ctx, "allow"));
                state.setDirty(); tell(ctx, "Mod permission updated"); return 1;
            })))))
            .then(literal("shareweapon").then(argument("allow", BoolArgumentType.bool()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                WorldState state = WorldState.get(player.server);
                UUID weapon = player.getMainHandItem().get(Components.WEAPON_ID.get());
                if (weapon == null) throw new IllegalArgumentException("Held weapon has no installed Mods");
                for (var mod : state.ledger.installed(weapon)) if (mod.owner().equals(player.getUUID()))
                    state.ledger.share(player.getUUID(), mod.id(), BoolArgumentType.getBool(ctx, "allow"));
                state.setDirty(); tell(ctx, "Permissions updated for owned Mods on held weapon"); return 1;
            }))))
            .then(literal("visibility").then(argument("seeOthers", BoolArgumentType.bool()).then(argument("shareOwn", BoolArgumentType.bool()).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                WorldState.get(player.server).visibility(player.getUUID(), new WorldState.Visibility(BoolArgumentType.getBool(ctx, "seeOthers"), BoolArgumentType.getBool(ctx, "shareOwn")));
                tell(ctx, "Damage visibility updated"); return 1;
            })))))
            .then(literal("grant").requires(source -> source.hasPermission(2))
                .then(argument("player", EntityArgument.player()).then(statArgument().then(operationArgument().then(
                    argument("value", DoubleArgumentType.doubleArg(-1000000, 1000000)).executes(guard(ctx -> {
                        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                        WorldState state = WorldState.get(player.server);
                        UUID id = UUID.randomUUID();
                        state.ledger.add(new ModLedger.Entry(id, player.getUUID(), null, false, Map.of(stat(ctx), bonus(ctx)), ModDefinitions.INSTANCE.catalog().legacyInfo(ModInfo.DEFAULT), -1));
                        state.setDirty(); tell(ctx, "Granted Mod " + id); return 1;
                    })))))))
            .then(literal("playerbonus").requires(source -> source.hasPermission(2))
                .then(argument("player", EntityArgument.player()).then(statArgument().then(operationArgument().then(
                    argument("value", DoubleArgumentType.doubleArg(-1000000, 1000000)).executes(guard(ctx -> {
                        ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                        WorldState state = WorldState.get(player.server);
                        String stat = stat(ctx);
                        Bonus old = state.bonuses(player.getUUID()).getOrDefault(stat, Bonus.NONE);
                        Bonus value = bonus(ctx);
                        String operation = StringArgumentType.getString(ctx, "operation");
                        var factors = new ArrayList<>(old.multiply());
                        factors.addAll(value.multiply());
                        state.bonus(player.getUUID(), stat, new Bonus(operation.equals("addin") ? value.addin() : old.addin(),
                                operation.equals("addition") ? value.addition() : old.addition(), factors,
                                operation.equals("addout") ? value.addout() : old.addout(), old.inputAddin(), old.inputAddition()));
                        tell(ctx, "Player stat bonus updated"); return 1;
                    })))))))
            .then(literal("clearbonus").requires(source -> source.hasPermission(2))
                .then(argument("player", EntityArgument.player()).then(statArgument().executes(guard(ctx -> {
                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                    WorldState.get(player.server).bonus(player.getUUID(), stat(ctx), Bonus.NONE);
                    tell(ctx, "Player stat bonus cleared"); return 1;
                })))))
            .then(literal("modmeta").requires(source -> source.hasPermission(2)).then(argument("mod", StringArgumentType.word())
                .then(argument("category", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(ModInfo.Category.values()).filter(value -> value != ModInfo.Category.ALL).map(ModInfo.Category::key), builder))
                .then(argument("rarity", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(ModInfo.Rarity.values()).map(ModInfo.Rarity::key), builder))
                .then(argument("polarity", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Arrays.stream(ModInfo.Polarity.values()).map(ModInfo.Polarity::key), builder))
                .then(argument("name", StringArgumentType.greedyString()).executes(guard(ctx -> {
                    var info = new ModInfo(StringArgumentType.getString(ctx, "name"), ModInfo.Category.valueOf(StringArgumentType.getString(ctx, "category").toUpperCase(Locale.ROOT)),
                            ModInfo.Rarity.valueOf(StringArgumentType.getString(ctx, "rarity").toUpperCase(Locale.ROOT)), ModInfo.Polarity.valueOf(StringArgumentType.getString(ctx, "polarity").toUpperCase(Locale.ROOT)),
                            WorldState.get(ctx.getSource().getServer()).ledger.entry(id(ctx)).info().capacityCost());
                    WorldState.get(ctx.getSource().getServer()).ledger.metadata(id(ctx), ModDefinitions.INSTANCE.catalog().legacyInfo(info));
                    tell(ctx, "Mod metadata updated"); return 1;
                }))))))))
            .then(literal("modcost").requires(source -> source.hasPermission(2)).then(argument("mod", StringArgumentType.word()).then(argument("cost", IntegerArgumentType.integer(0, 1_000_000)).executes(guard(ctx -> {
                var state = WorldState.get(ctx.getSource().getServer()); var old = state.ledger.entry(id(ctx)).info();
                state.ledger.metadata(id(ctx), ModDefinitions.INSTANCE.catalog().legacyInfo(new ModInfo(old.name(), old.category(), old.rarity(), old.polarity(), IntegerArgumentType.getInteger(ctx, "cost"))));
                tell(ctx, "Mod capacity cost updated"); return 1;
            })))))
            .then(literal("weaponxp").requires(source -> source.hasPermission(2)).then(argument("amount", IntegerArgumentType.integer(0)).executes(guard(ctx -> {
                ServerPlayer player = ctx.getSource().getPlayerOrException();
                if (CombatData.WEAPONS.weapon(player.getMainHandItem()) == null) throw new IllegalArgumentException("Held item has no weapon profile");
                var state = WorldState.get(player.server); UUID id = Components.identify(player.getMainHandItem());
                var progress = state.progress(id).gain(IntegerArgumentType.getInteger(ctx, "amount")).progress(); state.progress(id, progress);
                net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player, new yagen.waitmydawn.yagens_attributes.network.ProgressPayload(id, progress));
                tell(ctx, "Weapon level=" + progress.level() + " experience=" + progress.experience()); return 1;
            }))))
        );
    }

    public static List<Map<String, Bonus>> bonuses(ServerPlayer player) {
        List<Map<String, Bonus>> sources = new ArrayList<>();
        WorldState state = WorldState.get(player.server);
        UUID weapon = player.getMainHandItem().get(Components.WEAPON_ID.get());
        var profile = CombatData.WEAPONS.weapon(player.getMainHandItem());
        if (weapon != null && profile != null) sources.addAll(state.ledger.bonuses(weapon, net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString(), profile.category()));
        sources.add(state.bonuses(player.getUUID()));
        return sources;
    }

    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> statArgument() {
        return argument("stat", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(Stats.KEYS, builder));
    }
    private static com.mojang.brigadier.builder.RequiredArgumentBuilder<CommandSourceStack, String> operationArgument() {
        return argument("operation", StringArgumentType.word()).suggests((ctx, builder) -> SharedSuggestionProvider.suggest(List.of("addin", "addition", "multiply", "addout"), builder));
    }
    private static String stat(CommandContext<CommandSourceStack> ctx) {
        String stat = StringArgumentType.getString(ctx, "stat");
        if (!Stats.KEYS.contains(stat)) throw new IllegalArgumentException("Unknown stat");
        return stat;
    }
    private static Bonus bonus(CommandContext<CommandSourceStack> ctx) {
        double value = DoubleArgumentType.getDouble(ctx, "value");
        return switch (StringArgumentType.getString(ctx, "operation")) {
            case "addin" -> new Bonus(value, 0, List.of(), 0);
            case "addition" -> new Bonus(0, value, List.of(), 0);
            case "multiply" -> new Bonus(0, 0, List.of(value), 0);
            case "addout" -> new Bonus(0, 0, List.of(), value);
            default -> throw new IllegalArgumentException("Unknown operation");
        };
    }
    private static UUID id(CommandContext<CommandSourceStack> ctx) { return UUID.fromString(StringArgumentType.getString(ctx, "mod")); }
    private static int remove(CommandContext<CommandSourceStack> ctx, boolean take) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        WorldState state = WorldState.get(player.server);
        state.ledger.uninstall(player.getUUID(), id(ctx), player.getMainHandItem().get(Components.WEAPON_ID.get()), take);
        state.setDirty(); tell(ctx, take ? "Mod transferred to independent storage" : "Mod uninstalled"); return 1;
    }
    private static String describe(ModLedger.Entry mod) {
        return mod.id() + " owner=" + mod.owner() + " weapon=" + mod.weapon() + " shared=" + mod.shared() + " " + mod.bonuses();
    }
    private static void tell(CommandContext<CommandSourceStack> ctx, String text) { ctx.getSource().sendSuccess(() -> Component.literal(text), false); }
    private static Command<CommandSourceStack> guard(Command<CommandSourceStack> command) {
        return ctx -> {
            try { return command.run(ctx); }
            catch (IllegalArgumentException exception) { ctx.getSource().sendFailure(Component.translatable(exception.getMessage())); return 0; }
        };
    }
}
