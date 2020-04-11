package com.l2jfrozen.gameserver.model.actor.instance;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.entity.sevensigns.SevenSignsFestival;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;

/**
 * L2FestivalMonsterInstance This class manages all attackable festival NPCs, spawned during the Festival of Darkness.
 * @author Tempy
 */
public class L2FestivalMonsterInstance extends L2MonsterInstance
{
	protected int bonusMultiplier = 1;
	
	/**
	 * Constructor of L2FestivalMonsterInstance (use L2Character and L2NpcInstance constructor).<BR>
	 * <BR>
	 * <B><U> Actions</U> :</B><BR>
	 * <BR>
	 * <li>Call the L2Character constructor to set the template of the L2FestivalMonsterInstance (copy skills from template to object and link calculators to NPC_STD_CALCULATOR)</li>
	 * <li>Set the name of the L2MonsterInstance</li>
	 * <li>Create a RandomAnimation Task that will be launched after the calculated delay if the server allow it</li><BR>
	 * <BR>
	 * @param objectId Identifier of the object to initialized
	 * @param template the template
	 */
	public L2FestivalMonsterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	/**
	 * Sets the offering bonus.
	 * @param bonusMultiplier the new offering bonus
	 */
	public void setOfferingBonus(final int bonusMultiplier)
	{
		this.bonusMultiplier = bonusMultiplier;
	}
	
	/**
	 * Return True if the attacker is not another L2FestivalMonsterInstance.<BR>
	 * <BR>
	 * @param  attacker the attacker
	 * @return          true, if is auto attackable
	 */
	@Override
	public boolean isAutoAttackable(final L2Character attacker)
	{
		if (attacker instanceof L2FestivalMonsterInstance)
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * All mobs in the festival are aggressive, and have high aggro range.
	 * @return true, if is aggressive
	 */
	@Override
	public boolean isAggressive()
	{
		return true;
	}
	
	/**
	 * All mobs in the festival really don't need random animation.
	 * @return true, if successful
	 */
	@Override
	public boolean hasRandomAnimation()
	{
		return false;
	}
	
	/**
	 * Actions:
	 * <li>Check if the killing object is a player, and then find the party they belong to.</li>
	 * <li>Add a blood offering item to the leader of the party.</li>
	 * <li>Update the party leader's inventory to show the new item addition.</li>
	 * @param lastAttacker the last attacker
	 */
	@Override
	public void doItemDrop(final L2Character lastAttacker)
	{
		L2PcInstance killingChar = null;
		
		if (!(lastAttacker instanceof L2PcInstance))
		{
			return;
		}
		
		killingChar = (L2PcInstance) lastAttacker;
		L2Party associatedParty = killingChar.getParty();
		
		killingChar = null;
		
		if (associatedParty == null)
		{
			return;
		}
		
		final L2PcInstance partyLeader = associatedParty.getPartyMembers().get(0);
		L2ItemInstance addedOfferings = partyLeader.getInventory().addItem("Sign", SevenSignsFestival.FESTIVAL_OFFERING_ID, bonusMultiplier, partyLeader, this);
		
		associatedParty = null;
		
		InventoryUpdate iu = new InventoryUpdate();
		
		if (addedOfferings.getCount() != bonusMultiplier)
		{
			iu.addModifiedItem(addedOfferings);
		}
		else
		{
			iu.addNewItem(addedOfferings);
		}
		
		addedOfferings = null;
		
		partyLeader.sendPacket(iu);
		iu = null;
		
		super.doItemDrop(lastAttacker); // Normal drop
	}
}
