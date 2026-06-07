package me.aleksilassila.litematica.printer.mixin;

import dev.bilixwhite.litematica.printer.printer.zxy.inventory.InventoryUtils;
import dev.bilixwhite.litematica.printer.printer.zxy.inventory.SwitchItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import dev.bilixwhite.litematica.printer.printer.zxy.utils.ZxyUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static dev.bilixwhite.litematica.printer.printer.zxy.inventory.InventoryUtils.isOpenHandler;
import static dev.bilixwhite.litematica.printer.printer.zxy.inventory.SwitchItem.reSwitchItem;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

    @Inject(at = @At("TAIL"), method = "handleContainerContent")
    public void onInventory(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        if (isOpenHandler) {
            InventoryUtils.switchInv();
        }
        if (reSwitchItem != null) {
            SwitchItem.reSwitchItem();
        }
        if (Minecraft.getInstance().player != null && ZxyUtils.printerMemoryAdding) {
            Minecraft.getInstance().player.closeContainer();
        }
        if (ZxyUtils.num == 1 || ZxyUtils.num == 3) {
            ZxyUtils.syncInv();
        }
    }
}
