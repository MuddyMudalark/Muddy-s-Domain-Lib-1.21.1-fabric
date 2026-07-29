package muddy.domain_framework.sounds;

import muddy.domain_framework.MuddysDomainFramework;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    private ModSounds() {

    }

    public static final SoundEvent ITEM_NO_HORN = registerSound("no_horn");

    private static SoundEvent registerSound(String id) {
        ResourceLocation identifier = ResourceLocation.fromNamespaceAndPath(MuddysDomainFramework.MOD_ID, id);
        return Registry.register(BuiltInRegistries.SOUND_EVENT, identifier, SoundEvent.createFixedRangeEvent(identifier, 1));
    }

    public static void initialize() {
        MuddysDomainFramework.LOGGER.info("Domain Expansion:");
    }
}
