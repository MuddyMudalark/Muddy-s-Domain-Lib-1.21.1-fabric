package muddy.domain_lib.item.custom;

import muddy.domain_lib.entity.ModEntities;
import muddy.domain_lib.entity.custom.DomainEntity;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class DomainSpawningItem extends Item {
    Holder<MobEffect> domainAppliedEffect;

    public DomainSpawningItem(Properties properties, Holder<MobEffect> domainEffect) {
        super(properties);

        this.domainAppliedEffect = domainEffect;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand interactionHand) {
        DomainEntity domain = new DomainEntity(ModEntities.DOMAIN_ENTITY, level);
        domain.setDomainEffect(this.domainAppliedEffect);
        domain.setPos(player.position());
        domain.setDomainRadius(18);

        level.addFreshEntity(domain);

        return super.use(level, player, interactionHand);
    }
}
