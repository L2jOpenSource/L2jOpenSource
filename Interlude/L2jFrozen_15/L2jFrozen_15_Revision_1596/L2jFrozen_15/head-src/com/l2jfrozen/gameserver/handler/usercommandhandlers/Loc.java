package com.l2jfrozen.gameserver.handler.usercommandhandlers;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable;
import com.l2jfrozen.gameserver.handler.IUserCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 *
 *
 */
public class Loc implements IUserCommandHandler
{
	private static final int[] COMMAND_IDS =
	{
		0
	};
	
	@Override
	public boolean useUserCommand(final int id, final L2PcInstance activeChar)
	{
		final int nearestTown = MapRegionTable.getInstance().getClosestTownNumber(activeChar);
		SystemMessageId msg;
		
		switch (nearestTown)
		{
			case 0:
				msg = SystemMessageId.LOC_TI_S1_S2_S3;
				break;
			case 1:
				msg = SystemMessageId.LOC_ELVEN_S1_S2_S3;
				break;
			case 2:
				msg = SystemMessageId.LOC_DARK_ELVEN_S1_S2_S3;
				break;
			case 3:
				msg = SystemMessageId.LOC_ORC_S1_S2_S3;
				break;
			case 4:
				msg = SystemMessageId.LOC_DWARVEN_S1_S2_S3;
				break;
			case 5:
				msg = SystemMessageId.LOC_GLUDIO_S1_S2_S3;
				break;
			case 6:
				msg = SystemMessageId.LOC_GLUDIN_S1_S2_S3;
				break;
			case 7:
				msg = SystemMessageId.LOC_DION_S1_S2_S3;
				break;
			case 8:
				msg = SystemMessageId.LOC_GIRAN_S1_S2_S3;
				break;
			case 9:
				msg = SystemMessageId.LOC_OREN_S1_S2_S3;
				break;
			case 10:
				msg = SystemMessageId.LOC_ADEN_S1_S2_S3;
				break;
			case 11:
				msg = SystemMessageId.LOC_HUNTER_S1_S2_S3;
				break;
			case 12:
				msg = SystemMessageId.LOC_GIRAN_HARBOR_S1_S2_S3;
				break;
			case 13:
				msg = SystemMessageId.LOC_HEINE_S1_S2_S3;
				break;
			case 14:
				msg = SystemMessageId.LOC_RUNE_S1_S2_S3;
				break;
			case 15:
				msg = SystemMessageId.LOC_GODDARD_S1_S2_S3;
				break;
			case 16:
				msg = SystemMessageId.LOC_SCHUTTGART_S1_S2_S3;
				break;
			case 17:
				msg = SystemMessageId.LOC_FLORAN_S1_S2_S3;
				break;
			case 18:
				msg = SystemMessageId.LOC_PRIMEVAL_ISLE_S1_S2_S3;
				break;
			default:
				msg = SystemMessageId.LOC_ADEN_S1_S2_S3;
		}
		
		SystemMessage sm = new SystemMessage(msg);
		msg = null;
		sm.addNumber(activeChar.getX());
		sm.addNumber(activeChar.getY());
		sm.addNumber(activeChar.getZ());
		activeChar.sendPacket(sm);
		sm = null;
		
		return true;
	}
	
	@Override
	public int[] getUserCommandList()
	{
		return COMMAND_IDS;
	}
}
