package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExBrProductList;

/**
 * Created by GodFather
 */
public class RequestBrProductList extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0:66 RequestBrProductList";
	
	@Override
	protected void readImpl()
	{
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		player.sendPacket(new ExBrProductList());
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}