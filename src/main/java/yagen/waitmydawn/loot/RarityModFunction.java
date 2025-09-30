package yagen.waitmydawn.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.mods.ModRarity;
import yagen.waitmydawn.item.Mod;
import yagen.waitmydawn.registries.LootRegistry;

import java.util.List;

import static yagen.waitmydawn.api.mods.ModRarity.fromInteger;
import static yagen.waitmydawn.api.util.ModCompat.TRANSFORM_POOL_BY_RARITY;

public class RarityModFunction extends LootItemConditionalFunction {

    public static final MapCodec<RarityModFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
                    commonFields(inst).and(
                            inst.group(
                                    Codec.INT.optionalFieldOf("rarity", ModRarity.COMMON.getValue()).forGetter(f -> f.rarity),
                                    Codec.INT.optionalFieldOf("level_percent_inf", 0).forGetter(f -> f.levelPercentInf),
                                    Codec.INT.optionalFieldOf("level_percent_sup", 50).forGetter(f -> f.levelPercentSup)
//                            Codec.INT.optionalFieldOf("riven_weight", 0).forGetter(f -> f.rivenWeight)
                            )
                    ).apply(inst, RarityModFunction::new)
    );

    private final int rarity, levelPercentInf, levelPercentSup;

    public RarityModFunction(List<LootItemCondition> conditions,
                             int rarity, int levelPercentInf, int levelPercentSup) {
        super(conditions);
        this.rarity = rarity;
        this.levelPercentInf = Math.min(levelPercentInf, 100);
        this.levelPercentSup = Math.max(Math.min(levelPercentSup, 100), levelPercentInf);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        RandomSource rand = ctx.getRandom();

        List<AbstractMod> pool = TRANSFORM_POOL_BY_RARITY.get(fromInteger(rarity));
        if (pool == null || pool.isEmpty()) return stack;

        AbstractMod mod = pool.get(rand.nextInt(pool.size()));
        // Sup - Inf can't be 0
        int fix = 0;
        if (levelPercentInf == levelPercentSup) fix = 1;
        int modLevel = Math.max(1, (int) Math.round(mod.getMaxLevel() * (levelPercentInf + rand.nextInt(levelPercentSup - levelPercentInf + fix)) / 100.0));

        if (modLevel > mod.getMaxLevel()) modLevel = mod.getMaxLevel();

        IModContainer.createModContainer(mod, modLevel, stack);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.RARITY_MOD_FUNCTION.get();
    }

    public static LootItemFunction.Builder builder(int rarity, int levelPercentInf, int levelPercentSup) {
        return new LootItemFunction.Builder() {
            @Override
            public LootItemFunction build() {
                return new RarityModFunction(List.of(), rarity, levelPercentInf, levelPercentSup);
            }
        };
    }
}