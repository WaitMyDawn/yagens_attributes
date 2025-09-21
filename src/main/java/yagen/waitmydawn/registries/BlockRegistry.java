package yagen.waitmydawn.registries;


import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.block.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.entity.ModOperationTableEntity;

import java.util.Collection;


public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, YagensAttributes.MODID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, YagensAttributes.MODID);

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
    }

    public static final DeferredHolder<Block, Block> MOD_OPERATION_BLOCK = BLOCKS.register("mod_operation_table",
            () -> new ModOperationBlock(BlockBehaviour.Properties.of()));
    public static final DeferredHolder<Block, Block> MOD_RECYCLE_BLOCK = BLOCKS.register("mod_recycle_table",
            () -> new ModRecycleBlock(BlockBehaviour.Properties.of()));
    public static final DeferredHolder<Block, Block> MOD_ESSENCE_BLOCK = BLOCKS.register("mod_essence_block",
            () -> new Block(BlockBehaviour.Properties.of()
                    .strength(4f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ModOperationTableEntity>> MOD_OPERATION_TABLE_ENTITY =
            BLOCK_ENTITIES.register("mod_operation_table",
                    () -> BlockEntityType.Builder.of(ModOperationTableEntity::new,
                            MOD_OPERATION_BLOCK.get()).build(null));

    public static Collection<DeferredHolder<Block, ? extends Block>> blocks() {
        return BLOCKS.getEntries();
    }
}
