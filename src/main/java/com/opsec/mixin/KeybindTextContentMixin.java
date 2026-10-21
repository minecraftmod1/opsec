package com.opsec.mixin;

import com.opsec.OpsecConfig;
import net.minecraft.text.KeybindTextContent;
import net.minecraft.text.StringVisitable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(KeybindTextContent.class)
public abstract class KeybindTextContentMixin {
    @Shadow @Final private String key;

    @Inject(method = "visit(Lnet/minecraft/text/StringVisitable$Visitor;)Ljava/util/Optional;",
            at = @At("HEAD"), cancellable = true)
    private <T> void opsec$hide(StringVisitable.Visitor<T> visitor, CallbackInfoReturnable<Optional<T>> cir) {
        OpsecConfig c = OpsecConfig.get();
        if (c.hideModKeybinds && c.shouldHideKey(this.key)) {
            // an unmodded client shows the raw id for a keybind it doesn't know
            cir.setReturnValue(visitor.accept(this.key));
        }
    }
}
