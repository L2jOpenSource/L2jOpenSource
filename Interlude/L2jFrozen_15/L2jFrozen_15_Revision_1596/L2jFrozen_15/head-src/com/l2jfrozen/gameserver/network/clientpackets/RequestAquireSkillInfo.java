package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillSpellbookTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.model.L2PledgeSkillLearn;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.serverpackets.AquireSkillInfo;

public class RequestAquireSkillInfo extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAquireSkillInfo.class);
	
	private int id;
	private int level;
	private int skillType;
	
	@Override
	protected void readImpl()
	{
		id = readD();
		level = readD();
		skillType = readD();
	}
	
	@Override
	protected void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		final L2FolkInstance trainer = activeChar.getLastFolkNPC();
		if (trainer == null)
		{
			return;
		}
		
		if (!activeChar.isGM() && !activeChar.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false))
		{
			return;
		}
		
		boolean canteach = false;
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		if (skill == null)
		{
			if (Config.DEBUG)
			{
				LOGGER.warn("skill id " + id + " level " + level + " is undefined. aquireSkillInfo failed.");
			}
			return;
		}
		
		if (skillType == 0)
		{
			if (!trainer.getTemplate().canTeach(activeChar.getSkillLearningClassId()))
			{
				return; // cheater
			}
			
			final L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(activeChar, activeChar.getSkillLearningClassId());
			
			for (final L2SkillLearn s : skills)
			{
				if (s.getId() == id && s.getLevel() == level)
				{
					canteach = true;
					break;
				}
			}
			
			if (!canteach)
			{
				return; // cheater
			}
			
			final int requiredSp = SkillTreeTable.getInstance().getSkillCost(activeChar, skill);
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredSp, 0);
			
			int spbId = -1;
			if (Config.DIVINE_SP_BOOK_NEEDED && skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
			{
				spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, level);
			}
			else if (Config.SP_BOOK_NEEDED && skill.getLevel() == 1)
			{
				spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
			}
			
			if (spbId > -1)
			{
				asi.addRequirement(99, spbId, 1, 50);
			}
			
			sendPacket(asi);
		}
		else if (skillType == 2)
		{
			int requiredRep = 0;
			int itemId = 0;
			final L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(activeChar);
			
			for (final L2PledgeSkillLearn s : skills)
			{
				if (s.getId() == id && s.getLevel() == level)
				{
					canteach = true;
					requiredRep = s.getRepCost();
					itemId = s.getItemId();
					break;
				}
			}
			
			if (!canteach)
			{
				return; // cheater
			}
			
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), requiredRep, 2);
			
			if (Config.LIFE_CRYSTAL_NEEDED)
			{
				asi.addRequirement(1, itemId, 1, 0);
			}
			
			sendPacket(asi);
		}
		else
		// Common Skills
		{
			int costid = 0;
			int costcount = 0;
			int spcost = 0;
			
			final L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(activeChar);
			
			for (final L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
				{
					continue;
				}
				
				canteach = true;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				spcost = s.getSpCost();
			}
			
			final AquireSkillInfo asi = new AquireSkillInfo(skill.getId(), skill.getLevel(), spcost, 1);
			asi.addRequirement(4, costid, costcount, 0);
			sendPacket(asi);
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] 6B RequestAquireSkillInfo";
	}
}
