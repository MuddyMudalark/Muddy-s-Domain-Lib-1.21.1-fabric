package muddy.domain_framework.mixin;

import muddy.domain_framework.util.ClashScoreAccessor;
import muddy.domain_framework.util.Domain;
import muddy.domain_framework.util.DomainCenterPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerMixin implements ClashScoreAccessor, DomainCenterPosition {
    @Unique
    public int clashScore = 0;
    @Unique
    public Domain domain = new Domain(BlockPos.ZERO, 0);
    @Unique
    public boolean shouldRenderDomainInside;

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void domain$addSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        compoundTag.putInt("DomainClashScore", clashScore);
        compoundTag.put("Domain", Domain.CODEC.encodeStart(NbtOps.INSTANCE, domain).getOrThrow());
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void domain$readAddedSaveData(CompoundTag compoundTag, CallbackInfo ci) {
        domain$setClashScore(compoundTag.getInt("DomainClashScore"));

        domain = Domain.CODEC.parse(NbtOps.INSTANCE, compoundTag.get("CenterOfDomain"))
                .resultOrPartial()
                .orElse(new Domain(BlockPos.ZERO, 0));
    }

    @Override
    public int domain$getClashScore() {
        return clashScore;
    }

    @Override
    public void domain$incrementClashScore() {
        Player thisPlayer = ((Player)(Object)this);
        thisPlayer.displayClientMessage(Component.literal("Your Dominance is at: ".concat((clashScore*10+10)+"%")), true);

        clashScore++;
    }

    @Override
    public void domain$setClashScore(int clashScore) {
        this.clashScore = clashScore;
    }

    @Override
    public Domain domain$getDomain() {
        return domain;
    }

    @Override
    public boolean domain$shouldRenderInternalDomain() {
        return domain.getCenterAsVector().distanceTo(((Player)(Object)this).position()) <= domain.radius;
    }

    @Override
    public void domain$setDomain(Domain domain) {
        this.domain = domain;
    }
}
