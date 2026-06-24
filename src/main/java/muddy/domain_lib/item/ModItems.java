package muddy.domain_lib.item;

import muddy.domain_lib.MuddysDomainLib;
import muddy.domain_lib.item.custom.DomainSpawningItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;

public class ModItems {
    public static Item register(Item item, String id) {

        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(MuddysDomainLib.MOD_ID, id);

        Item registeredItem = Registry.register(BuiltInRegistries.ITEM, itemID, item);

        return registeredItem;
    }

    // This is how a domains sure hit will be determined from the used item
    public static final Item DOMAIN_TEST_ITEM = register(
            new DomainSpawningItem(new Item.Properties(), MobEffects.REGENERATION),
            "domain_test_item"
    );

    public static final Item DOMAIN_TEST_ITEM2 = register(
            new DomainSpawningItem(new Item.Properties(), MobEffects.LEVITATION),
            "domain_test_item2"
    );

    public static void initialize() {
        MuddysDomainLib.LOGGER.info("Shadows Loaded");
    }

}
