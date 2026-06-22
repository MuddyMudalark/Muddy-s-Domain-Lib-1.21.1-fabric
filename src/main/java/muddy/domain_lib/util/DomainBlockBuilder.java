package muddy.domain_lib.util;


import muddy.domain_lib.block.ModBlocks;
import muddy.domain_lib.block.custom.DomainAirBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
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

                        if (distanceSquare <= outerSquare && distanceSquare >= innerSquare) {
                            BlockPos pos = centerPos.offset(x, y, z);

                            level.setBlockAndUpdate(pos, ModBlocks.DOMAIN_BARRIER_BLOCK.defaultBlockState());
                        }
                    }
                }
            }
        }

    }

    public static void buildHollowInside(Level level, BlockPos centerPos, int radius, Holder<MobEffect> domainEffect) {
        radius -= 1;
        DomainAirBlock domainAir = (DomainAirBlock) ModBlocks.DOMAIN_AIR_BLOCK;
        domainAir.setDomainEffect(domainEffect);

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
