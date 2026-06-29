package muddy.domain_framework.block.custom;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.AirBlock;

import java.util.UUID;

public class DomainAirBlock extends AirBlock {
    private Holder<MobEffect> domainEffect;
    private UUID domainOwnerUUID;
    private int domainEffectLength = 20;
    private boolean hasInitialized = false;
    private boolean hasExpandedFully = false;

    public void of(boolean hasExpandedFully, boolean hasInitialized, int domainEffectLength, UUID domainOwnerUUID, Holder<MobEffect> domainEffect) {
        this.hasExpandedFully = hasExpandedFully;
        this.hasInitialized = hasInitialized;
        this.domainEffectLength = domainEffectLength;
        this.domainOwnerUUID = domainOwnerUUID;
        this.domainEffect = domainEffect;
    }

    public boolean getHasExpandedFully() {
        return hasExpandedFully;
    }

    public void setHasExpandedFully(boolean hasExpandedFully) {
        this.hasExpandedFully = hasExpandedFully;
    }

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
