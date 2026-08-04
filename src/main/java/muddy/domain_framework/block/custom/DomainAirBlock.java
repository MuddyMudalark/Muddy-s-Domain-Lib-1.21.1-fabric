package muddy.domain_framework.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.UUID;

public class DomainAirBlock extends AirBlock {
    private Holder<MobEffect> domainEffect;
    private UUID domainOwnerUUID;
    private int domainEffectLength = 20;
    private boolean hasInitialized = false;
    private boolean hasExpandedFully = false;
    private BlockPos centerPosition;

    private boolean shouldTargetOwner = false;
    private boolean shouldTargetOthers = true;

    public void of(boolean hasExpandedFully, boolean hasInitialized, int domainEffectLength, UUID domainOwnerUUID, Holder<MobEffect> domainEffect, BlockPos centerPosition, boolean shouldTargetOwner, boolean shouldTargetOthers) {
        this.hasExpandedFully = hasExpandedFully;
        this.hasInitialized = hasInitialized;
        this.domainEffectLength = domainEffectLength;
        this.domainOwnerUUID = domainOwnerUUID;
        this.domainEffect = domainEffect;
        this.centerPosition = centerPosition;

        this.shouldTargetOwner = shouldTargetOwner;
        this.shouldTargetOthers = shouldTargetOthers;
    }

    public boolean shouldTargetOwner() {
        return this.shouldTargetOwner;
    }

    public boolean shouldTargetOthers() {
        return this.shouldTargetOthers;
    }

    public BlockPos getCenterPosition() {
        return centerPosition;
    }

    public void setCenterPosition(BlockPos centerPosition) {
        this.centerPosition = centerPosition;
    }

    public boolean getHasExpandedFully() {
        return hasExpandedFully;
    }

    protected boolean isDomainAir(BlockState blockState) {
        return true;
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
