package muddy.domain_framework.mixin;

import muddy.domain_framework.util.DomainAirSignalGetter;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelReader.class)
public interface LevelReaderAccessor extends DomainAirSignalGetter {

}
