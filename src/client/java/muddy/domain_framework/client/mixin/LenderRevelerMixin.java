package muddy.domain_framework.client.mixin;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import muddy.domain_framework.client.utils.TheVisibleSectionsThingy;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(LevelRenderer.class)
public class LenderRevelerMixin implements TheVisibleSectionsThingy {

    @Shadow
    private final ObjectArrayList<SectionRenderDispatcher.RenderSection> visibleSections = new ObjectArrayList<>(10000);

    @Override
    public ObjectArrayList<SectionRenderDispatcher.RenderSection> domain$getVisibleSections() {
        return visibleSections;
    }
}
