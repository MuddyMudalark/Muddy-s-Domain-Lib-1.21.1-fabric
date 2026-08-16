package muddy.domain_framework.mixin;

import muddy.domain_framework.network.DomainHasExpandedS2CPayload;
import muddy.domain_framework.network.UpdateClientClashScoreS2CPayload;
import muddy.domain_framework.util.ClashScoreAccessor;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements ClashScoreAccessor {
    @Unique
    public int clashScore = 0;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void domain$addSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("DomainClashScore", clashScore);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void domain$readAddedSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        domain$setClashScore(compoundTag.getInt("DomainClashScore"));
        ServerPlayer player = ((ServerPlayer)(Object)this);
        if (player.connection != null) {
            UpdateClientClashScoreS2CPayload payload = new UpdateClientClashScoreS2CPayload(compoundTag.getInt("DomainClashScore"));

            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public int domain$getClashScore() {
        return clashScore;
    }

    @Override
    public void domain$incrementClashScore() {
        Player thisPlayer = ((Player)(Object)this);
        thisPlayer.displayClientMessage(Component.literal("Your Dominance is at: ".concat((clashScore)+"")), true);

        clashScore++;
    }

    @Override
    public void domain$setClashScore(int clashScore) {
        this.clashScore = clashScore;
    }
}
