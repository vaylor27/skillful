package io.github.thatrobin.skillful.mixin;

import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillTreeRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {

    @Inject(at = @At("TAIL"), method = "onPlayerConnect(Lnet/minecraft/network/ClientConnection;Lnet/minecraft/server/network/ServerPlayerEntity;)V")
    private void setTabs(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
        PacketByteBuf skillData = new PacketByteBuf(Unpooled.buffer());
        Map<Identifier, Skill.Task> map = new HashMap<>();
        SkillTreeRegistry.entries().forEach(identifierTaskEntry -> map.put(identifierTaskEntry.getKey(), identifierTaskEntry.getValue()));
        skillData.writeMap(map, PacketByteBuf::writeIdentifier, ((packetByteBuf, task) -> task.toPacket(packetByteBuf)));
        ServerPlayNetworking.send(player, SkillTabModPackets.SKILL_DATA, skillData);
    }
}
