package muddy.domain_framework.entity.custom;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.util.DomainBlockBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

public class DomainEntity extends LivingEntity {
    public static int DEFAULT_LIFETIME = 200;

    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

    private Holder<MobEffect> domainEffect;

    private Player owner;
    private UUID ownerUUID;

    private List<Entity> attachedEntities = new ArrayList<>();

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
    private boolean instantExpand = false;
    private boolean shouldTargetOwner = false;
    private boolean shouldTargetOthers = true;
    private boolean hasCheckedForClash = false;
    private boolean isClashing = false;

    public DomainEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);

    }

    public void of(Holder<MobEffect> domainEffect, int domainEffectLength, Vec3 position, Player owner, int maxRadius) {
        of(domainEffect, domainEffectLength, position, owner, maxRadius, 200);
    }

    public void of(Holder<MobEffect> domainEffect, int domainEffectLength, Vec3 position, Player owner, int maxRadius, int lifetime) {
        of(domainEffect, domainEffectLength, position, owner, maxRadius, lifetime, false);
    }

    public void of(Holder<MobEffect> domainEffect, int domainEffectLength, Vec3 position, Player owner, int maxRadius, int lifetime, boolean instantExpand) {
        of(domainEffect, domainEffectLength, position, owner, maxRadius, lifetime, new HashMap<>(), instantExpand);
    }

    public void of(Holder<MobEffect> domainEffect, int domainEffectLength, Vec3 position, Player owner, int maxRadius, int lifetime, Map<BlockPos, BlockState> savedBlocks, boolean instantExpand) {
        of(domainEffect, domainEffectLength, position, owner, maxRadius, lifetime, savedBlocks, instantExpand, false, true);
    }

    public void of (Holder<MobEffect> domainEffect, int domainEffectLength, Vec3 position, Player owner, int maxRadius, int lifetime, Map<BlockPos, BlockState> savedBlocks, boolean instantExpand, boolean shouldTargetOwner, boolean shouldTargetOthers) {
        this.domainEffect = domainEffect;
        this.domainEffectLength = domainEffectLength;
        this.setPos(position);
        this.setOwner(owner);
        this.maxRadius = maxRadius;
        this.lifetime = lifetime;
        this.instantExpand = instantExpand;

        this.shouldTargetOwner = shouldTargetOwner;
        this.shouldTargetOthers = shouldTargetOthers;

        this.savedBlocks.putAll(savedBlocks);
    }

    public boolean shouldTargetOwner() {
        return shouldTargetOwner;
    }

    public boolean shouldTargetOthers() {
        return shouldTargetOthers;
    }

    public void attachEntity(Entity entity) {
        attachedEntities.add(entity);
    }

    public void detachEntity(Entity entity) {
        if (attachedEntities.contains(entity)) {
            attachedEntities.remove(entity);
        } else {
            MuddysDomainFramework.LOGGER.info("{} already removed or never on list", entity);
        }
    }

    public List<Entity> getAttachedEntities() {
        return attachedEntities;
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return AttributeSupplier.builder()
                .add(Attributes.MAX_HEALTH)
                .add(Attributes.STEP_HEIGHT)
                .add(Attributes.MOVEMENT_EFFICIENCY)
                .add(Attributes.SCALE)
                .add(Attributes.MAX_ABSORPTION)
                .add(Attributes.WATER_MOVEMENT_EFFICIENCY);
    }

    public boolean isFullyExpanded() {
        return hasExpandedFully;
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

    public int getExpandingRadius() {
        return radius;
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
        compoundTag.putInt("DomainRadius", this.maxRadius);
        compoundTag.putInt("DomainLifetime", this.lifetime);
        compoundTag.putBoolean("HasDomainExpanded", this.hasExpandedFully);
        compoundTag.put("DomainEffect", MobEffect.CODEC.encodeStart(NbtOps.INSTANCE, this.domainEffect).getOrThrow());

        compoundTag.putBoolean("ShouldTargetOwner", this.shouldTargetOwner);
        compoundTag.putBoolean("ShouldTargetOthers", this.shouldTargetOthers);

        if (this.owner != null) {
            compoundTag.putUUID("Owner", this.owner.getUUID());
        }

        if (!this.savedBlocks.isEmpty()) {
            compoundTag.put("DomainBlocksPos", BlockPos.CODEC.listOf().encodeStart(NbtOps.INSTANCE, savedBlocks.keySet().stream().toList()).getOrThrow());
            compoundTag.put("DomainBlockStates", BlockState.CODEC.listOf().encodeStart(NbtOps.INSTANCE, savedBlocks.values().stream().toList()).getOrThrow());
        }

        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        firstLoad = false;
        hasExpandedFully = compoundTag.get("HasDomainExpanded") == null || compoundTag.getBoolean("HasDomainExpanded");
        age = compoundTag.get("DomainAge") == null ? 0 : compoundTag.getInt("DomainAge");
        lifetime = compoundTag.get("DomainLifetime") == null ? 300 : compoundTag.getInt("DomainLifetime");
        maxRadius = compoundTag.get("DomainRadius") == null ? 300 : compoundTag.getInt("DomainRadius");
        domainEffect = MobEffect.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("DomainEffect"))
                .resultOrPartial(error -> MuddysDomainFramework.LOGGER.info("The Overall Effect This Code Has on me is: {}", error))
                .orElse(MobEffects.LEVITATION);

        this.ownerUUID = compoundTag.getUUID("Owner");

        this.shouldTargetOwner = compoundTag.getBoolean("ShouldTargetOwner");
        this.shouldTargetOthers = compoundTag.getBoolean("ShouldTargetOthers");

        List<BlockPos> blockPosList = BlockPos.CODEC.listOf().parse(
                NbtOps.INSTANCE, compoundTag.get("DomainBlocksPos")).resultOrPartial(
                        error -> MuddysDomainFramework.LOGGER.info("What the scallop BlockPos List? {}", error))
                .orElse(List.of(BlockPos.ZERO));

        List<BlockState> blockStateList = BlockState.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.get("DomainBlockStates")).resultOrPartial(
                    error -> MuddysDomainFramework.LOGGER.info("What the scallop BlockState List? {}", error))
                .orElse(List.of(Blocks.AIR.defaultBlockState()));

        Map<BlockPos, BlockState> mappedResults = new HashMap<>();

        if (blockPosList.size() == blockStateList.size()) {
            MuddysDomainFramework.LOGGER.info("both lists are equal in length");
        }

        for (int index = 0; index < blockPosList.size(); index++) {
            mappedResults.put(blockPosList.get(index), blockStateList.get(index));
        }

        savedBlocks = mappedResults;

        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    protected void onEffectAdded(MobEffectInstance mobEffectInstance, @Nullable Entity entity) {
        this.removeEffect(mobEffectInstance.getEffect());

        super.onEffectAdded(mobEffectInstance, entity);
    }

    @Override
    public void tick() {
        this.setPos(blockPosition().getCenter());

        if (!this.level().isClientSide) {
            if (!firstLoad && !hasReloaded) {
                radius = maxRadius;
                hasExpandedFully = true;

                sendPlayersInDomainPacket();

                if (!domainEffect.equals(null) && ownerUUID != null) {
                    buildDomainExpansionOnReload();

                    hasReloaded = true;
                }
            }
            if (firstTick) {
                if (instantExpand) {
                    radius = maxRadius;

                    buildDomainExpansionOnReload();
                } else {
                    checkForClash();

                    shouldSaveDomain();
                }
            } else {
                if (radius >= maxRadius) {
                    hasExpandedFully = true;

                    if (age == 0) {
                        sendPlayersInDomainPacket();

                        buildDomainExpansionOnReload();
                    }

                    age++;
                    radius = maxRadius;

                } else if (!instantExpand && (!hasExpandedFully && expandTick) && !isClashing) {
                    incrementDomainExpansion();
                } else {
                    incrementBetweenExpansion();
                }
            }
            if (ownerUUID != null) {
                owner = level().getPlayerByUUID(ownerUUID);
            }
        }

        if (!attachedEntities.isEmpty()) {
            for (Entity entity : attachedEntities) {
                entity.setPos(this.position());
            }
        }

        shouldCloseDomain();

        super.tick();
    }

    private boolean ownerCausesDomainExpansionToEnd() {
        if (this.ownerUUID != null && this.level().getPlayerByUUID(this.ownerUUID) != null) {

            return this.owner.isDeadOrDying() || this.owner.distanceTo(this) > this.maxRadius;
        }

        return false;
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    public void incrementDomainExpansion() {
        if (radius < 4) {
            firstTicksBuildDomainExpansion();
        } else {
            buildDomainExpansion();
        }

        if (radius >= 13) {
            yRadius += 3;
        } else {
            yRadius += 2;
        }

        radius++;

        expandTick = false;
    }

    public void incrementBetweenExpansion() {
        ticksInBetweenExpansion++;

        if (ticksInBetweenExpansion >= 4) {
            ticksInBetweenExpansion = 0;

            expandTick = true;
        }
    }

    public void firstTicksBuildDomainExpansion() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), this);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
    }

    public void buildDomainExpansion() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), this);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius);
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), radius, yRadius);
    }

    public void buildDomainExpansionOnReload() {
        DomainBlockBuilder.buildHollowInside(level(), blockPosition(), this);

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), maxRadius);
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), maxRadius, maxRadius);
    }

    public void sendPlayersInDomainPacket() {
        DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(true);

        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
            if (player.distanceTo(this) <= maxRadius) {
                ServerPlayNetworking.send(player, payload);
            }
        }

    }

    public void shouldSaveDomain() {
        if (hasCheckedForClash && !hasExpandedFully && firstLoad) {
            DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(false);

            for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
                if (player.distanceTo(this) <= maxRadius) {
                    ServerPlayNetworking.send(player, payload);
                }
            }

            saveDomainBlocks();
        }
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

    public void checkForClash() {
        ArrayList<DomainEntity> domainsInRange = new ArrayList<>();
        ArrayList<DomainClashEntity> clashingDomainsInRange = new ArrayList<>();
        Map<UUID, Holder<MobEffect>> domainOwnersAndEffects = new HashMap<>();

        // Search For Entities In Range
        for (Entity entity : level().getEntities(this, AABB.encapsulatingFullBlocks(blockPosition().offset(-100, -100, -100), blockPosition().offset(100, 100, 100)))) {
            if (entity != null) {
                if (entity instanceof DomainEntity domainEntity) {
                    if (domainEntity.distanceTo(this) <= maxRadius || this.distanceTo(domainEntity) <= domainEntity.getDomainRadius()) {
                        if (!this.getUUID().equals(entity.getUUID())) {
                            domainsInRange.add(domainEntity);
                        }
                    }
                }
                if (entity instanceof DomainClashEntity domainClashEntity) {
                    if (domainClashEntity.distanceTo(this) <= maxRadius || this.distanceTo(domainClashEntity) <= domainClashEntity.getMaxRadius()) {
                        clashingDomainsInRange.add(domainClashEntity);
                    }
                }
            }
        }

        DomainClashEntity domainClash = new DomainClashEntity(ModEntities.DOMAIN_CLASH_ENTITY, level());

        if (!domainsInRange.isEmpty()) {
            Vec3 midpointCoordinates = position();

            int clashRadius = maxRadius;
            int clashLifetime = lifetime;
            List<UUID> clashDomainOwnerUUIDs = new ArrayList<>(List.of(ownerUUID));
            List<Holder<MobEffect>> clashingDomainEffects = new ArrayList<>(List.of(domainEffect));
            domainsInRange.add(this);
            BlockPos clashPos = this.blockPosition();

            for (DomainEntity domainEntity : domainsInRange) {
                if (!domainEntity.isFullyExpanded() || domainsInRange.size() > 1) {
                    midpointCoordinates = midpointOfVectors(midpointCoordinates, domainEntity.position());

                    clashRadius = Math.max(clashRadius, domainEntity.getDomainRadius());
                    clashLifetime = Math.max(clashLifetime, domainEntity.getLifetime());
                    clashDomainOwnerUUIDs.add(domainEntity.getOwner().getUUID());
                    clashingDomainEffects.add(domainEntity.getDomainEffect());
                    clashPos = new BlockPos((int) midpointCoordinates.x, (int) midpointCoordinates.y, (int) midpointCoordinates.z);


                } else {
                    clashRadius = domainEntity.getDomainRadius();
                    clashLifetime = domainEntity.getLifetime();
                    clashDomainOwnerUUIDs.add(domainEntity.getOwnerUUID());
                    clashingDomainEffects.add(domainEntity.getDomainEffect());
                    clashPos = domainEntity.blockPosition();
                }

                domainOwnersAndEffects.put(domainEntity.getOwnerUUID(), domainEntity.getDomainEffect());
            }

            domainClash.of(clashRadius, clashLifetime, domainsInRange, clashPos);

        }
        if (!clashingDomainsInRange.isEmpty()) {
            Vec3 midpointCoordinates = position();

            int clashRadius = maxRadius;
            int clashLifetime = lifetime;
            List<UUID> clashDomainOwnerUUIDs = new ArrayList<>(List.of(ownerUUID));
            BlockPos clashPos = this.blockPosition();


            for (DomainClashEntity domainClashEntity : clashingDomainsInRange) {
                if (!domainClashEntity.isFullyExpanded() || clashingDomainsInRange.size() > 1) {
                    midpointCoordinates = midpointOfVectors(midpointCoordinates, domainClashEntity.position());

                    clashRadius = Math.max(clashRadius, domainClashEntity.getMaxRadius());
                    clashLifetime = Math.max(clashLifetime, domainClashEntity.getLifetime());

                    for (UUID ownerUUID : domainClashEntity.getDomainOwnerUUIDList()) {
                        if (!clashDomainOwnerUUIDs.contains(ownerUUID)) {
                            clashDomainOwnerUUIDs.add(ownerUUID);
                        }
                    }

                    domainsInRange.addAll(domainClashEntity.getDomainClashParents());

                    clashPos = new BlockPos((int) midpointCoordinates.x, (int) midpointCoordinates.y, (int) midpointCoordinates.z);
                } else {
                    clashRadius = domainClashEntity.getMaxRadius();
                    clashLifetime = domainClashEntity.getLifetime();

                    domainsInRange.addAll(domainClashEntity.getDomainClashParents());
                    clashPos = domainClashEntity.blockPosition();
                }

                for (UUID ownerUUID : domainClashEntity.getDomainOwnerUUIDList()) {
                    for (DomainEntity domainEntity : domainClashEntity.getDomainClashParents()) {
                        if (ownerUUID.equals(domainEntity.getOwnerUUID())) {
                            domainOwnersAndEffects.put(ownerUUID, domainEntity.getDomainEffect());
                        }
                    }
                }
            }

            domainClash.of(clashRadius, clashLifetime, domainsInRange, clashPos);
        }

        isClashing = (!domainsInRange.isEmpty() || !clashingDomainsInRange.isEmpty());

        if (isClashing) {
            level().addFreshEntity(domainClash);

            if (!domainsInRange.isEmpty()) {
                for (DomainEntity domain : domainsInRange) {
                    domain.replaceDomainSpace();
                }
            }
            if (!clashingDomainsInRange.isEmpty()) {
                for (DomainClashEntity clash : clashingDomainsInRange) {
                    clash.replaceDomainSpace();
                }
            }
        }

        MuddysDomainFramework.LOGGER.info("Clash Should Initialise {}", isClashing);

        hasCheckedForClash = true;
    }

    public boolean shouldCloseDomain() {
        if (age >= lifetime || isDeadOrDying()) {
            killAttachedEntities();
            replaceDomainSpace();

            return true;
        }
        if (ownerCausesDomainExpansionToEnd()) {
            killAttachedEntities();
            replaceDomainSpace();

            return true;
        }

        return false;
    }

    private void killAttachedEntities() {
        for (Entity attachedEntity: attachedEntities) {
            attachedEntity.discard();
        }
    }

    public void replaceDomainSpace() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState oldState = entry.getValue();

            if (!(oldState.getBlock() instanceof DomainAirBlock) || !(oldState.getBlock() instanceof DomainClashAirBlock) || !(oldState.getBlock() instanceof DomainBarrierBlock)) {
                this.level().setBlockAndUpdate(savedBlockPos, oldState);
            }

        }

        DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(false);

        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
            if (player.distanceTo(this) <= maxRadius) {
                ServerPlayNetworking.send(player, payload);
            }
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
