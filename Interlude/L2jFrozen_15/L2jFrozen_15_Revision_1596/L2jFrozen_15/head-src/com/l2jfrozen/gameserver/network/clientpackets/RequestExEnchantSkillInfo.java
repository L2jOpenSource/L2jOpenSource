package com.l2jfrozen.gameserver.network.clientpackets;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.model.L2EnchantSkillLearn;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.ExEnchantSkillInfo;

/**
 * Format chdd c: (id) 0xD0 h: (subid) 0x06 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillInfo extends L2GameClientPacket
{
	private int skillId;
	private int skillLvl;
	
	@Override
	protected void readImpl()
	{
		skillId = readD();
		skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if (skillId <= 0 || skillLvl <= 0)
		{
			return;
		}
		
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (activeChar.getLevel() < 76)
		{
			return;
		}
		
		final L2FolkInstance trainer = activeChar.getLastFolkNPC();
		if (trainer == null)
		{
			return;
		}
		
		if (!activeChar.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false) && !activeChar.isGM())
		{
			return;
		}
		
		boolean canteach = false;
		
		final L2Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
		if (skill == null || skill.getId() != skillId)
		{
			return;
		}
		
		if (!trainer.getTemplate().canTeach(activeChar.getClassId()))
		{
			return; // cheater
		}
		
		final L2EnchantSkillLearn[] skills = SkillTreeTable.getInstance().getAvailableEnchantSkills(activeChar);
		
		for (final L2EnchantSkillLearn s : skills)
		{
			if (s.getId() == skillId && s.getLevel() == skillLvl)
			{
				canteach = true;
				break;
			}
		}
		
		if (!canteach)
		{
			return; // cheater
		}
		
		final int requiredSp = SkillTreeTable.getInstance().getSkillSpCost(activeChar, skill);
		final int requiredExp = SkillTreeTable.getInstance().getSkillExpCost(activeChar, skill);
		final byte rate = SkillTreeTable.getInstance().getSkillRate(activeChar, skill);
		final ExEnchantSkillInfo asi = new ExEnchantSkillInfo(skill.getId(), skill.getLevel(), requiredSp, requiredExp, rate);
		
		if (Config.ES_SP_BOOK_NEEDED && (skill.getLevel() == 101 || skill.getLevel() == 141)) // only first lvl requires book
		{
			final int spbId = 6622;
			asi.addRequirement(4, spbId, 1, 0);
		}
		sendPacket(asi);
		
	}
	
	@Override
	public String getType()
	{
		return "[C] D0:06 RequestExEnchantSkillInfo";
	}
	
}
