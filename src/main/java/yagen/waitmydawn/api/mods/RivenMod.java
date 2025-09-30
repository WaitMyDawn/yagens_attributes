package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.capabilities.ModContainer;
import yagen.waitmydawn.registries.ComponentRegistry;
import yagen.waitmydawn.registries.ItemRegistry;

import java.util.ArrayList;
import java.util.List;

public final class RivenMod extends AbstractMod {

    public static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(YagensAttributes.MODID, "riven_tool_mod");

    public RivenMod() {
        super(8, "Riven", ModRarity.RIVEN);
        this.baseCapacityCost = 10;
    }

    @Override
    public ResourceLocation getModResource() {
        return ID;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int modLevel, LivingEntity player) {
        return List.of();
    }

    public static List<MutableComponent> getRivenUniqueInfo(ItemStack stack, int modLevel) {
        if (!IModContainer.isModContainer(stack)) {
            return List.of();
        }
        IModContainer container = IModContainer.get(stack);
        for (ModSlot slot : container.getActiveMods()) {
            if (slot.getMod() instanceof RivenMod) {
                ComponentRegistry.RivenRawInfoList raw =
                        stack.get(ComponentRegistry.RIVEN_RAW_INFO.get());
                if (raw != null) {
                    return raw.raw().stream()
                            .map(r -> Component.translatable(r.key(), r.base() * modLevel))
                            .toList();
                }
                break;
            }
        }
        return List.of();
    }
}