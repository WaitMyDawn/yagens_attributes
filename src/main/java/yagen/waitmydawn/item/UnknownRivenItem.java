package yagen.waitmydawn.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import yagen.waitmydawn.api.item.FormaType;
import yagen.waitmydawn.api.mods.*;
import yagen.waitmydawn.capabilities.ModContainer;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UnknownRivenItem extends Item {

    public UnknownRivenItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext ctx,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yagens_attributes.unknown_riven.tooltip1")
                .withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player,
                                                  InteractionHand hand) {
        if (!level.isClientSide) {
            int bonus, penalty;
            switch (RandomSource.create().nextInt(4)) {
                case 0 -> {
                    bonus = 2;
                    penalty = 0;
                }
                case 1 -> {
                    bonus = 2;
                    penalty = 1;
                }
                case 2 -> {
                    bonus = 3;
                    penalty = 0;
                }
                default -> {
                    bonus = 3;
                    penalty = 1;
                }
            }
            ItemStack modStack = createRandomModItem(bonus, penalty, 1, "random", null);
            player.getItemInHand(hand).shrink(1);
            if (!player.addItem(modStack)) {
                player.drop(modStack, false);
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand),
                level.isClientSide);
    }

    public static ItemStack createRandomModItem(int bonus, int penalty, int level, String polarity, Item targetWeapon) {
        RandomSource rnd = RandomSource.create();
        if (polarity.equals("random")) {
            FormaType[] all = FormaType.values();
            polarity = all[rnd.nextInt(all.length)].getValue();
        }
        if (targetWeapon == null) {
            targetWeapon = RivenModPool.randomWeapon(rnd);
        }

        List<RivenUniqueInfo> posPool;
        List<RivenUniqueInfo> negPool;
        List<RivenUniqueInfo> lines = new ArrayList<>();
        if (targetWeapon instanceof ProjectileWeaponItem) {
            posPool = RivenUniqueInfo.PROJECTILE_POSITIVE;
            negPool = RivenUniqueInfo.PROJECTILE_NEGATIVE;
        } else {
            posPool = RivenUniqueInfo.MELEE_POSITIVE;
            negPool = RivenUniqueInfo.MELEE_NEGATIVE;
        }

        List<RivenUniqueInfo> chosenPositives = RivenUniqueInfo.draw(posPool, bonus, rnd);

        java.util.Set<String> usedAttributes = chosenPositives.stream()
                .map(info -> getAttributeKey(info.key()))
                .collect(Collectors.toSet());

        List<RivenUniqueInfo> validNegPool = negPool.stream()
                .filter(info -> {
                    String attrKey = getAttributeKey(info.key());
                    return !usedAttributes.contains(attrKey);
                })
                .toList();

        List<RivenUniqueInfo> chosenNegatives = RivenUniqueInfo.draw(validNegPool, penalty, rnd);

        lines.addAll(chosenPositives);
        lines.addAll(chosenNegatives);

        List<RivenUniqueInfo> scaledLines = scaleLines(lines, bonus, penalty, targetWeapon, rnd);

        ItemStack stack = new ItemStack(ItemRegistry.MOD.get());

        IModContainerMutable mutable = new ModContainer(1, false, true).mutableCopy();
        mutable.addMod(new RivenMod(), level);
        IModContainer container = mutable.toImmutable();
        stack.set(ComponentRegistry.MOD_CONTAINER.get(), container);


        List<ComponentRegistry.RivenRawInfoList.RivenRawInfo> raw = scaledLines.stream()
                .map(l -> new ComponentRegistry.RivenRawInfoList.RivenRawInfo(l.key(), BigDecimal.valueOf(l.baseValue())
                        .setScale(2, RoundingMode.HALF_UP)
                        .doubleValue()))
                .toList();
        stack.set(ComponentRegistry.RIVEN_RAW_INFO.get(),
                new ComponentRegistry.RivenRawInfoList(raw));
        stack.set(ComponentRegistry.RIVEN_POLARITY_TYPE.get(), polarity);
        stack.set(ComponentRegistry.RIVEN_TYPE.get(), targetWeapon);

        return stack;
    }

    record Coeff(double pos, double neg) {
    }

    private static final Map<String, Coeff> COEFF_MAP = Map.of(
            "2p0n", new Coeff(0.99f, 0f),
            "2p1n", new Coeff(1.2375f, 0.495f),
            "3p0n", new Coeff(0.75f, 0f),
            "3p1n", new Coeff(0.9375f, 0.75f)
    );

    private static String getAttributeKey(String key) {
        if (key == null || key.isEmpty()) return "null";
        int lastDotIndex = key.lastIndexOf('.');
        String keyName = (lastDotIndex != -1) ? key.substring(lastDotIndex + 1) : key;
        int lastUnderscoreIndex = keyName.lastIndexOf('_');
        if (lastUnderscoreIndex != -1) {
            return keyName.substring(0, lastUnderscoreIndex);
        }
        return keyName;
    }

    private static List<RivenUniqueInfo> scaleLines(List<RivenUniqueInfo> lines,
                                                    int posCnt,
                                                    int negCnt,
                                                    Item weapon,
                                                    RandomSource rnd) {

        Coeff coeff = COEFF_MAP.getOrDefault(posCnt + "p" + negCnt + "n",
                new Coeff(1f, 1f));
        double posBase = coeff.pos();
        double negBase = coeff.neg();

        double disp = RivenModPool.getDisposition(weapon);

        return IntStream.range(0, lines.size())
                .mapToObj(i -> {
                    RivenUniqueInfo old = lines.get(i);
                    boolean isPos = i < posCnt;
                    double base = isPos ? posBase : negBase;
                    double newVal = old.baseValue() * base * (0.9f + rnd.nextFloat() * 0.2f) * disp;
                    return new RivenUniqueInfo(old.key(), old.weight(), newVal);
                })
                .collect(Collectors.toList());
    }
}