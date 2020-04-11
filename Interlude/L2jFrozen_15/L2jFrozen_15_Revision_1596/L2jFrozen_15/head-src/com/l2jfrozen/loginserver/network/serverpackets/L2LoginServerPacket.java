package com.l2jfrozen.loginserver.network.serverpackets;

import com.l2jfrozen.loginserver.L2LoginClient;
import com.l2jfrozen.netcore.SendablePacket;

/**
 * @author programmos
 */
public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
	public abstract String getType();
}
