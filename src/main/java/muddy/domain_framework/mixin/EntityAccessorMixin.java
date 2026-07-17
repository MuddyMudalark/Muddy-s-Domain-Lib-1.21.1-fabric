package muddy.domain_framework.mixin;

import muddy.domain_framework.util.RandomEntityAccessor;
import net.minecraft.util.RandomSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.swing.text.html.parser.Entity;

@Mixin(Entity.class)
public class EntityAccessorMixin implements RandomEntityAccessor {
    @Shadow
    protected final RandomSource random = RandomSource.create();

    @Override
    public RandomSource domain$antiModderArchitecture() {
        return random;
    }
}
