package muddy.domain_framework.client.utils;

import net.minecraft.core.BlockPos;

public interface DomainCenterPosition {
    BlockPos domain$getDomainCenter();
    boolean domain$shouldRenderInternalDomain();
    void domain$setDomainCenter(BlockPos domainCenter);
}
