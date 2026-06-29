package muddy.domain_framework.block;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, name);

        // Sometimes, you may not want to register an item for the block.
        // Eg: if it's a technical block like `minecraft:air` or `minecraft:end_gateway`
        if (shouldRegisterItem) {
            BlockItem blockItem = new BlockItem(block, new Item.Properties());
            Registry.register(BuiltInRegistries.ITEM, id, blockItem);
        }

        return Registry.register(BuiltInRegistries.BLOCK, id, block);
    }

    public static final Block DOMAIN_BARRIER_BLOCK = register(
            new DomainBarrierBlock(BlockBehaviour.Properties.of().strength(-1.0F, 3600000.0F).lightLevel((blockState) -> 15)),
            "domain_barrier",
            false
    );

    public static final Block DOMAIN_AIR_BLOCK = register(
            new DomainAirBlock(BlockBehaviour.Properties.of().lightLevel((blockState) -> 15)),
            "domain_air",
            false
    );

    public static final Block DOMAIN_CLASH_AIR_BLOCK = register(
            new DomainClashAirBlock(BlockBehaviour.Properties.of().lightLevel((blockState) -> 15)),
            "domain_clash_air",
            false
    );

    public static void initialize() {
        MuddysDomainFramework.LOGGER.info("Gardens Loaded");
    }

}
