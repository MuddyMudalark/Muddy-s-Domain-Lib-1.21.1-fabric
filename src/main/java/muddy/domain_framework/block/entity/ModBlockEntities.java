package muddy.domain_framework.block.entity;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.block.entity.custom.DomainBarrierEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModBlockEntities {

    private static <T extends BlockEntity> BlockEntityType<T> register(
            String name,
            BlockEntityType.BlockEntitySupplier<? extends T> entityFactory,
            Block... blocks) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, name);
        return Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE, id, BlockEntityType.Builder.<T>of(entityFactory, blocks).build());
    }

    public static final BlockEntityType<DomainBarrierEntity> DOMAIN_BLOCK_ENTITY =
            register("domain_block_entity", DomainBarrierEntity::new, ModBlocks.DOMAIN_BARRIER_BLOCK);
}
