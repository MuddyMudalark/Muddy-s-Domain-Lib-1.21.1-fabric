package muddy.domain_framework.mixin;

import muddy.domain_framework.block.custom.DomainAirBlock;
import muddy.domain_framework.block.custom.DomainClashAirBlock;
import muddy.domain_framework.sounds.ModSounds;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(InstrumentItem.class)
public class AntiGoatTeleportationMixin {
    @Mutable
    @Final
    @Shadow
    private final TagKey<Instrument> instruments;

    public AntiGoatTeleportationMixin(TagKey<Instrument> instruments) {
        this.instruments = instruments;
    }

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void domain$unUseify(Level level, Player player, InteractionHand interactionHand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemStack = player.getItemInHand(interactionHand);

        if (level.getBlockState(player.blockPosition()).getBlock() instanceof DomainAirBlock || level.getBlockState(player.blockPosition()).getBlock() instanceof DomainClashAirBlock) {
            player.playSound(ModSounds.ITEM_NO_HORN, 2f, 1f);

            cir.setReturnValue(InteractionResultHolder.fail(itemStack));
            cir.cancel();
        }
    }

}
