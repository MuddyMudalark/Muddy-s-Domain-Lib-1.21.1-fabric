package muddy.domain_lib.block.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.AirBlock;

public class DomainAirBlock extends AirBlock {
    Holder<MobEffect> domainEffect;

    public DomainAirBlock(Properties properties) {
        super(properties);
    }

    public void setDomainEffect(Holder<MobEffect> domainEffect) {
        this.domainEffect = domainEffect;
    }

    public Holder<MobEffect> getDomainEffect() {
        return this.domainEffect;
    }
}
