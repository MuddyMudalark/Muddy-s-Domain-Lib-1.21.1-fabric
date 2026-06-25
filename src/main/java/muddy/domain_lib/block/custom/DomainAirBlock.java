package muddy.domain_lib.block.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AirBlock;

import java.util.UUID;

public class DomainAirBlock extends AirBlock {
    private Holder<MobEffect> domainEffect;
    private UUID domainOwnerUUID;
    private int domainEffectLength = 20;
    private boolean hasInitialized = false;

    public int getDomainEffectLength() {
        return domainEffectLength;
    }

    public void setDomainEffectLength(int domainEffectLength) {
        this.domainEffectLength = domainEffectLength;
    }

    public UUID getDomainOwnerUUID() {
        return domainOwnerUUID;
    }

    public void setDomainOwnerUUID(UUID domainOwnerUUID) {
        this.domainOwnerUUID = domainOwnerUUID;
    }

    public boolean getIfHasInitialized() {
        return hasInitialized;
    }

    public void setIfHasInitialized(boolean hasInitialized) {
        this.hasInitialized = hasInitialized;
    }

    public DomainAirBlock(Properties properties) {
        super(properties);
    }

    public void setDomainEffect(Holder<MobEffect> domainEffect) {
        this.domainEffect = domainEffect;
    }

    public Holder<MobEffect> getDomainEffect() {
        return  domainEffect;
    }
}
