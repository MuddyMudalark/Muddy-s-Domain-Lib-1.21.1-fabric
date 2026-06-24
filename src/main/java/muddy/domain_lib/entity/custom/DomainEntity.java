package muddy.domain_lib.entity.custom;

import muddy.domain_lib.MuddysDomainLib;
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
        this.ownerUUID = this.owner.getUUID();
    }

    public Player getOwner() {
        return this.owner;
    }

    public void setDomainRadius(int newRadius) {
        this.maxRadius = newRadius;
        this.yRadius = -newRadius;
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
        compoundTag.putBoolean("FirstLoad", false);
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

        this.firstLoad = compoundTag.getBoolean("FirstLoad");
        this.hasExpandedFully = compoundTag.getBoolean("HasDomainExpanded");
        this.age = compoundTag.getInt("DomainAge");
        this.lifetime = compoundTag.getInt("DomainLifetime");
        this.maxRadius = compoundTag.getInt("DomainRadius");
        this.domainEffect = MobEffect.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("DomainEffect"))
                .resultOrPartial(error -> MuddysDomainLib.LOGGER.info("The Overall Effect This Code Has on me is: {}", error))
                .orElse(MobEffects.LEVITATION);

        if (!compoundTag.getUUID("Owner").equals(null)) {
            this.ownerUUID = compoundTag.getUUID("Owner");
        }

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

        this.savedBlocks.clear();

        this.savedBlocks = mappedResults;

        List<String> nullList = new ArrayList<>();
        List<String> nonNullList = new ArrayList<>();

        for (String key: compoundTag.getAllKeys()) {
            Tag tag = compoundTag.get(key);

            if (tag.equals(null)) {
                nullList.add(tag.getAsString());
            } else {
                nonNullList.add(tag.getAsString());
            }
        }

        MuddysDomainLib.LOGGER.info("The Non-Null Objects Are: {}", nonNullList);
        MuddysDomainLib.LOGGER.info("The Null Objects Are: {}", nullList);

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
            if (!this.firstLoad && !hasReloaded) {
                this.radius = this.maxRadius;
                this.hasExpandedFully = true;

                if (!domainEffect.equals(null) && owner != null) {
                    MuddysDomainLib.LOGGER.info("Has been reloaded with domain effect: {}", domainEffect.getRegisteredName());

                    domainExpansionOnReload();

                    hasReloaded = true;
                }
            }
            if (this.firstTick) {
                if (!this.hasExpandedFully && this.firstLoad ) {
                    saveDomainBlocks();
                }
            } else {
                if (this.radius >= this.maxRadius) {
                    this.hasExpandedFully = true;

                    this.age++;
                } else if (this.expandTick && !this.hasExpandedFully) {
                    if (this.radius < 4) {
                        this.firstTicksDomainExpansion();
                    } else {
                        this.domainExpansion();
                    }

                    if (this.radius >= 13) {
                        yRadius+=3;
                    } else {
                        this.yRadius+=2;
                    }
                    this.radius++;
                    this.expandTick=false;
                } else {
                    this.ticksInBetweenExpansion++;

                    if (this.ticksInBetweenExpansion >= 4) {
                        this.ticksInBetweenExpansion=0;

                        this.expandTick=true;
                    }
                }
            }
            if (this.ownerUUID != null) {
                this.owner = this.level().getPlayerByUUID(this.ownerUUID);
            }
        } if (this.age >= this.lifetime || this.isDeadOrDying()) {
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
        DomainBlockBuilder.buildHollowInside(this.level(), this.blockPosition(), this.radius, this.domainEffect, this.owner, this.domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(this.level(), this.blockPosition(), this.radius);
    }

    public void domainExpansion() {
        DomainBlockBuilder.buildHollowInside(this.level(), this.blockPosition(), this.radius, this.domainEffect, this.owner, this.domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(this.level(), this.blockPosition(), this.radius);
        DomainBlockBuilder.buildHollowSphereDynamically(this.level(), this.blockPosition(), this.radius, this.yRadius);
    }

    public void domainExpansionOnReload() {
        DomainBlockBuilder.buildHollowInside(this.level(), this.blockPosition(), this.maxRadius, this.domainEffect, this.owner, this.domainEffectLength);

        DomainBlockBuilder.buildStandingSurface(this.level(), this.blockPosition(), this.maxRadius);
        DomainBlockBuilder.buildHollowSphere(this.level(), this.blockPosition(), this.maxRadius);
    }

    public void saveDomainBlocks() {
        int maxRadius = this.maxRadius + 1;

        for (int x = -maxRadius; x <= maxRadius; x++) {
            for (int y = -maxRadius; y <= maxRadius; y++) {
                for (int z = -maxRadius; z <= maxRadius; z++) {

                    int distSq = x * x + y * y + z * z;

                    if (distSq <= maxRadius * maxRadius) {
                        BlockPos pos = this.blockPosition().offset(x, y, z);

                        savedBlocks.put(pos.immutable(), this.level().getBlockState(pos));
                    }
                }
            }
        }
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
    protected @NotNull AABB makeBoundingBox() {
        return AABB.ofSize(new Vec3(1, 1, 1), 1, 1, 1);
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
