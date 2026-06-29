package muddy.domain_framework.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class DomainEffect extends MobEffect {
    public DomainEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int i) {
        super.onEffectAdded(livingEntity, i);
    }
}
