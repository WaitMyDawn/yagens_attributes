package yagen.waitmydawn.registries;

import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import yagen.waitmydawn.YagensAttributes;
import yagen.waitmydawn.advancement.TemplateTrigger;
import yagen.waitmydawn.advancement.RivenCycleTrigger;

public class CriteriaRegistry {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS =
            DeferredRegister.create(BuiltInRegistries.TRIGGER_TYPES, YagensAttributes.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, RivenCycleTrigger> RIVEN_CYCLE =
            TRIGGERS.register("riven_cycle", RivenCycleTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, TemplateTrigger> FULLY_POLARIZED =
            TRIGGERS.register("fully_polarized", TemplateTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, TemplateTrigger> EXTERMINATE_ENDO =
            TRIGGERS.register("exterminate_endo", TemplateTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, TemplateTrigger> ASSASSINATION_ENDO =
            TRIGGERS.register("assassination_endo", TemplateTrigger::new);
    public static final DeferredHolder<CriterionTrigger<?>, TemplateTrigger> MAX_LEVEL =
            TRIGGERS.register("max_level", TemplateTrigger::new);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}