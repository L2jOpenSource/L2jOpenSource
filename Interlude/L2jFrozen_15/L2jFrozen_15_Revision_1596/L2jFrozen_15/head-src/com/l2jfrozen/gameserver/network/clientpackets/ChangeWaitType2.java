package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Object;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2StaticObjectInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ChairSit;

public final class ChangeWaitType2 extends L2GameClientPacket
{
	private boolean typeStand;
	
	@Override
	protected void readImpl()
	{
		typeStand = readD() == 1;
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		final L2Object target = player.getTarget();
		
		if (getClient() != null)
		{
			if (player.isOutOfControl())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
				return;
			}
			
			if (player.getMountType() != 0)
			{
				return;
			}
			
			if (target != null && !player.isSitting() && target instanceof L2StaticObjectInstance && ((L2StaticObjectInstance) target).getType() == 1 && CastleManager.getInstance().getCastle(target) != null && player.isInsideRadius(target, L2StaticObjectInstance.INTERACTION_DISTANCE, false, false))
			{
				final ChairSit cs = new ChairSit(player, ((L2StaticObjectInstance) target).getStaticObjectId());
				player.sendPacket(cs);
				player.sitDown();
				player.broadcastPacket(cs);
			}
			
			if (typeStand)
			{
				player.standUp();
			}
			else
			{
				player.sitDown();
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 1D ChangeWaitType2";
	}
}