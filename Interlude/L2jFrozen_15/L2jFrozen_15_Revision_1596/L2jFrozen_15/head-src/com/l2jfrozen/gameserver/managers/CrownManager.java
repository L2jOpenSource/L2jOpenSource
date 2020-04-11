package com.l2jfrozen.gameserver.managers;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.datatables.CrownTable;
import com.l2jfrozen.gameserver.model.L2Clan;
import com.l2jfrozen.gameserver.model.L2ClanMember;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;

/**
 * @author evill33t Reworked by NB4L1
 */
public class CrownManager
{
	protected static final Logger LOGGER = Logger.getLogger(CrownManager.class);
	private static CrownManager instance;
	
	public CrownManager()
	{
	}
	
	public static final CrownManager getInstance()
	{
		if (instance == null)
		{
			instance = new CrownManager();
		}
		return instance;
	}
	
	public void checkCrowns(final L2Clan clan)
	{
		if (clan == null)
		{
			return;
		}
		
		for (final L2ClanMember member : clan.getMembers())
		{
			if (member != null && member.isOnline() && member.getPlayerInstance() != null)
			{
				checkCrowns(member.getPlayerInstance());
			}
		}
	}
	
	public void checkCrowns(final L2PcInstance activeChar)
	{
		if (activeChar == null)
		{
			return;
		}
		
		boolean isLeader = false;
		int crownId = -1;
		
		L2Clan activeCharClan = activeChar.getClan();
		L2ClanMember activeCharClanLeader;
		
		if (activeCharClan != null)
		{
			activeCharClanLeader = activeChar.getClan().getLeader();
		}
		else
		{
			activeCharClanLeader = null;
		}
		
		if (activeCharClan != null)
		{
			Castle activeCharCastle = CastleManager.getInstance().getCastleByOwner(activeCharClan);
			
			if (activeCharCastle != null)
			{
				crownId = CrownTable.getCrownId(activeCharCastle.getCastleId());
			}
			
			activeCharCastle = null;
			
			if (activeCharClanLeader != null && activeCharClanLeader.getObjectId() == activeChar.getObjectId())
			{
				isLeader = true;
			}
		}
		
		activeCharClan = null;
		activeCharClanLeader = null;
		
		if (crownId > 0)
		{
			if (isLeader && activeChar.getInventory().getItemByItemId(6841) == null)
			{
				activeChar.addItem("Crown", 6841, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
			
			if (activeChar.getInventory().getItemByItemId(crownId) == null)
			{
				activeChar.addItem("Crown", crownId, 1, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
		
		boolean alreadyFoundCirclet = false;
		boolean alreadyFoundCrown = false;
		
		for (final L2ItemInstance item : activeChar.getInventory().getItems())
		{
			if (CrownTable.getCrownList().contains(item.getItemId()))
			{
				if (crownId > 0)
				{
					if (item.getItemId() == crownId)
					{
						if (!alreadyFoundCirclet)
						{
							alreadyFoundCirclet = true;
							continue;
						}
					}
					else if (item.getItemId() == 6841 && isLeader)
					{
						if (!alreadyFoundCrown)
						{
							alreadyFoundCrown = true;
							continue;
						}
					}
				}
				
				activeChar.destroyItem("Removing Crown", item, activeChar, true);
				activeChar.getInventory().updateDatabase();
			}
		}
	}
}
