package muddy.domain_framework;

import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.effect.ModEffects;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.entity.custom.DomainClashEntity;
import muddy.domain_framework.entity.custom.DomainEntity;
import muddy.domain_framework.item.ModItems;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.resources.ResourceLocation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuddysDomainFramework implements ModInitializer {
	public static final String MOD_ID = "muddys-domain-framework";


	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModEffects.initialize();
		ModEntities.initialize();
		ModItems.initialize();
		ModBlocks.initialize();

		FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_ENTITY, DomainEntity.createAttributes());
		FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashEntity.createAttributes());

		LOGGER.info("Domain Expansion: Malevolent Codebase");
	}

	public static ResourceLocation id(String path) {
		return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
	}
}
