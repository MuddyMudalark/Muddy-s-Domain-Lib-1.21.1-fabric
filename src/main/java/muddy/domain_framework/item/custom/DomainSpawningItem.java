package muddy.domain_framework.item.custom;

import muddy.domain_framework.entity.ModEntities;
import muddy.domain_framework.entity.custom.DomainEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
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

    public DomainSpawningItem(Properties properties, Holder<MobEffect> domainEffect) {
        super(properties);

        this.domainAppliedEffect = domainEffect;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        DomainEntity domain = new DomainEntity(ModEntities.DOMAIN_ENTITY, level);
        domain.setDomainEffect(this.domainAppliedEffect);
        domain.setPos(player.position());
        domain.setOwner(player);
        domain.setDomainRadius(domainRadius);
        domain.setDomainEffectLength(domainEffectLength);

        player.setDeltaMovement(Vec3.ZERO);
        player.getCooldowns().addCooldown(this, domain.getLifetime() + 300);

        level.addFreshEntity(domain);

        return super.use(level, player, interactionHand);
    }
}
