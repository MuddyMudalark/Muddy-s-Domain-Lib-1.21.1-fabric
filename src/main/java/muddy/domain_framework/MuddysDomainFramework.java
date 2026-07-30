package muddy.domain_framework;

import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.effect.ModEffects;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.entity.custom.DomainClashEntity;
import muddy.domain_framework.entity.custom.DomainEntity;
import muddy.domain_framework.item.ModItems;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuddysDomainFramework implements ModInitializer {
    public static final String MOD_ID = "muddys-domain-framework";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        ModEffects.initialize();
        ModSounds.initialize();
        ModEntities.initialize();
        ModItems.initialize();
        ModBlocks.initialize();

        Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "no_horn"),
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "no_horn")));

        PayloadTypeRegistry.playS2C().register(DomainHasExpandedS2CPayload.ID, DomainHasExpandedS2CPayload.CODEC);

        FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_ENTITY, DomainEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashEntity.createAttributes());

        LOGGER.info("Domain Expansion: Malevolent Codebase");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
