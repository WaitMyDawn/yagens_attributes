package yagen.waitmydawn.api.mods;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.gui.mod_operation.ModOperationMenu;
import yagen.waitmydawn.registries.ComponentRegistry;

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
    public List<MutableComponent> getUniqueInfo(int modLevel) {
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
                            .map(r -> {
                                String modId = ModOperationMenu.getModIdFromKey(r.key());
                                if (modId == null || modId.equals(YagensAttributes.MODID))
                                    return Component.translatable(r.key(), String.format("%.2f", r.base() * modLevel));
                                else {
                                    String operation = ModOperationMenu.getOperationFromKey(r.key());
                                    String attributeString = ModOperationMenu.getModAttributeFromKey(r.key());
                                    Attribute attribute = ModOperationMenu.getModAttribute(modId, attributeString);
                                    if (attribute == null)
                                        return Component.translatable(r.key(), String.format("%.2f", r.base() * modLevel));
                                    return Component.translatable(
                                            "tooltips.yagens_attributes.general._" + operation,
                                            String.format("%.2f", r.base() * modLevel),
                                            Component.translatable(attribute.getDescriptionId()));
                                }
                            })
                            .toList();
                }
                break;
            }
        }
        return List.of();
    }
}