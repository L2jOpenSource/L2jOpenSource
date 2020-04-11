/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.loginserver.network;

import java.nio.ByteBuffer;

import l2r.loginserver.network.L2LoginClient.LoginClientState;
import l2r.loginserver.network.clientpackets.AuthGameGuard;
import l2r.loginserver.network.clientpackets.RequestAuthLogin;
import l2r.loginserver.network.clientpackets.RequestServerList;
import l2r.loginserver.network.clientpackets.RequestServerLogin;

import org.mmocore.network.IPacketHandler;
import org.mmocore.network.ReceivablePacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for packets received by Login Server
 * @author KenM
 */
public final class L2LoginPacketHandler implements IPacketHandler<L2LoginClient>
{
	protected static final Logger _log = LoggerFactory.getLogger(L2LoginPacketHandler.class);
	
	@Override
	public ReceivablePacket<L2LoginClient> handlePacket(ByteBuffer buf, L2LoginClient client)
	{
		int opcode = buf.get() & 0xFF;
		
		ReceivablePacket<L2LoginClient> packet = null;
		LoginClientState state = client.getState();
		
		switch (state)
		{
			case CONNECTED:
				switch (opcode)
				{
					case 0x07:
						packet = new AuthGameGuard();
						break;
					default:
						debugOpcode(opcode, state);
						break;
				}
				break;
			case AUTHED_GG:
				switch (opcode)
				{
					case 0x00:
						packet = new RequestAuthLogin();
						break;
					default:
						debugOpcode(opcode, state);
						break;
				}
				break;
			case AUTHED_LOGIN:
				switch (opcode)
				{
					case 0x02:
						packet = new RequestServerLogin();
						break;
					case 0x05:
						packet = new RequestServerList();
						break;
					default:
						debugOpcode(opcode, state);
						break;
				}
				break;
		}
		return packet;
	}
	
	private void debugOpcode(int opcode, LoginClientState state)
	{
		_log.info("Unknown Opcode: " + opcode + " for state: " + state.name());
	}
}
