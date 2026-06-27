package muddy.domain_lib.util;


import muddy.domain_lib.block.ModBlocks;
import muddy.domain_lib.block.custom.DomainAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.Level;

import java.util.UUID;

public class DomainClashBlockBuilder {
    public static void buildHollowInside(Level level, BlockPos centerPos, int radius) {
        radius -= 1;
        DomainAirBlock domainAir = (DomainAirBlock) ModBlocks.DOMAIN_CLASH_AIR_BLOCK;

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    int distanceSquare = x * x + y * y + z * z;

                    if (distanceSquare <= radius * radius) {
                        BlockPos pos = centerPos.offset(x, y, z);

                        level.setBlockAndUpdate(pos, domainAir.defaultBlockState());
                    }
                }
            }
        }

    }
}
