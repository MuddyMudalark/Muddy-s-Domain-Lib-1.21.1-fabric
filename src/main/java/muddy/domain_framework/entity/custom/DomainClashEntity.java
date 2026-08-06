package muddy.domain_framework.entity.custom;

import com.mojang.serialization.Codec;
import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.util.ClashScoreAccessor;
import muddy.domain_framework.util.DomainBlockBuilder;
import muddy.domain_framework.util.DomainClashBlockBuilder;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class DomainClashEntity extends LivingEntity {
    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();
    private ListTag savedBlockEntities = new ListTag();
    private Map<UUID, Holder<MobEffect>> ownersAndDomainEffects = new HashMap<>();
    private List<ResourceLocation> clashingShaderPaths = new ArrayList<>();

    private int ticksInBetweenExpansion = 0;

    private int maxRadius;
    private int radius = 5;
    private int yRadius = -maxRadius;
    private int lifetime = 2400;
    private int domainLifetime = 1200;

    private int playerCount = 0;
    private int degreesPerPlayer = 90;

    private int age = 0;

    private List<DomainEntity> domainClashParents = new ArrayList<>();
    private Map<UUID, Integer> domainEffectLengths = new HashMap<>();
    private Player clashWinner = null;

    private boolean isClashing = true;
    private boolean expandTick = true;

    private boolean hasExpandedFully = false;

    private boolean firstTimeTicked = true;
    private boolean ownersHaveBeenTeleported = false;
    private boolean tickedHurt = false;

    public DomainClashEntity(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    public void of(int radius, int lifetime, List<DomainEntity> domainEntities, BlockPos centerPos, List<ResourceLocation> clashingShaderPaths) {
        this.maxRadius = radius;
        this.domainLifetime = lifetime;

        for (DomainEntity domain : domainEntities) {
            ownersAndDomainEffects.put(domain.getOwnerUUID(), domain.getDomainEffect());
            domainEffectLengths.put(domain.getOwnerUUID(), domain.getDomainEffectLength());
        }

        this.playerCount = ownersAndDomainEffects.size();
        this.degreesPerPlayer = playerCount == 0 ? 90 : 360 / playerCount;

        this.clashingShaderPaths = clashingShaderPaths;
        this.setPos(centerPos.getCenter());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compoundTag) {
        compoundTag.putInt("DomainAge", this.age);
        compoundTag.putInt("DomainRadius", this.maxRadius);
        compoundTag.putInt("DomainLifetime", this.lifetime);
        compoundTag.putBoolean("HasDomainExpanded", this.hasExpandedFully);
        compoundTag.put("DomainEffects", MobEffect.CODEC.listOf().encodeStart(NbtOps.INSTANCE, ownersAndDomainEffects.values().stream().toList()).getOrThrow());

        compoundTag.put("OwnersUUIDs", UUIDUtil.CODEC.listOf().encodeStart(NbtOps.INSTANCE, ownersAndDomainEffects.keySet().stream().toList()).getOrThrow());

        compoundTag.put("DomainEffectLengths", Codec.INT.listOf().encodeStart(NbtOps.INSTANCE, domainEffectLengths.values().stream().toList()).getOrThrow());

        compoundTag.put("ShaderPaths", ResourceLocation.CODEC.listOf().encodeStart(NbtOps.INSTANCE, clashingShaderPaths).getOrThrow());

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

        compoundTag.put("ReplacedBlockEntities", savedBlockEntities);

        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        hasExpandedFully = compoundTag.getBoolean("HasDomainExpanded");
        age = compoundTag.getInt("DomainAge");
        lifetime = compoundTag.getInt("DomainLifetime");
        maxRadius = compoundTag.getInt("DomainRadius");
        firstTimeTicked = false;

        List<UUID> domainOwnerUUIDList = UUIDUtil.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.get("OwnersUUIDs")).getOrThrow();
        List<Holder<MobEffect>> domainEffects = MobEffect.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.get("DomainEffects")).getOrThrow();
        List<Integer> domainEffectLengths = Codec.INT.listOf().parse(NbtOps.INSTANCE, compoundTag.get("DomainEffectLengths")).getOrThrow();

        listsToMap(domainOwnerUUIDList, domainEffects);
        listsToMap(domainOwnerUUIDList, domainEffectLengths);

        ListTag posList = (ListTag) compoundTag.get("DomainBlocksPos");
        ListTag stateList = (ListTag) compoundTag.get("DomainBlockStates");

        List<BlockPos> blockPosList = new ArrayList<>(List.of());
        List<BlockState> blockStateList = new ArrayList<>(List.of());

        clashingShaderPaths = ResourceLocation.CODEC.listOf().parse(NbtOps.INSTANCE, compoundTag.get("ShaderPaths")).getOrThrow();

        assert posList != null;
        for (Tag tag : posList) {
            IntArrayTag intArray = (IntArrayTag) ((CompoundTag) tag).get("Pos");

            assert intArray != null;
            int x = intArray.get(0).getAsInt();
            int y = intArray.get(1).getAsInt();
            int z = intArray.get(2).getAsInt();

            blockPosList.add(new BlockPos(x, y, z));
        }
        assert stateList != null;
        for (Tag tag : stateList) {
            CompoundTag blockState = (CompoundTag) ((CompoundTag) tag).get("BlockState");

            BlockState state = BlockState.CODEC.parse(NbtOps.INSTANCE, blockState)
                    .resultOrPartial(error -> MuddysDomainFramework.LOGGER.error("Error With Blockstate of: {}", error))
                    .orElse(Blocks.AIR.defaultBlockState());

            blockStateList.add(state);
        }

        Map<BlockPos, BlockState> mappedResults = new HashMap<>();

        for (int i = 0; i < blockPosList.size(); i++) {
            mappedResults.put(blockPosList.get(i), blockStateList.get(i));
        }

        this.playerCount = ownersAndDomainEffects.size();
        this.degreesPerPlayer = playerCount == 0 ? 90 : 360 / playerCount;

        savedBlockEntities = (ListTag) compoundTag.get("ReplacedBlockEntities");
        savedBlocks.clear();
        savedBlocks = mappedResults;

        super.readAdditionalSaveData(compoundTag);
    }

    public static <K, V> Map<K, V> listsToMap(List<K> keys, List<V> values) {
        return IntStream.range(0, keys.size()).boxed()
                .collect(Collectors.toMap(keys::get, values::get));
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
        return ownersAndDomainEffects.keySet().stream().toList();
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
            if (firstTimeTicked && firstTick) {
                saveDomainBlocks();

                int playerIndex = 0;
                int playerCount = ownersAndDomainEffects.size();
                int degreesPerPlayer = playerCount == 0 ? 90 : 360 / playerCount;
                for (UUID ownerUUID : ownersAndDomainEffects.keySet()) {

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

                    DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(true);

                    for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
                        if (player.distanceTo(this) <= maxRadius) {
                            ServerPlayNetworking.send(player, payload);
                        }
                    }
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
//                    MuddysDomainFramework.LOGGER.info("Beep (Clashing)");
                    for (UUID ownerUUID : ownersAndDomainEffects.keySet()) {
                        Player player = level().getPlayerByUUID(ownerUUID);
                        if (player != null) {
                            for (UUID ownerUUID2 : ownersAndDomainEffects.keySet()) {
                                if (!ownerUUID.equals(ownerUUID2)) {
                                    Player player2 = level().getPlayerByUUID(ownerUUID2);

                                    if (player2 != null && player != null) {
                                        if (player.hurtTime == 1) {
                                            if (player.getLastHurtByMob() == player2) {

                                                ((ClashScoreAccessor) player2).domain$incrementClashScore();
                                            }
                                        }
                                    }
                                }
                            }

                            if (((ClashScoreAccessor) player).domain$getClashScore() >= 10) {
                                clashWinner = player;

                                endDomainClashWithWinner();
                            }
                        }


                    }

                    age++;
                }
            }


            if (age >= lifetime || isDeadOrDying()) {
                MuddysDomainFramework.LOGGER.info("You're taking too long");

                replaceDomainSpace();
            }
            if (ownersAllDieCauseDomainClashToEnd()) {
                MuddysDomainFramework.LOGGER.info("You are dead lmao");

                replaceDomainSpace();
            }

        }

        super.tick();
    }

    private boolean ownersAllDieCauseDomainClashToEnd() {
        int ownersWhoDied = 0;
        for (UUID ownerUUID : ownersAndDomainEffects.keySet()) {
            if (level().getPlayerByUUID(ownerUUID) != null) {
                Player owner = level().getPlayerByUUID(ownerUUID);

                assert owner != null;
                if (owner.isDeadOrDying() || owner.distanceTo(this) > this.maxRadius) {
                    ownersWhoDied++;
                }
            }
        }

        return ownersWhoDied >= ownersAndDomainEffects.size();
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

                        if (level().getBlockEntity(pos) != null) {
                            savedBlockEntities.add(level().getBlockEntity(pos).saveWithFullMetadata(level().registryAccess()));
                        }

                    }
                }
            }
        }
    }

    public void firstTicksDomainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius, haveOwnersBeenTeleported());

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius, getFirstShaderPath());
    }

    public void endDomainClashWithWinner() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState blockState = entry.getValue();

            if (!(blockState.getBlock() instanceof DomainAirBlock) || !(blockState.getBlock() instanceof DomainClashAirBlock) || !(blockState.getBlock() instanceof DomainBarrierBlock)) {
                this.level().setBlockAndUpdate(savedBlockPos, blockState);
            } else {
                this.level().setBlockAndUpdate(savedBlockPos, Blocks.AIR.defaultBlockState());
            }

            if (!savedBlockEntities.isEmpty()) {
                if (level().getBlockEntity(savedBlockPos) != null) {
                    for (Tag tag : savedBlockEntities) {
                        BlockEntity reconstructedBlockEntity = level().getBlockEntity(savedBlockPos);

                        assert reconstructedBlockEntity != null;
                        reconstructedBlockEntity.loadWithComponents((CompoundTag) tag, level().registryAccess());

                        if (reconstructedBlockEntity.getBlockPos() == savedBlockPos) {
                            this.level().setBlockEntity(reconstructedBlockEntity);
                        }
                    }
                }
            }
        }

        DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(false);

        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
            if (player.distanceTo(this) <= maxRadius) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        UUID winnerUUID = clashWinner.getUUID();

        DomainEntity domainEntity = new DomainEntity(ModEntities.DOMAIN_ENTITY, level());
        domainEntity.of(ownersAndDomainEffects.get(winnerUUID), domainEffectLengths.get(winnerUUID), position(), clashWinner, maxRadius, domainLifetime, savedBlocks, true);

        clashWinner.setPos(this.position());

        level().addFreshEntity(domainEntity);

        replaceDomainSpace();
    }

    public void domainExpansion() {
        DomainClashBlockBuilder.buildHollowInside(level(), blockPosition(), radius, haveOwnersBeenTeleported());

        DomainBlockBuilder.buildStandingSurface(level(), blockPosition(), radius, getFirstShaderPath());
        DomainBlockBuilder.buildHollowSphereDynamically(level(), blockPosition(), radius, yRadius, getFirstShaderPath());
    }

    public void replaceDomainSpace() {
        for (Map.Entry<BlockPos, BlockState> entry : savedBlocks.entrySet()) {
            BlockPos savedBlockPos = entry.getKey();
            BlockState blockState = entry.getValue();

            if (!(blockState.getBlock() instanceof DomainAirBlock) || !(blockState.getBlock() instanceof DomainClashAirBlock) || !(blockState.getBlock() instanceof DomainBarrierBlock)) {
                this.level().setBlockAndUpdate(savedBlockPos, blockState);
            } else {
                this.level().setBlockAndUpdate(savedBlockPos, Blocks.AIR.defaultBlockState());
            }

            if (!savedBlockEntities.isEmpty()) {
                if (level().getBlockEntity(savedBlockPos) != null) {
                    for (Tag tag : savedBlockEntities) {
                        BlockEntity reconstructedBlockEntity = level().getBlockEntity(savedBlockPos);

                        assert reconstructedBlockEntity != null;
                        reconstructedBlockEntity.loadWithComponents((CompoundTag) tag, level().registryAccess());

                        if (reconstructedBlockEntity.getBlockPos() == savedBlockPos) {
                            this.level().setBlockEntity(reconstructedBlockEntity);
                        }
                    }
                }
            }
        }

        DomainHasExpandedS2CPayload payload = new DomainHasExpandedS2CPayload(false);

        for (ServerPlayer player : PlayerLookup.world((ServerLevel) level())) {
            if (player.distanceTo(this) <= maxRadius) {
                ServerPlayNetworking.send(player, payload);
            }
        }

        for (UUID playerUUID : ownersAndDomainEffects.keySet().stream().toList()) {
            Player player = level().getPlayerByUUID(playerUUID);

            if (player != null) {
                ((ClashScoreAccessor) player).domain$setClashScore(0);
            }

        }

        this.remove(RemovalReason.DISCARDED);
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
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

    public ResourceLocation getFirstShaderPath() {
        return clashingShaderPaths.getFirst();
    }

    public List<ResourceLocation> getClashingShaderPaths() {
        return clashingShaderPaths;
    }

    public void setClashingShaderPaths(List<ResourceLocation> clashingShaderPaths) {
        this.clashingShaderPaths = clashingShaderPaths;
    }
}
