package muddy.domain_framework.client;

import com.mojang.blaze3d.shaders.Uniform;
import com.mojang.blaze3d.vertex.*;
import muddy.domain_framework.client.entity.DomainClashRenderer;
import muddy.domain_framework.client.entity.DomainRenderer;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.client.utils.DomainCenterPosition;
import muddy.domain_framework.util.HasDomainExpanded;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.CoreShaderRegistrationCallback;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.ladysnake.satin.api.event.PostWorldRenderCallbackV3;

import static muddy.domain_framework.MuddysDomainFramework.MOD_ID;

public class MuddysDomainFrameworkClient implements ClientModInitializer {
    @Nullable
    public static ShaderInstance DOMAIN_SHADER;



    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.DOMAIN_ENTITY, DomainRenderer::new);
        EntityRendererRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashRenderer::new);

//        RenderLayerHelper.registerBlockRenderLayer(ModRenderTypes.insideDomain());
//        BlockRenderLayerMap.INSTANCE.putBlock(ModBlocks.DOMAIN_BARRIER_BLOCK, ModRenderTypes.insideDomain());

        ClientPlayNetworking.registerGlobalReceiver(DomainHasExpandedS2CPayload.ID, (payload, context) -> {
            ClientLevel level = context.client().level;
            if (level == null) {
                return;
            }

            ((HasDomainExpanded) context.player()).domain$setHasDomainExpanded(payload.hasFullyExpanded());
        });

        CoreShaderRegistrationCallback.EVENT.register(context -> {
            context.register(
                    ResourceLocation.fromNamespaceAndPath(
                            MOD_ID,
                            "domain_default"
                    ),
                    DefaultVertexFormat.BLOCK,
                    shader -> DOMAIN_SHADER = shader
            );
        });

        //Look into how sodium avoids conflicts with other client-side mods.

        PostWorldRenderCallbackV3.EVENT.register((poseStack, projectionMat, modelViewMat, camera, tickDelta) -> {

        });

        ClientTickEvents.END_CLIENT_TICK.register(listener -> {
            LocalPlayer player = listener.player;

            if (player != null) {
                BlockPos centerOfDomain = ((DomainCenterPosition) player).domain$getDomainCenter();

                assert DOMAIN_SHADER != null;
                Uniform domainCenterUniform = DOMAIN_SHADER.getUniform("DomainCenterPosition");

                if (domainCenterUniform != null) {
                    if (centerOfDomain != null) {
                        Vec3 domainVectorisedCenter = (centerOfDomain.getCenter().subtract(player.position()));

                        domainCenterUniform.set(
                                (float) domainVectorisedCenter.x(),
                                (float) domainVectorisedCenter.y(),
                                (float) domainVectorisedCenter.z()
                        );
                    }
                    else {
                        domainCenterUniform.set(0.0F, 0.0F, 0.0F);
                    }
                }
            }
        });

    }
}