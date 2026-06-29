package muddy.domain_framework.effect;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.effect.custom.DomainEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {
    public static final Holder<MobEffect> DOMAIN_EXPANDING;

    static {
        DOMAIN_EXPANDING = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "expanding-domain"),
                new DomainEffect(MobEffectCategory.NEUTRAL, 0)
        );
    }

    public static void initialize() {
        MuddysDomainFramework.LOGGER.info("Imagining A Version Of Myself That's Freely Surpassed His Limits");
    }
}
