package muddy.domain_framework.item;

import muddy.domain_framework.MuddysDomainFramework;
import muddy.domain_framework.effect.ModEffects;
import muddy.domain_framework.item.custom.DomainSpawningItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;

import java.util.List;
import java.util.Optional;

public class ModItems {
    public static Item register(Item item, String id) {

        ResourceLocation itemID = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, id);

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

    public static final Item RED_BULL = register(
            new Item(new Item.Properties().food(
                    new FoodProperties(0, 0, true, 1, Optional.empty(), List.of(
                            new FoodProperties.PossibleEffect(
                                    new MobEffectInstance(ModEffects.RED_BULL), 60))
                    )
            )), "red_bull");

    public static void initialize() {
        MuddysDomainFramework.LOGGER.info("Shadows Loaded");
    }

}