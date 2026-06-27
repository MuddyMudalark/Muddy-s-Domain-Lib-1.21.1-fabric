package muddy.domain_lib.entity.custom;

import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.entity.ModEntities;
import muddy.domain_lib.util.DomainBlockBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DomainEntity extends LivingEntity { ;
    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

    private Holder<MobEffect> domainEffect;
    private Player owner;
    private UUID ownerUUID;

    private int ticksInBetweenExpansion = 0;

    private int maxRadius = 15;
    private int radius = 1;
    private int yRadius = -maxRadius;

    private int age = 0;
    private int lifetime = 200;
    private int domainEffectLength = 20;

    private boolean firstLoad = true;
    private boolean hasReloaded = false;

    private boolean hasExpandedFully = false;
    private boolean expandTick = true;

    public DomainEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);

    }

    public static AttributeSupplier.Builder createAttributes() {
        return AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.STEP_HEIGHT)
                .add(Attributes.MOVEMENT_EFFICIENCY)
                .add(Attributes.SCALE)
                .add(Attributes.MAX_ABSORPTION);
    }

    public void setDomainEffectLength(int domainEffectLength) {
        this.domainEffectLength = domainEffectLength;
    }

    public int getDomainEffectLength() {
        return domainEffectLength;
    }

    public int getAge() {
        return this.age;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    public void setOwner(Player owner) {

        this.owner = owner;
        this.ownerUUID = owner.getUUID();
    }

    public Player getOwner() {
        return this.owner;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public void setDomainRadius(int newRadius) {
        this.maxRadius = newRadius;
        this.yRadius = -newRadius;
    }

    public int getDomainRadius() {
        return maxRadius;
    }

    public Holder<MobEffect> getDomainEffect() {
        return this.domainEffect;
    }

    public void setDomainEffect(Holder<MobEffect> domainEffect) {
        this.domainEffect = domainEffect;
    }

    @Override
    public boolean shouldRender(double d, double e, double f) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("DomainAge", this.age);
        compoundTag.putInt("DomainRadius", this.radius);
        compoundTag.putInt("DomainLifetime", this.lifetime);
        compoundTag.putBoolean("HasDomainExpanded", this.hasExpandedFully);
        compoundTag.put("DomainEffect", MobEffect.CODEC.encodeStart(NbtOps.INSTANCE, this.domainEffect).getOrThrow());

        if (this.owner != null) {
            compoundTag.putUUID("Owner", this.owner.getUUID());
        }

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
        MuddysDomainLib.LOGGER.info("Getting Info From Save Data");

//        firstLoad = compoundTag.getBoolean("FirstLoad");
        firstLoad = false;
        hasExpandedFully = compoundTag.getBoolean("HasDomainExpanded");
        age = compoundTag.getInt("DomainAge");
        lifetime = compoundTag.getInt("DomainLifetime");
        maxRadius = compoundTag.getInt("DomainRadius");
        domainEffect = MobEffect.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("DomainEffect"))
                .resultOrPartial(error -> MuddysDomainLib.LOGGER.info("The Overall Effect This Code Has on me is: {}", error))
                .orElse(MobEffects.LEVITATION);

        this.ownerUUID = compoundTag.getUUID("Owner");

        MuddysDomainLib.LOGGER.info("After Reading Data The Owner's UUID is: {}", (ownerUUID == null ? "NULL" : ownerUUID.toString()));


        ListTag posList = (ListTag) compoundTag.get("DomainBlocksPos");
        ListTag stateList = (ListTag) compoundTag.get("DomainBlockStates");

        List<BlockPos> blockPosList = new ArrayList<>(List.of());
        List<BlockState> blockStateList = new ArrayList<>(List.of());

        for (Tag tag : posList) {
            IntArrayTag intArray = (IntArrayTag) ((CompoundTag)tag).get("Pos");

            int x = intArray.get(0).getAsInt();
            int y = intArray.get(1).getAsInt();
            int z = intArray.get(2).getAsInt();

            blockPosList.add(new BlockPos(x,y,z));
        } for (Tag tag: stateList) {
            CompoundTag blockState = (CompoundTag) ((CompoundTag)tag).get("BlockState");

            BlockState state = BlockState.CODEC.parse(NbtOps.INSTANCE, blockState)
                    .resultOrPartial(error -> MuddysDomainLib.LOGGER.error("AHHHH WHAT THE FUCK STOP ERRORING YOU STUPID BLOCKSTATE: {}", error))
                    .orElse(Blocks.AIR.defaultBlockState());

            blockStateList.add(state);
        }

        Map<BlockPos, BlockState> mappedResults = new HashMap<>();

        for (int i = 0; i < blockPosList.size(); i++) {
            mappedResults.put(blockPosList.get(i), blockStateList.get(i));
        }

        savedBlocks.clear();

        savedBlocks = mappedResults;

        List<String> nullList = new ArrayList<>();
        List<String> nonNullList = new ArrayList<>();

        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        this.removeEffect(mobEffectInstance.getEffect());

        super.onEffectAdded(mobEffectInstance, entity);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (!firstLoad && !hasReloaded) {
                radius = maxRadius;
                hasExpandedFully = true;

                if (!domainEffect.equals(null) && ownerUUID != null) {
                    domainExpansionOnReload();

                    hasReloaded = true;
                }
            }
            if (firstTick) {
                if (!hasExpandedFully && firstLoad) {
                    saveDomainBlocks();
                    checkForClash();
                }
            } else {
                if (radius >= maxRadius) {
                    hasExpandedFully = true;

                    age++;
                } else if (!hasExpandedFully && expandTick) {
                    if (radius < 4) {
                        firstTicksDomainExpansion();
                    } else {
                        domainExpansion();
                    }

                    if (radius >= 13) {
                        yRadius+=3;
                    } else {
                        yRadius+=2;
                    }
                    radius++;
                    expandTick=false;
                } else {
                    ticksInBetweenExpansion++;

                    if (ticksInBetweenExpansion >= 4) {
                        ticksInBetweenExpansion=0;

                        expandTick=true;
                    }
                }
            }
            if (ownerUUID != null) {
                owner = level().getPlayerByUUID(ownerUUID);
            }
        } if (age >= lifetime || isDeadOrDying()) {
            closeDomain();
        } if (ownerCausesDomainExpansionToEnd()) {
            closeDomain();
        }

        super.tick();
    }

    private boolean ownerCausesDomainExpansionToEnd () {
        if (this.ownerUUID != null && this.level().getPlayerByUUID(this.ownerUUID) != null) {

            return this.owner.isDeadOrDying() || this.owner.distanceTo(this) > this.maxRadius;
        }

        return false;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    private void firstTicksDomainExpansion() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), radius, domainEffect, ownerUUID, domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
    }

    public void domainExpansion() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), radius, domainEffect, ownerUUID, domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), radius, yRadius);
    }

    public void domainExpansionOnReload() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), maxRadius, domainEffect, ownerUUID, domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), maxRadius);
        DomainBlockBuilder.buildHollowSphere(level(), blockPosition(), maxRadius);
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

    private List<DomainEntity> domainsInRange = new ArrayList<>();

    public void checkForClash() {
        for (int i = 0; i < 999; i++) {
            if (level().getEntity(i) != null)  {
                if (level().getEntity(i) instanceof DomainEntity domainEntity) {
                    if (domainEntity.distanceTo(this) <= maxRadius || this.distanceTo(domainEntity) <= domainEntity.getDomainRadius()) {
                        domainsInRange.add(domainEntity);
                    }
                }
            } else {
                if (!domainsInRange.isEmpty()) {
                    initiateDomainClash();
                }
            }
        }
    }

    public void initiateDomainClash() {
        Vec3 midpointCoordinates = position();

        int clashRadius = 0;
        int clashLifetime = 0;
        List<UUID> clashDomainOwnerUUIDs = List.of(ownerUUID);
        List<DomainEntity> domainClashParents = List.of(this);
        BlockPos clashPos = this.blockPosition();


        for (DomainEntity domainEntity : domainsInRange) {
            midpointCoordinates = midpointOfVectors(this.position(), domainEntity.position());

            clashRadius = Math.max(clashRadius, domainEntity.getDomainRadius());
            clashLifetime = Math.max(clashLifetime, domainEntity.getLifetime());
            clashDomainOwnerUUIDs.add(domainEntity.getOwner().getUUID());
            clashPos = new BlockPos((int) midpointCoordinates.x, (int) midpointCoordinates.y, (int) midpointCoordinates.z);
        }

        DomainClashEntity domainClashEntity = new DomainClashEntity(ModEntities.DOMAIN_CLASH_ENTITY, level());
        domainClashEntity.of(clashRadius, clashLifetime, clashDomainOwnerUUIDs, domainClashParents, clashPos);

        level().addFreshEntity(domainClashEntity);
    }

    public void closeDomain() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState oldState = entry.getValue();

            this.level().setBlockAndUpdate(savedBlockPos, oldState);
        }

        this.remove(RemovalReason.DISCARDED);
    }

    private Vec3 midpointOfVectors(Vec3 point1, Vec3 point2) {
        double x = (point1.x() + point2.x()) / 2;
        double y = (point1.y() + point2.y()) / 2;
        double z = (point1.z() + point2.z()) / 2;

        return new Vec3(x, y, z);
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
