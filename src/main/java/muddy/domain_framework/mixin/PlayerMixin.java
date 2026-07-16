package muddy.domain_framework.mixin;

import muddy.domain_framework.util.ClashScoreAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements ClashScoreAccessor {
    @Unique
    public int clashScore = 0;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void domain$playerSaveClashScore(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("DomainClashScore", clashScore);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void domain$playerClashRetrieveScore(CompoundTag compoundTag, CallbackInfo ci) {
        domain$setClashScore(compoundTag.getInt("DomainClashScore"));
    }


    @Override
    public int domain$getClashScore() {
        return clashScore;
    }

    @Override
    public void domain$incrementClashScore() {
        Player thisPlayer = ((Player)(Object)this);

        thisPlayer.displayClientMessage(Component.literal("Your Dominance is at: ".concat((domain$getClashScore()/10)*100+"%")), true);

        clashScore++;
    }

    @Override
    public void domain$setClashScore(int clashScore) {
        this.clashScore = clashScore;
    }
}
