package com.l2jfrozen.gameserver.handler.itemhandlers;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.handler.IItemHandler;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.EtcStatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.skills.effects.EffectCharge;
import com.l2jfrozen.gameserver.skills.l2skills.L2SkillCharge;

public class EnergyStone implements IItemHandler
{
	
	public EnergyStone()
	{
	}
	
	@Override
	public void useItem(final L2PlayableInstance playable, final L2ItemInstance item)
	{
		L2PcInstance activeChar;
		if (playable instanceof L2PcInstance)
		{
			activeChar = (L2PcInstance) playable;
		}
		else if (playable instanceof L2PetInstance)
		{
			activeChar = ((L2PetInstance) playable).getOwner();
		}
		else
		{
			return;
		}
		if (item.getItemId() != 5589)
		{
			return;
		}
		if (activeChar.isAllSkillsDisabled())
		{
			activeChar.sendPacket(ActionFailed.STATIC_PACKET);
			return;
		}
		if (activeChar.isSitting())
		{
			activeChar.sendPacket(new SystemMessage(SystemMessageId.CANT_MOVE_SITTING));
			return;
		}
		skill = getChargeSkill(activeChar);
		if (skill == null)
		{
			final SystemMessage sm = new SystemMessage(SystemMessageId.S1_CANNOT_BE_USED);
			sm.addItemName(5589);
			activeChar.sendPacket(sm);
			return;
		}
		
		final SystemMessage sm1 = new SystemMessage(SystemMessageId.USE_S1_);
		sm1.addItemName(5589);
		activeChar.sendPacket(sm1);
		
		effect = activeChar.getChargeEffect();
		if (effect == null)
		{
			final L2Skill dummy = SkillTable.getInstance().getInfo(skill.getId(), skill.getLevel());
			if (dummy != null)
			{
				dummy.getEffects(activeChar, activeChar);
				final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, skill.getId(), 1, 1, 0);
				activeChar.sendPacket(MSU);
				activeChar.destroyItemWithoutTrace("Consume", item.getObjectId(), 1, null, false);
			}
			return;
		}
		
		if (effect.numCharges < 2)
		{
			effect.addNumCharges(1);
			final SystemMessage sm = new SystemMessage(SystemMessageId.FORCE_INCREASED_TO_S1);
			sm.addNumber(effect.getLevel());
			activeChar.sendPacket(sm);
		}
		else
		{
			if (effect.numCharges == 2)
			{
				activeChar.sendPacket(new SystemMessage(SystemMessageId.FORCE_MAXLEVEL_REACHED));
			}
		}
		
		final MagicSkillUser MSU = new MagicSkillUser(playable, activeChar, skill.getId(), 1, 1, 0);
		activeChar.sendPacket(MSU);
		activeChar.broadcastPacket(MSU);
		activeChar.sendPacket(new EtcStatusUpdate(activeChar));
		activeChar.destroyItem("Consume", item.getObjectId(), 1, null, false);
	}
	
	private L2SkillCharge getChargeSkill(final L2PcInstance activeChar)
	{
		final L2Skill skills[] = activeChar.getAllSkills();
		final L2Skill arr$[] = skills;
		for (final L2Skill s : arr$)
		{
			if (s.getId() == 50 || s.getId() == 8)
			{
				return (L2SkillCharge) s;
			}
		}
		
		return null;
	}
	
	@Override
	public int[] getItemIds()
	{
		return ITEM_IDS;
	}
	
	private static final int ITEM_IDS[] =
	{
		5589
	};
	private EffectCharge effect;
	private L2SkillCharge skill;
	
}