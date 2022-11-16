package io.github.thatrobin.skillful.mixin;

import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.ActionOnBlockUsePower;
import io.github.apace100.apoli.power.PowerType;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import io.github.thatrobin.skillful.Skillful;
import io.github.thatrobin.skillful.networking.SkillTabModPackets;
import io.github.thatrobin.skillful.skill_trees.Skill;
import io.github.thatrobin.skillful.skill_trees.SkillPowerRegistry;
import io.github.thatrobin.skillful.skill_trees.SkillTreeRegistry;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.SculkCatalystBlock;
import net.minecraft.block.entity.SculkCatalystBlockEntity;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
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
        SkillTreeRegistry.entries().forEach(identifierTaskEntry -> {
            map.put(identifierTaskEntry.getKey(), identifierTaskEntry.getValue());
        });
        skillData.writeMap(map, PacketByteBuf::writeIdentifier, ((packetByteBuf, task) -> task.toPacket(packetByteBuf)));
        ServerPlayNetworking.send(player, SkillTabModPackets.SKILL_DATA, skillData);

        PowerHolderComponent component = PowerHolderComponent.KEY.get(player);
        SkillPowerRegistry.entries().forEach(identifierPowerTypeEntry -> {
            PowerType<?> power = identifierPowerTypeEntry.getValue();
            if(!component.hasPower(power, identifierPowerTypeEntry.getKey())) {
                component.addPower(power, identifierPowerTypeEntry.getKey());
            }
        });
        component.sync();
    }
    
}
