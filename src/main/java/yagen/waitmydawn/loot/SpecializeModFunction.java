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
import yagen.waitmydawn.api.mods.AbstractMod;
import yagen.waitmydawn.api.mods.IModContainer;
import yagen.waitmydawn.api.registry.ModRegistry;
import yagen.waitmydawn.registries.LootRegistry;

import java.util.List;

public class SpecializeModFunction extends LootItemConditionalFunction {

    public static final MapCodec<SpecializeModFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
                    commonFields(inst).and(
                            inst.group(
                                    Codec.STRING.optionalFieldOf("mod_id", "").forGetter(f -> f.modId),
                                    Codec.INT.optionalFieldOf("level_percent_inf", 0).forGetter(f -> f.levelPercentInf),
                                    Codec.INT.optionalFieldOf("level_percent_sup", 50).forGetter(f -> f.levelPercentSup)
//                            Codec.INT.optionalFieldOf("riven_weight", 0).forGetter(f -> f.rivenWeight)
                            )
                    ).apply(inst, SpecializeModFunction::new)
    );

    private final String modId;
    private final int levelPercentInf, levelPercentSup;

    public SpecializeModFunction(List<LootItemCondition> conditions,
                                 String modId, int levelPercentInf, int levelPercentSup) {
        super(conditions);
        this.modId = modId;
        this.levelPercentInf = levelPercentInf;
        this.levelPercentSup = levelPercentSup;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        RandomSource rand = ctx.getRandom();

        AbstractMod mod = ModRegistry.getMod(modId);
        // Sup - Inf can't be 0
        int modLevel = Math.max(1, (int) Math.round(mod.getMaxLevel() * (levelPercentInf + rand.nextInt(levelPercentSup - levelPercentInf)) / 100.0));

        IModContainer.createModContainer(mod, modLevel, stack);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.SPECIALIZE_MOD_FUNCTION.get();
    }

    public static LootItemFunction.Builder builder(String modId, int levelPercentInf, int levelPercentSup) {
        return new LootItemFunction.Builder() {
            @Override
            public LootItemFunction build() {
                return new SpecializeModFunction(List.of(), modId, levelPercentInf, levelPercentSup);
            }
        };
    }
}