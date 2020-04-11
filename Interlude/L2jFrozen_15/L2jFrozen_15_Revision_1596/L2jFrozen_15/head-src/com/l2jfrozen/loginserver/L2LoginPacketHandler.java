package com.l2jfrozen.loginserver;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.loginserver.L2LoginClient.LoginClientState;
import com.l2jfrozen.loginserver.network.clientpackets.AuthGameGuard;
import com.l2jfrozen.loginserver.network.clientpackets.RequestAuthLogin;
import com.l2jfrozen.loginserver.network.clientpackets.RequestServerList;
import com.l2jfrozen.loginserver.network.clientpackets.RequestServerLogin;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.netcore.IPacketHandler;
import com.l2jfrozen.netcore.ReceivablePacket;

/**
 * Handler for packets received by Login Server
 * @author ProGramMoS
 */

public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	private final Logger LOGGER = Logger.getLogger(L2LoginPacketHandler.class);
	
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(final ByteBuffer buf, final L2LoginClient client)
	{
		final int opcode = buf.get() & 0xFF;
		
		/*
		 * Disabled for now PacketsFloodProtector for now used only on GameServer if (!PacketsFloodProtector.tryPerformAction(opcode, -1, client)) { return null; }
		 */
		
		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();
		
		if (Config.DEBUG_PACKETS)
		{
			Log.add("Packet: " + Integer.toHexString(opcode) + " on State: " + state.name() + " Client: " + client.toString(), "log/packets/", "LoginPacketsLog");
		}
		
		switch (state)
		{
			case CONNECTED:
				if (opcode == 0x07)
				{
					packet = new AuthGameGuard();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_GG:
				if (opcode == 0x00)
				{
					packet = new RequestAuthLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
			case AUTHED_LOGIN:
				if (opcode == 0x05)
				{
					packet = new RequestServerList();
				}
				else if (opcode == 0x02)
				{
					packet = new RequestServerLogin();
				}
				else
				{
					debugOpcode(opcode, state);
				}
				break;
		}
		
		state = null;
		
		return packet;
	}
	
	private void debugOpcode(final int opcode, final LoginClientState state)
	{
		LOGGER.debug("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}