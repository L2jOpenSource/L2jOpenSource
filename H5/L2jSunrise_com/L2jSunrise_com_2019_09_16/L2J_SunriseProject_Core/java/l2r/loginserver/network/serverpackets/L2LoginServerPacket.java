package l2r.loginserver.network.serverpackets;

import l2r.loginserver.network.L2LoginClient;

import com.l2jserver.mmocore.SendablePacket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class L2LoginServerPacket extends SendablePacket<L2LoginClient>
{
	private static final Logger _log = LoggerFactory.getLogger(L2LoginServerPacket.class);
	
	@Override
	public final void write()
	{
		try
		{
			writeImpl();
			return;
		}
		catch (Exception e)
		{
			_log.error("Client: " + getClient() + " - Failed writing: " + getClass().getSimpleName() + "!", e);
		}
	}
	
	protected abstract void writeImpl();
}
