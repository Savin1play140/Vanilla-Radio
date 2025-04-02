package com.damir00109;

import de.maxhenkel.voicechat.api.*;
import de.maxhenkel.voicechat.api.audiochannel.LocationalAudioChannel;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class VanillaDamir00109 implements ModInitializer, VoicechatPlugin {
	public static final String MOD_ID = "vpl";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final LocationalAudioChannel[] channels = new LocationalAudioChannel[14];
	private static VoicechatServerApi vc_api;

	@Override
	public void onInitialize() {
		DModItems.registerModItems();   // Регистрация предметов
		DModBlocks.registerModBlocks(); // Регистрация блоков
	}

	public static LocationalAudioChannel createChannelBy(int id, Vec3d pos, World level) {
		VoicechatServerApi api = VanillaDamir00109.vc_api;
		LocationalAudioChannel lac = api.createLocationalAudioChannel(UUID.randomUUID(), api.fromServerLevel(level), api.createPosition(pos.x, pos.y, pos.z));
		VanillaDamir00109.channels[id] = lac;
		return lac;
	}

	public static LocationalAudioChannel[] getAllChannels() {
		return VanillaDamir00109.channels;
	}
	public static LocationalAudioChannel getChannelByNum(int num) {
		LocationalAudioChannel channel = VanillaDamir00109.channels[num];
		return channel;
	}


	@Override
	public String getPluginId() {
		return "Vanilla+ Radio";
	}

	@Override
	public void initialize(VoicechatApi api) {
		VanillaDamir00109.LOGGER.info("VoiceChat initialized");
	}

	@Override
	public void registerEvents(EventRegistration registration) {
		registration.registerEvent(VoicechatServerStartedEvent.class, this::onServerStarted);
		registration.registerEvent(MicrophonePacketEvent.class, this::onMicPacket);
		VanillaDamir00109.LOGGER.info("VoiceChat register events");
	}
	private void onServerStarted(VoicechatServerStartedEvent event) {
		VanillaDamir00109.vc_api = event.getVoicechat();
	}
	private void onMicPacket(MicrophonePacketEvent event) {
		VoicechatConnection sender = event.getSenderConnection();
		assert sender != null;
		Object player = sender.getPlayer().getPlayer();

		VanillaDamir00109.LOGGER.info("Microphone! "+player.toString());
	}
}
