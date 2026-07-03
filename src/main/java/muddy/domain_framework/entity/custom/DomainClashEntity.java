package muddy.domain_framework.entity.custom;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.util.DomainBlockBuilder;
import muddy.domain_framework.util.DomainClashBlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class DomainClashEntity extends LivingEntity {
    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

    private int ticksInBetweenExpansion = 0;

    private int maxRadius;
    private int radius = 5;
    private int yRadius = -maxRadius;
    private int lifetime = 1200;
    private int age = 0;

    private List<UUID> domainOwnerUUIDList = new ArrayList<>();
    private List<DomainEntity> domainClashParents = new ArrayList<>();

    private boolean isClashing = true;
    private boolean expandTick = true;

    private boolean killedAllParents = false;
    private boolean hasExpandedFully = false;

    private boolean firstTimeTicked = true;
    private boolean ownersHaveBeenTeleported = false;

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
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("DomainAge", this.age);
        compoundTag.putInt("DomainRadius", this.maxRadius);
        compoundTag.putInt("DomainLifetime", this.lifetime);
        compoundTag.putBoolean("HasDomainExpanded", this.hasExpandedFully);
//        compoundTag.put("DomainEffect", MobEffect.CODEC.encodeStart(NbtOps.INSTANCE, this.domainEffect).getOrThrow());

        compoundTag.put("OwnersUUIDs", UUIDUtil.CODEC.listOf().encodeStart(NbtOps.INSTANCE, this.domainOwnerUUIDList).getOrThrow());


        if (this.savedBlocks != null && !this.savedBlocks.isEmpty()) {
            ListTag posList = new ListTag();
            ListTag stateList = new ListTag();

            for (Map.Entry<BlockPos, BlockState> block : savedBlocks.entrySet()) {
                CompoundTag posEntry = new CompoundTag();
                CompoundTag stateEntry = new CompoundTag();

                posEntry.put("Pos", BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, block.getKey()).getOrThrow());

                stateEntry.put("BlockState", BlockState.CODEC.encodeStart(NbtOps.INSTANCE, block.getValue()).getOrThrow());

                posList.add(posEntry);
                stateList.add(stateEntry);
            }

            compoundTag.put("DomainBlocksPos", posList);
            compoundTag.put("DomainBlockStates", stateList);

        }

        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        hasExpandedFully = compoundTag.getBoolean("HasDomainExpanded");
        age = compoundTag.getInt("DomainAge");
        lifetime = compoundTag.getInt("DomainLifetime");
        maxRadius = compoundTag.getInt("DomainRadius");

        domainOwnerUUIDList = UUIDUtil.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.get("OwnersUUIDs")).getOrThrow();


        ListTag posList = (ListTag) compoundTag.get("DomainBlocksPos");
        ListTag stateList = (ListTag) compoundTag.get("DomainBlockStates");

        List<BlockPos> blockPosList = new ArrayList<>(List.of());
        List<BlockState> blockStateList = new ArrayList<>(List.of());

        assert posList != null;
        for (Tag tag : posList) {
            IntArrayTag intArray = (IntArrayTag) ((CompoundTag)tag).get("Pos");

            assert intArray != null;
            int x = intArray.get(0).getAsInt();
            int y = intArray.get(1).getAsInt();
            int z = intArray.get(2).getAsInt();

            blockPosList.add(new BlockPos(x,y,z));
        }
        assert stateList != null;
        for (Tag tag: stateList) {
            CompoundTag blockState = (CompoundTag) ((CompoundTag)tag).get("BlockState");

            BlockState state = BlockState.CODEC.parse(NbtOps.INSTANCE, blockState)
                    .resultOrPartial(error -> MuddysDomainFramework.LOGGER.error("Error With Blockstate of: {}", error))
                    .orElse(Blocks.AIR.defaultBlockState());

            blockStateList.add(state);
        }

        Map<BlockPos, BlockState> mappedResults = new HashMap<>();

        for (int i = 0; i < blockPosList.size(); i++) {
            mappedResults.put(blockPosList.get(i), blockStateList.get(i));
        }

        savedBlocks.clear();

        savedBlocks = mappedResults;

        super.readAdditionalSaveData(compoundTag);

        super.readAdditionalSaveData(compoundTag);
    }

    public boolean haveOwnersBeenTeleported() {
        return ownersHaveBeenTeleported;
    }

    public boolean isFullyExpanded() {
        return hasExpandedFully;
    }

    public int getMaxRadius() {
        return maxRadius;
    }

    public int getLifetime() {
        return lifetime;
    }

    public List<UUID> getDomainOwnerUUIDList() {
        return domainOwnerUUIDList;
    }

    public List<DomainEntity> getDomainClashParents() {
        return domainClashParents;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.KNOCKBACK_RESISTANCE)
                .add(Attributes.MOVEMENT_SPEED)
                .add(Attributes.ARMOR)
                .add(Attributes.ARMOR_TOUGHNESS)
                .add(Attributes.MAX_ABSORPTION)
                .add(Attributes.STEP_HEIGHT)
                .add(Attributes.SCALE)
                .add(Attributes.GRAVITY)
                .add(Attributes.SAFE_FALL_DISTANCE)
                .add(Attributes.FALL_DAMAGE_MULTIPLIER)
                .add(Attributes.JUMP_STRENGTH)
                .add(Attributes.OXYGEN_BONUS)
                .add(Attributes.BURNING_TIME)
                .add(Attributes.EXPLOSION_KNOCKBACK_RESISTANCE)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY)
                .add(Attributes.MOVEMENT_EFFICIENCY)
                .add(Attributes.ATTACK_KNOCKBACK);
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (!killedAllParents) {
                int domainsKilledCount = 0;

                for (DomainEntity domainClashParent : domainClashParents.reversed()) {
                    domainClashParent.closeDomain();

                    domainsKilledCount++;

                    if (domainsKilledCount == domainClashParents.size()) {
                        killedAllParents = true;
                    }
                }
            } else {
                if (firstTimeTicked) {
                    saveDomainBlocks();

                    int playerIndex = 0;
                    int playerCount = domainOwnerUUIDList.size();
                    int degreesPerPlayer = playerCount == 0 ? 90 : 360 / playerCount;
                    for (UUID ownerUUID : domainOwnerUUIDList) {

                        if (level().getPlayerByUUID(ownerUUID) != null) {
                            Player owner = level().getPlayerByUUID(ownerUUID);

                            int x = (int) (radius * Math.cos(degreesPerPlayer * playerIndex));
                            int z = (int) (radius * Math.sin(degreesPerPlayer * playerIndex));

                            assert owner != null;
                            owner.setPos(this.blockPosition().offset(x, 0, z).getBottomCenter());
                            playerIndex++;
                        }
                    }

                    if (playerIndex >= playerCount) {
                        ownersHaveBeenTeleported = true;
                    }

                    firstTimeTicked = false;
                } else {
                    if (!hasExpandedFully) {
                        if (radius >= maxRadius) {
                            isClashing = true;
                            hasExpandedFully = true;
                        } else if (expandTick) {
                            if (radius < 10) {
                                firstTicksDomainExpansion();
                            } else {
                                domainExpansion();

                                if (radius >= 13) {
                                    yRadius += 3;
                                } else {
                                    yRadius += 2;
                                }
                            }

                            radius++;

                            expandTick = false;
                        } else {
                            ticksInBetweenExpansion++;

                            if (ticksInBetweenExpansion >= 4) {
                                ticksInBetweenExpansion = 0;

                                expandTick = true;
                            }
                        }
                    } else if (isClashing) {
                        age++;
                    }
                }
            }

            if (age >= lifetime || isDeadOrDying()) {
                closeDomain();
            } if (ownersAllDieCauseDomainClashToEnd()) {
                closeDomain();
            }

        }

        super.tick();
    }

    private boolean ownersAllDieCauseDomainClashToEnd() {
        int ownersWhoDied = 0;
        for (UUID ownerUUID : domainOwnerUUIDList) {
            if (level().getPlayerByUUID(ownerUUID) != null) {
                Player owner = level().getPlayerByUUID(ownerUUID);

                assert owner != null;
                if (owner.isDeadOrDying() || owner.distanceTo(this) > this.maxRadius) {
                    ownersWhoDied++;
                }
            }
        }

        return ownersWhoDied >= domainOwnerUUIDList.size();
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

    public void firstTicksDomainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius, haveOwnersBeenTeleported());

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
    }

    public void domainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius, haveOwnersBeenTeleported());

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), radius, yRadius);
    }

    public void closeDomain() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState oldState = entry.getValue();

            level().setBlockAndUpdate(savedBlockPos, oldState);
        }

        this.remove(RemovalReason.DISCARDED);
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
    public @NotNull HumanoidArm getMainArm() {
        return HumanoidArm.RIGHT;
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
    public void setNoGravity(boolean bl) {
        bl = true;

        super.setNoGravity(bl);
    }

}
