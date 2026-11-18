package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;

public final class ModBlocks {
    public static final DeferredRegister.Blocks REGISTER = DeferredRegister.createBlocks(YagensAttributes.MODID);
    public static final DeferredBlock<Block> GALLIUM_ORE = REGISTER.registerSimpleBlock("gallium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.STONE).instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(3.0F, 3.0F).requiresCorrectToolForDrops());
    public static final DeferredBlock<Block> DEEPSLATE_GALLIUM_ORE = REGISTER.registerSimpleBlock("deepslate_gallium_ore",
            BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM)
                    .strength(4.5F, 3.0F).sound(SoundType.DEEPSLATE).requiresCorrectToolForDrops());

    private ModBlocks() {}
}
