package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;

/**
 * @author vGodFather
 */
public class NetPing extends L2GameClientPacket
{
	private static final String _C__B1_NETPING = "[C] B1 NetPing";
	
	int playerId;
	int ping;
	int mtu;
	
	@Override
	protected void readImpl()
	{
		playerId = readD();
		ping = readD();
		mtu = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		activeChar.setQuickVar("ping", ping);
		activeChar.setQuickVar("mtu", mtu);
	}
	
	@Override
	public String getType()
	{
		return _C__B1_NETPING;
	}
}