package muddy.domain_lib;

import muddy.domain_lib.block.ModBlocks;
import muddy.domain_lib.entity.ModEntities;
import muddy.domain_lib.entity.custom.DomainEntity;
import muddy.domain_lib.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuddysDomainLib implements ModInitializer {
	public static final String MOD_ID = "muddys-domain-lib";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();

		FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_ENTITY, DomainEntity.createAttributes());

		LOGGER.info("Domain Expansion: Malevolent Codebase");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
