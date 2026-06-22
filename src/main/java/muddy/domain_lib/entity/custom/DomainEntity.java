package muddy.domain_lib.entity.custom;

import com.mojang.brigadier.context.CommandContext;
import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.util.DomainBlockBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.*;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class DomainEntity extends LivingEntity{ ;
    private Map<BlockPos, BlockState> savedBlocks = new HashMap<>();

    public Holder<MobEffect> domainEffect;

    private int ticksInBetweenExpansion = 0;
    private int maxRadius = 15;
    private int radius = 1;
    private int yRadius = -15;

    private int age = 0;
    private int lifetime = 1200;

    boolean hasExpandedFully = false;
    boolean expandTick = true;

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

    public int getAge() {
        return this.age;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
    }

    public int getLifetime() {
        return this.lifetime;
    }

    public void setDomainRadius(int newRadius) {
        this.maxRadius = newRadius;
        this.yRadius = -newRadius;
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
        compoundTag.putBoolean("HasDomainExpanded", this.hasExpandedFully);

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

//            MuddysDomainLib.LOGGER.info(posList.toString());

            compoundTag.put("DomainBlocksPos", posList);
            compoundTag.put("DomainBlockStates", stateList);

        }

        super.addAdditionalSaveData(compoundTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.hasExpandedFully = compoundTag.getBoolean("HasDomainExpanded");
        this.age = compoundTag.getInt("DomainAge");
        this.maxRadius = compoundTag.getInt("DomainRadius");

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
            CompoundTag blockStates = (CompoundTag) ((CompoundTag)tag).get("BlockState");

            MuddysDomainLib.LOGGER.info(blockStates.getAsString());

            blockPosList.add();
        }



        Map<BlockPos, BlockState> mappedResults = new HashMap<>();
        super.readAdditionalSaveData(compoundTag);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (this.firstTick) {
                if (!this.hasExpandedFully) {
                    saveDomainBlocks();
                }
            } else {
                if (this.expandTick && !this.hasExpandedFully) {
                    if (this.radius <= this.maxRadius) {
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
                    }
                } else if (this.radius > this.maxRadius) {
                    this.hasExpandedFully = true;

                    this.age++;

                    if (this.age >= this.lifetime || this.isDeadOrDying()) {
                        closeDomain();

                        this.remove(RemovalReason.DISCARDED);
                    }
                } else {
                    this.ticksInBetweenExpansion++;

                    if (this.ticksInBetweenExpansion >= 4) {
                        this.ticksInBetweenExpansion=0;

                        this.expandTick=true;
                    }
                }
            }
        }
        super.tick();
    }

    @Override
    protected @Nullable SoundEvent getDeathSound() {
        return SoundEvents.GLASS_BREAK;
    }

    private void firstTicksDomainExpansion() {
        DomainBlockBuilder.buildHollowInside(this.level(), this.blockPosition(), this.radius, this.domainEffect);

        DomainBlockBuilder.buildStandingSurface(this.level(), this.blockPosition(), this.radius);
    }

    public void domainExpansion() {
        DomainBlockBuilder.buildHollowInside(this.level(), this.blockPosition(), this.radius, this.domainEffect);

        DomainBlockBuilder.buildStandingSurface(this.level(), this.blockPosition(), this.radius);
        DomainBlockBuilder.buildHollowSphereDynamically(this.level(), this.blockPosition(), this.radius, this.yRadius);
    }

    public void saveDomainBlocks() {
        int thatRadius = maxRadius + 1;

        for (int x = -thatRadius; x <= thatRadius; x++) {
            for (int y = -thatRadius; y <= thatRadius; y++) {
                for (int z = -thatRadius; z <= thatRadius; z++) {

                    int distSq = x * x + y * y + z * z;

                    if (distSq <= thatRadius * thatRadius) {
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

    }

    @Override
    protected @NotNull AABB makeBoundingBox() {
        return AABB.ofSize(Vec3.ZERO, 0, 0, 0);
    }

    @Override
    public void setNoGravity(boolean bl) {
        bl = true;

        super.setNoGravity(bl);
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
