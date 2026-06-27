package muddy.domain_lib.entity;

import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.entity.custom.DomainClashEntity;
import muddy.domain_lib.entity.custom.DomainEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import static net.minecraft.core.Registry.register;

public class ModEntities {
    public static final EntityType<DomainEntity> DOMAIN_ENTITY = register(BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(MuddysDomainLib.MOD_ID, "domain_entity"),
            EntityType.Builder.of(DomainEntity::new, MobCategory.MISC).sized(1,1).build());

    public static final EntityType<DomainClashEntity> DOMAIN_CLASH_ENTITY = register(BuiltInRegistries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(MuddysDomainLib.MOD_ID, "domain_clash_entity"),
            EntityType.Builder.of(DomainClashEntity::new, MobCategory.MISC).sized(1,1).build());

    public static void initialize() {
        MuddysDomainLib.LOGGER.info("Chimeras Loaded");
    }
}
