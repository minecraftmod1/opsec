package com.opsec.mixin;

import com.opsec.OpsecConfig;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {
    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    private void opsec$filterPayloads(Packet<?> packet, CallbackInfo ci) {
        if (packet instanceof CustomPayloadC2SPacket p
                && OpsecConfig.get().shouldBlockPayload(p.payload().getId().id())) {
            ci.cancel();
        }
    }
}
