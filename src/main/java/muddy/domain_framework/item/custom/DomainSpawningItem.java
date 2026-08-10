package muddy.domain_framework.item.custom;

import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.entity.custom.DomainEntity;
import muddy.domain_framework.util.HasDomainExpanded;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class DomainSpawningItem extends Item {
    private final Holder<MobEffect> domainAppliedEffect;
    private int domainRadius = 18;
    private int domainEffectLength = 40;
    private String domainShaderName;

    private DomainEntity domain = null;

    public int getDomainEffectLength() {
        return domainEffectLength;
    }

    public void setDomainEffectLength(int domainEffectLength) {
        this.domainEffectLength = domainEffectLength;
    }

    public int getDomainRadius() {
        return domainRadius;
    }

    public void setDomainRadius(int domainRadius) {
        this.domainRadius = domainRadius;
    }

    public DomainSpawningItem(Properties properties, Holder<MobEffect> domainEffect, String domainShaderName) {
        super(properties);

        this.domainAppliedEffect = domainEffect;
        this.domainShaderName = domainShaderName;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int i, boolean bl) {
        if(entity instanceof Player player) {
            if (((HasDomainExpanded)player).domain$hasDomainExpanded() && !(player.isCreative() || player.isSpectator())) {
                if (domain != null && domain.getOwner().equals(player)) {
                    player.getCooldowns().addCooldown(this, DomainEntity.DEFAULT_LIFETIME + 1000);
                }
            }
        }

        super.inventoryTick(itemStack, level, entity, i, bl);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        domain = new DomainEntity(ModEntities.DOMAIN_ENTITY, level);
        domain.of(domainAppliedEffect, domainEffectLength, player.position(), player, domainRadius);
        domain.setDomainRadius(domainRadius);
        domain.setShaderName(domainShaderName);

        player.setDeltaMovement(Vec3.ZERO);

        if (!player.isCreative()) {
            player.getCooldowns().addCooldown(this, domain.getLifetime() + 300);
        }

        level.addFreshEntity(domain);

        return super.use(level, player, interactionHand);
    }
}
