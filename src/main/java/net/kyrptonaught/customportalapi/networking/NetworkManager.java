package net.kyrptonaught.customportalapi.networking;

import java.util.function.BiConsumer;

import com.google.common.collect.ImmutableList;

import net.kyrptonaught.customportalapi.CustomPortalsMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class NetworkManager {
	private static final String PROTOCOL_VERSION = "1.6";
	public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(CustomPortalsMod.MOD_ID, "main"), () -> PROTOCOL_VERSION, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);
	private static int nextId = 0;

	public static void register() {
		ImmutableList.<BiConsumer<SimpleChannel, Integer>>builder().add(ForcePlacePortalPacket::register).add(PortalRegistrySyncPacket::register).build().forEach(consumer -> consumer.accept(INSTANCE, getNextId()));
	}

	private static int getNextId() {
		return nextId++;
	}

}