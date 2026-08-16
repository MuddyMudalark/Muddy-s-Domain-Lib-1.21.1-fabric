package muddy.domain_framework;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import muddy.domain_framework.block.ModBlocks;
import muddy.domain_framework.command.ModGameRules;
import muddy.domain_framework.effect.ModEffects;
import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.entity.custom.DomainClashEntity;
import muddy.domain_framework.entity.custom.DomainEntity;
import muddy.domain_framework.item.ModItems;
import muddy.domain_framework.network.ClashWinScoreGameRuleS2CPayload;
import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.network.UpdateClientClashScoreS2CPayload;
import muddy.domain_framework.sounds.ModSounds;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuddysDomainFramework implements ModInitializer {
    public static final String MOD_ID = "muddys-domain-framework";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static int executeDomainSuperCommand(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(() -> Component.literal("Called /command_two."), false);
        return 1;
    }

    private static int executeSetClashLength(CommandContext<CommandSourceStack> context) {
        int value = IntegerArgumentType.getInteger(context, "value");
        context.getSource().sendSuccess(() -> Component.literal("Set the default length of a domain clash to %s ticks".formatted(value)), false);
        return 1;
    }

    @Override
    public void onInitialize() {
        ModEffects.initialize();
        ModSounds.initialize();
        ModEntities.initialize();
        ModItems.initialize();
        ModBlocks.initialize();
        ModGameRules.initialize();

        Registry.register(BuiltInRegistries.SOUND_EVENT, ResourceLocation.fromNamespaceAndPath(MOD_ID, "no_horn"),
                SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(MOD_ID, "no_horn")));

        PayloadTypeRegistry.playS2C().register(DomainHasExpandedS2CPayload.ID, DomainHasExpandedS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClashWinScoreGameRuleS2CPayload.ID, ClashWinScoreGameRuleS2CPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(UpdateClientClashScoreS2CPayload.ID, UpdateClientClashScoreS2CPayload.CODEC);

        FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_ENTITY, DomainEntity.createAttributes());
        FabricDefaultAttributeRegistry.register(ModEntities.DOMAIN_CLASH_ENTITY, DomainClashEntity.createAttributes());

        ServerTickEvents.END_SERVER_TICK.register(listener -> {
            ClashWinScoreGameRuleS2CPayload payload = new ClashWinScoreGameRuleS2CPayload(listener.getGameRules().getInt(ModGameRules.CLASH_WIN_SCORE));

            for (ServerPlayer player : listener.getPlayerList().getPlayers()) {
                ServerPlayNetworking.send(player, payload);
            }
        });

        LOGGER.info("Domain Expansion: Malevolent Codebase");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
