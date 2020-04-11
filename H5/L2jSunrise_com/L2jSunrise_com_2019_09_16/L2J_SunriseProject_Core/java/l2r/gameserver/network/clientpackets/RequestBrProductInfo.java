package l2r.gameserver.network.clientpackets;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.serverpackets.ExBrProductInfo;

/**
 * Created by GodFather
 */
public class RequestBrProductInfo extends L2GameClientPacket
{
	private static final String TYPE = "[C] D0:67 RequestBrProductInfo";
	
	private int _productId;
	
	@Override
	protected void readImpl()
	{
		_productId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		player.sendPacket(new ExBrProductInfo(_productId));
	}
	
	@Override
	public String getType()
	{
		return TYPE;
	}
}