package net.sf.l2j.gameserver.network.clientpackets;

import net.sf.l2j.gameserver.network.serverpackets.KeyPacket;
import net.sf.l2j.gameserver.network.serverpackets.L2GameServerPacket;

public final class ProtocolVersion extends L2GameClientPacket
{
	private int _version;
	
	@Override
	protected void readImpl()
	{
		_version = readD();
	}
	
	@Override
	protected void runImpl()
	{
		switch (_version)
		{
			case 737:
			case 740:
			case 744:
			case 746:
				getClient().sendPacket(new KeyPacket(getClient().enableCrypt()));
				break;
			
			default:
				getClient().close((L2GameServerPacket) null);
				break;
		}
	}
}