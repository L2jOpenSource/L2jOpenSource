package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.managers.CastleManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.entity.siege.Castle;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;

/**
 * This class ...
 * @version $Revision: 1.1.2.2.2.7 $ $Date: 2005/04/05 19:41:13 $
 */

public class ScrollOfResurrection implements IItemHandler
{
	// all the items ids that this handler knows
	private static final int[] ITEM_IDS =
	{
		737,
		3936,
		3959,
		6387
	};
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		if (!(playable instanceof L2PcInstance))
		{
			return;
		}
		
		L2PcInstance activeChar = (L2PcInstance) playable;
		
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		
		if (activeChar.isInOlympiadMode())
		{
			activeChar.sendMessage("This Item Cannot Be Used On Olympiad Games.");
		}
		
		if (activeChar.isMovementDisabled())
		{
			return;
		}
		
		final int itemId = item.getItemId();
		// boolean blessedScroll = (itemId != 737);
		final boolean humanScroll = itemId == 3936 || itemId == 3959 || itemId == 737;
		final boolean petScroll = itemId == 6387 || itemId == 737;
		
		// SoR Animation section
		L2Character target = (L2Character) activeChar.getTarget();
		
		if (target != null && target.isDead())
		{
			L2PcInstance targetPlayer = null;
			if (target instanceof L2PcInstance)
			{
				targetPlayer = (L2PcInstance) target;
			}
			
			L2PetInstance targetPet = null;
			if (target instanceof L2PetInstance)
			{
				targetPet = (L2PetInstance) target;
			}
			
			target = null;
			
			if (targetPlayer != null || targetPet != null)
			{
				boolean condGood = true;
				
				// check target is not in a active siege zone
				Castle castle = null;
				
				if (targetPlayer != null)
				{
					castle = CastleManager.getInstance().getCastle(targetPlayer.getX(), targetPlayer.getY(), targetPlayer.getZ());
				}
				else if (targetPet != null)
				{
					castle = CastleManager.getInstance().getCastle(targetPet.getX(), targetPet.getY(), targetPet.getZ());
				}
				
				if (castle != null && castle.getSiege().getIsInProgress())
				{
					condGood = false;
					activeChar.sendPacket(new SystemMessage(SystemMessageId.CANNOT_BE_RESURRECTED_DURING_SIEGE));
				}
				
				castle = null;
				
				if (targetPet != null)
				{
					if (targetPet.getOwner() != activeChar)
					{
						if (targetPet.getOwner().isReviveRequested())
						{
							if (targetPet.getOwner().isRevivingPet())
							{
								activeChar.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
							}
							else
							{
								activeChar.sendPacket(new SystemMessage(SystemMessageId.PET_CANNOT_RES)); // A pet cannot be resurrected while it's owner is in the process of resurrecting.
							}
							condGood = false;
						}
					}
					else if (!petScroll)
					{
						condGood = false;
						activeChar.sendMessage("You do not have the correct scroll");
					}
				}
				else if (targetPlayer != null)
				{
					if (targetPlayer.isFestivalParticipant()) // Check to see if the current player target is in a festival.
					{
						condGood = false;
						activeChar.sendPacket(SystemMessage.sendString("You may not resurrect participants in a festival."));
					}
					if (targetPlayer.isReviveRequested())
					{
						if (targetPlayer.isRevivingPet())
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.MASTER_CANNOT_RES)); // While a pet is attempting to resurrect, it cannot help in resurrecting its master.
						}
						else
						{
							activeChar.sendPacket(new SystemMessage(SystemMessageId.RES_HAS_ALREADY_BEEN_PROPOSED)); // Resurrection is already been proposed.
						}
						condGood = false;
					}
					else if (!humanScroll)
					{
						condGood = false;
						activeChar.sendMessage("You do not have the correct scroll");
					}
				}
				
				if (condGood)
				{
					int skillId = 0;
					final int skillLevel = 1;
					
					switch (itemId)
					{
						case 737:
							skillId = 2014;
							break; // Scroll of Resurrection
						case 3936:
							skillId = 2049;
							break; // Blessed Scroll of Resurrection
						case 3959:
							skillId = 2062;
							break; // L2Day - Blessed Scroll of Resurrection
						case 6387:
							skillId = 2179;
							break; // Blessed Scroll of Resurrection: For Pets
					}
					
					if (skillId != 0)
					{
						final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
						activeChar.useMagic(skill, true, true);
						
						// Consume the scroll
						if (!activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false))
						{
							return;
						}
						
						final SystemMessage sm = new SystemMessage(SystemMessageId.S1_DISAPPEARED);
						sm.addItemName(itemId);
						activeChar.sendPacket(sm);
					}
				}
			}
		}
		else
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.TARGET_IS_INCORRECT));
		}
		
		activeChar = null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
}