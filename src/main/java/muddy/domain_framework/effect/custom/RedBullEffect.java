package muddy.domain_framework.effect.custom;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class RedBullEffect extends MobEffect {
    public RedBullEffect(MobEffectCategory mobEffectCategory, int i) {
        super(mobEffectCategory, i);
    }

    @Override
    public void onEffectAdded(LivingEntity livingEntity, int i) {
        livingEntity.kill();

        super.onEffectAdded(livingEntity, i);
    }
}
