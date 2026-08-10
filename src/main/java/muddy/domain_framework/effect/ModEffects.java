package muddy.domain_framework.effect;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.effect.custom.RedBullEffect;
import muddy.domain_framework.effect.custom.SimpleDomainEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ModEffects {
    public static final Holder<MobEffect> RED_BULL;
    public static final Holder<MobEffect> EXAMPLE_SIMPLE_DOMAIN;

    static {
        RED_BULL = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "expanding-domain"),
                new RedBullEffect(MobEffectCategory.NEUTRAL, 0)
        );

        EXAMPLE_SIMPLE_DOMAIN = Registry.registerForHolder(
                BuiltInRegistries.MOB_EFFECT,
                ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, "example_simple_domain"),
                new SimpleDomainEffect(MobEffectCategory.BENEFICIAL, 0)
        );
    }

    public static void initialize() {
        MuddysDomainFramework.LOGGER.info("Imagining A Version Of Myself That's Freely Surpassed His Limits");
    }
}
