package yagen.waitmydawn.yagens_attributes.registries;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.registries.*;
import yagen.waitmydawn.yagens_attributes.YagensAttributes;
import yagen.waitmydawn.yagens_attributes.network.ClientPackets;
import yagen.waitmydawn.yagens_attributes.setup.ModTransactions;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> REGISTER = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YagensAttributes.MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MODS = REGISTER.register("mods", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.yagens_attributes.mods")).icon(() -> new ItemStack(ModItems.MOD.get()))
            .displayItems((parameters, output) -> {
                for (var card : ClientPackets.modCatalog().creative())
                    output.accept(ModTransactions.stack(card.id(), card.level()));
                output.accept(ModItems.UNKNOWN_MELEE_RIVEN); output.accept(ModItems.UNKNOWN_RANGED_RIVEN);
            }).build());
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> ITEMS = REGISTER.register("items", () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.yagens_attributes.items")).icon(() -> new ItemStack(ModItems.FORMA.get()))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.ENDO); output.accept(ModItems.FORMA); output.accept(ModItems.KUVA); output.accept(ModItems.OROKIN_CATALYST);
                output.accept(ModItems.GALLIUM_ORE);
                output.accept(ModItems.DEEPSLATE_GALLIUM_ORE);
                output.accept(ModItems.RAW_GALLIUM);
                output.accept(ModItems.GALLIUM);
            }).build());

    private ModCreativeTabs() {
    }
}
