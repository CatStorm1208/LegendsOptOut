package de.catstorm.optout.optout.mixin;

import de.catstorm.optout.optout.LegendsOptOut;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Shadow public abstract String getNameForScoreboard();
    @Shadow public abstract void sendMessage(Text message, boolean overlay);

    @Inject(method = "isInvulnerableTo", at = @At("HEAD"), cancellable = true)
    private void isInvulnerableTo(DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource.getSource() != null) {
            for (var player : LegendsOptOut.playersScheduledForRemoval) {
                if (player.playerName.equals(damageSource.getSource().getNameForScoreboard())) {
                    LegendsOptOut.playersScheduledForRemoval.remove(player);
                    damageSource.getSource().sendMessage(Text.of("Your opt-out has been aborted!"));
                }
                else if (player.playerName.equals(getNameForScoreboard())) {
                    LegendsOptOut.playersScheduledForRemoval.remove(player);
                    sendMessage(Text.of("Your opt-out has been aborted!"), false);
                }
            }
            if ((!LegendsOptOut.optedInPlayerNames.contains(getNameForScoreboard()) ||
                !LegendsOptOut.optedInPlayerNames.contains(damageSource.getSource().getNameForScoreboard())) &&
                damageSource.getSource() instanceof PlayerEntity) {
                cir.setReturnValue(true);
            }
        }
    }
}