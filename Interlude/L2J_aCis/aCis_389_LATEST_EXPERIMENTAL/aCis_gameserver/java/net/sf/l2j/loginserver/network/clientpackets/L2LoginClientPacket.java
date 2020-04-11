package net.sf.l2j.loginserver.network.clientpackets;

import net.sf.l2j.commons.logging.CLogger;
import net.sf.l2j.commons.mmocore.ReceivablePacket;

import net.sf.l2j.loginserver.network.LoginClient;

public abstract class L2LoginClientPacket extends ReceivablePacket<LoginClient>
{
	protected static final CLogger LOGGER = new CLogger(L2LoginClientPacket.class.getName());
	
	@Override
	protected final boolean read()
	{
		try
		{
			return readImpl();
		}
		catch (Exception e)
		{
			LOGGER.error("Failed reading {}. ", e, getClass().getSimpleName());
			return false;
		}
	}
	
	protected abstract boolean readImpl();
}
