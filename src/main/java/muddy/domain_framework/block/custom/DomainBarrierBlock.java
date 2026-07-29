package muddy.domain_framework.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;

public class DomainBarrierBlock extends Block {
    private BlockPos centerOfDomain;

    public DomainBarrierBlock(Properties properties) {
        super(properties);
    }

    public void setCenterOfDomain(BlockPos centerOfDomain) {
        this.centerOfDomain = centerOfDomain;
    }

    public BlockPos getCenterOfDomain() {
        return centerOfDomain;
    }

}
