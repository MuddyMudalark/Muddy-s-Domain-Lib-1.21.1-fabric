package muddy.domain_framework.util;


import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.entity.custom.DomainEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class DomainBlockBuilder {
    public static void buildStandingSurface(Level level, BlockPos centerPos, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {

                int distanceSquare = x * x + z * z;

                if (distanceSquare <= radius * radius) {
                    BlockPos pos = centerPos.offset(x, -1, z);

                    level.setBlockAndUpdate(pos, ModBlocks.DOMAIN_BARRIER_BLOCK.defaultBlockState());
                }
            }
        }
    }

    public static void buildHollowSphereDynamically(Level level, BlockPos centerPos, int radius, int yValue) {
        int outerSquare = radius * radius;
        int innerSquare = (radius - 1) * (radius - 1);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                if (y <= yValue) {
                    for (int z = -radius; z <= radius; z++) {

                        int distanceSquare = x * x + y * y + z * z;

                        if (y < 0) {
                            if (distanceSquare <= radius * radius) {
                                BlockPos pos = centerPos.offset(x, y, z);

                                level.setBlockAndUpdate(pos, ModBlocks.DOMAIN_BARRIER_BLOCK.defaultBlockState());
                            }
                        } else if (distanceSquare <= outerSquare && distanceSquare >= innerSquare) {
                            BlockPos pos = centerPos.offset(x, y, z);

                            level.setBlockAndUpdate(pos, ModBlocks.DOMAIN_BARRIER_BLOCK.defaultBlockState());
                        }
                    }
                }
            }
        }

    }

    public static void buildHollowSphere(Level level, BlockPos centerPos, int radius) {
        int outerSquare = radius * radius;
        int innerSquare = (radius - 1) * (radius - 1);

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {

                    int distanceSquare = x * x + y * y + z * z;

                    if (distanceSquare <= outerSquare && distanceSquare >= innerSquare) {
                        BlockPos pos = centerPos.offset(x, y, z);

                        level.setBlockAndUpdate(pos, ModBlocks.DOMAIN_BARRIER_BLOCK.defaultBlockState());
                    }
                }
            }
        }
    }

    public static void buildHollowInside(Level level, BlockPos centerPos, DomainEntity domainEntity) {
        int radius = domainEntity.getExpandingRadius() - 1;
        DomainAirBlock domainAir = (DomainAirBlock) ModBlocks.DOMAIN_AIR_BLOCK;
        domainAir.of(
                domainEntity.getIfDomainHasFullyExpanded(),
                true,
                domainEntity.getDomainEffectLength(),
                domainEntity.getOwnerUUID(),
                domainEntity.getDomainEffect()
        );

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
