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

public class SetFormaFunction extends LootItemConditionalFunction {

    public static final MapCodec<SetFormaFunction> CODEC = RecordCodecBuilder.mapCodec(inst ->
            commonFields(inst).and(
                    Codec.STRING.fieldOf("type").forGetter(f -> f.type)
            ).apply(inst, SetFormaFunction::new)
    );

    private final String type;

    public SetFormaFunction(List<LootItemCondition> conditions, String type) {
        super(conditions);
        this.type = type;
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext ctx) {
        stack.set(ComponentRegistry.FORMA_TYPE.get(), type);
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return LootRegistry.SET_FORMA_FUNCTION.get();
    }

    public static LootItemFunction.Builder builder(String type) {
        return new LootItemFunction.Builder() {
            @Override
            public LootItemFunction build() {
                return new SetFormaFunction(List.of(), type);
            }
        };
    }
}