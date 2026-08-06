package muddy.domain_framework.client.mixin;

import muddy.domain_framework.block.custom.DomainBarrierBlock;
import muddy.domain_framework.client.MuddysDomainFrameworkClient;
import muddy.domain_framework.client.render.ModRenderTypes;
import muddy.domain_framework.util.HasDomainExpanded;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(ItemBlockRenderTypes.class)
public class ItemBlockRenderTypesMixin {
//    @Shadow @Final
//    private static Map<Block, RenderType> TYPE_BY_BLOCK;

//    @Inject(method = "<clinit>", at = @At("TAIL"))
//    private static void domain$blockTypes(CallbackInfo ci) {
//        TYPE_BY_BLOCK.put(ModBlocks.DOMAIN_BARRIER_BLOCK, ModRenderTypes.insideDomain());
//    }

    @Inject(method = "getChunkRenderType", at = @At("HEAD"), cancellable = true)
    private static void domain$getChunkRenderType(BlockState blockState, CallbackInfoReturnable<RenderType> cir) {
        Block block = blockState.getBlock();

        assert Minecraft.getInstance().player != null;
        if (((HasDomainExpanded) Minecraft.getInstance().player).domain$hasDomainExpanded() && block instanceof DomainBarrierBlock) {
            cir.setReturnValue(ModRenderTypes.insideDomain());
        }

    }

}
