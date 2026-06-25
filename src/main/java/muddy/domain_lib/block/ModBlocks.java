package muddy.domain_lib.block;

import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.block.custom.DomainAirBlock;
import muddy.domain_lib.block.custom.DomainBarrierBlock;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
    public static Block register(Block block, String name, boolean shouldRegisterItem) {
        // Register the block and its item.
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(MuddysDomainLib.MOD_ID, name);

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

    public static void initialize() {
        MuddysDomainLib.LOGGER.info("Gardens Loaded");
    }

}
