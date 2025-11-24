package yagen.waitmydawn.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.LootRegistry;

import java.util.List;

public class SetEndoFunction extends LootItemConditionalFunction {

    public static final MapCodec<SetEndoFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(
                    inst.group(
                            Codec.INT.fieldOf("level").forGetter(f -> f.level),
                            Codec.STRING.fieldOf("missionType").forGetter(f -> f.missionType)
                    )
            ).apply(inst, SetEndoFunction::new)
    );

    private final int level;
    private final String missionType;

    public SetEndoFunction(List<LootItemCondition> conditions, int level, String missionType) {
        super(conditions);
        this.level = level;
        this.missionType = missionType;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        stack.set(ComponentRegistry.ENDO_INFO.get(), new ComponentRegistry.EndoInfo(level, missionType));
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.SET_ENDO_FUNCTION.get();
    }

    public static LootItemFunction.Builder builder(int level, String missionType) {
        return new LootItemFunction.Builder() {
            @Override
            public LootItemFunction build() {
                return new SetEndoFunction(List.of(), level, missionType);
            }
        };
    }
}