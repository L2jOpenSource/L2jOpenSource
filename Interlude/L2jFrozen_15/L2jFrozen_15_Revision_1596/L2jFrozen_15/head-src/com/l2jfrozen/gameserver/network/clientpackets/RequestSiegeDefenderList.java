package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.managers.FortManager;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.model.entity.siege.Fort;
import com.l2jfrozen.gameserver.network.serverpackets.FortSiegeDefenderList;
import com.l2jfrozen.gameserver.network.serverpackets.SiegeDefenderList;

/**
 * @author programmos
 */
public final class RequestSiegeDefenderList extends L2GameClientPacket
{
	private int castleId;
	
	@Override
	protected void readImpl()
	{
		castleId = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (castleId < 100)
		{
			final Castle castle = CastleManager.getInstance().getCastleById(castleId);
			
			if (castle == null)
			{
				return;
			}
			
			final SiegeDefenderList sdl = new SiegeDefenderList(castle);
			sendPacket(sdl);
		}
		else
		{
			final Fort fort = FortManager.getInstance().getFortById(castleId);
			
			if (fort == null)
			{
				return;
			}
			
			final FortSiegeDefenderList sdl = new FortSiegeDefenderList(fort);
			sendPacket(sdl);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] a3 RequestSiegeDefenderList";
	}
}
