package muddy.domain_lib.entity.custom;

import muddy.domain_lib.util.DomainBlockBuilder;
import muddy.domain_lib.util.DomainClashBlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DomainClashEntity extends LivingEntity {
    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

    private int lifetime = 1200;

    private int age = 0;

    private int ticksInBetweenExpansion = 0;

    private int maxRadius;
    private int radius = 1;
    private int yRadius = -maxRadius;

    private BlockPos centerPos;

    private List<UUID> domainOwnerUUIDList;
    private List<DomainEntity> domainClashParents;

    private boolean isClashing = true;
    private boolean expandTick = true;

    private boolean killedAllParents = false;
    private boolean hasExpandedFully = false;

    public DomainClashEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public void of(int radius, int lifetime, List<UUID> domainOwnerUUIDList, List<DomainEntity> domainClashParents, BlockPos centerPos) {
        this.maxRadius = radius;
        this.lifetime = lifetime;
        this.domainOwnerUUIDList = domainOwnerUUIDList;
        this.domainClashParents = domainClashParents;
        this.setPos(centerPos.getCenter());
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            int domainsKilledCount = 0;
            for (DomainEntity domainClashParent : domainClashParents) {
                domainClashParent.kill();

                if (domainsKilledCount == domainClashParents.size()) {
                    killedAllParents = true;
                }
            }

            if (killedAllParents) {
                // I am the darkness
                saveDomainBlocks();
            }

            if (!hasExpandedFully) {
                if (radius >= maxRadius) {
                    isClashing = true;
                    hasExpandedFully = true;
                } else if (expandTick) {
                    if (radius < 4) {
                        firstTicksDomainExpansion();
                    } else {
                        domainExpansion();
                    }

                    if (radius >= 13) {
                        yRadius += 3;
                    } else {
                        yRadius += 2;
                    }

                    radius = radius < maxRadius ? radius++ : maxRadius;
                    expandTick = false;
                } else {
                    ticksInBetweenExpansion++;

                    if (ticksInBetweenExpansion >= 4) {
                        ticksInBetweenExpansion = 0;

                        expandTick = true;
                    }
                }
            } else if (isClashing) {

            }

        }

        super.tick();
    }

    public void saveDomainBlocks() {
        int maxRadius = this.maxRadius + 1;

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {

                    int distSq = x * x + y * y + z * z;

                    if (distSq <= maxRadius * maxRadius) {
                        BlockPos pos = blockPosition().offset(x, y, z);

                        savedBlocks.put(pos.immutable(), level().getBlockState(pos));
                    }
                }
            }
        }
    }

    private void firstTicksDomainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
    }

    public void domainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), radius, yRadius);
    }

    public void closeDomain() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState oldState = entry.getValue();

            this.level().setBlockAndUpdate(savedBlockPos, oldState);
        }

        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public void setNoGravity(boolean bl) {
        bl = true;

        super.setNoGravity(bl);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected double getDefaultGravity() {
        return 0;
    }

    @Override
    public @NotNull Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public @NotNull ItemStack getItemBySlot(EquipmentSlot equipmentSlot) {
        return ItemStack.EMPTY;
    }

    @Override
    public void setItemSlot(EquipmentSlot equipmentSlot, ItemStack itemStack) {

    }

    @Override
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
    }
}
