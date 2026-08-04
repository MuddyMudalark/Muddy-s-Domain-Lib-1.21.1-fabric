package muddy.domain_framework.client.utils;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;

public interface TheVisibleSectionsThingy {
    ObjectArrayList<SectionRenderDispatcher.RenderSection> domain$getVisibleSections();
}
