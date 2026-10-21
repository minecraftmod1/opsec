package com.opsec.mixin;

import com.opsec.OpsecConfig;
import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverMixin {
    @Inject(method = "getClientModName", at = @At("HEAD"), cancellable = true)
    private static void opsec$brand(CallbackInfoReturnable<String> cir) {
        OpsecConfig c = OpsecConfig.get();
        if (c.spoofBrand) cir.setReturnValue(c.brand);
    }
}
