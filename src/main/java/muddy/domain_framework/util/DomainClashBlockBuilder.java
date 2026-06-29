package muddy.domain_framework.util;


import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DomainClashBlockBuilder {
    public static void buildHollowInside(Level level, BlockPos centerPos, int radius) {
        radius -= 1;
        DomainClashAirBlock domainClashAir = (DomainClashAirBlock) ModBlocks.DOMAIN_CLASH_AIR_BLOCK;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    int distanceSquare = x * x + y * y + z * z;

                    if (distanceSquare <= radius * radius) {
                        BlockPos pos = centerPos.offset(x, y, z);

                        level.setBlockAndUpdate(pos, domainClashAir.defaultBlockState());
                    }
                }
            }
        }

    }
}
