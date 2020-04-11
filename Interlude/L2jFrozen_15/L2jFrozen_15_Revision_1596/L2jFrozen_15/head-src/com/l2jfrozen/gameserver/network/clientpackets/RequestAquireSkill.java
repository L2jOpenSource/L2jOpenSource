package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillSpellbookTable;
import com.l2jfrozen.gameserver.datatables.sql.SkillTreeTable;
import com.l2jfrozen.gameserver.model.L2PledgeSkillLearn;
import com.l2jfrozen.gameserver.model.L2ShortCut;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2SkillLearn;
import com.l2jfrozen.gameserver.model.actor.instance.L2FishermanInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2FolkInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2VillageMasterInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ExStorageMaxCount;
import com.l2jfrozen.gameserver.network.serverpackets.PledgeSkillList;
import com.l2jfrozen.gameserver.network.serverpackets.ShortCutRegister;
import com.l2jfrozen.gameserver.network.serverpackets.StatusUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.util.IllegalPlayerAction;
import com.l2jfrozen.gameserver.util.Util;

public class RequestAquireSkill extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(RequestAquireSkill.class);
	
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
		
		final L2PcInstance player = getClient().getActiveChar();
		
		if (player == null)
		{
			return;
		}
		
		final L2FolkInstance trainer = player.getLastFolkNPC();
		
		if (trainer == null)
		{
			return;
		}
		
		final int npcid = trainer.getNpcId();
		
		if (!player.isInsideRadius(trainer, L2NpcInstance.INTERACTION_DISTANCE, false, false) && !player.isGM())
		{
			return;
		}
		
		if (!Config.ALT_GAME_SKILL_LEARN)
		{
			player.setSkillLearningClassId(player.getClassId());
		}
		
		if (player.getSkillLevel(id) >= level)
		{
			// already knows the skill with this level
			return;
		}
		
		final L2Skill skill = SkillTable.getInstance().getInfo(id, level);
		
		int counts = 0;
		int requiredSp = 10000000;
		
		if (skillType == 0)
		{
			
			final L2SkillLearn[] skills = SkillTreeTable.getInstance().getAvailableSkills(player, player.getSkillLearningClassId());
			
			for (final L2SkillLearn s : skills)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				if (sk == null || sk != skill || !sk.getCanLearn(player.getSkillLearningClassId()) || !sk.canTeachBy(npcid))
				{
					continue;
				}
				counts++;
				requiredSp = SkillTreeTable.getInstance().getSkillCost(player, skill);
			}
			
			if (counts == 0 && !Config.ALT_GAME_SKILL_LEARN)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= requiredSp)
			{
				int spbId = -1;
				// divine inspiration require book for each level
				if (Config.DIVINE_SP_BOOK_NEEDED && skill.getId() == L2Skill.SKILL_DIVINE_INSPIRATION)
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill, level);
				}
				else if (Config.SP_BOOK_NEEDED && skill.getLevel() == 1)
				{
					spbId = SkillSpellbookTable.getInstance().getBookForSkill(skill);
				}
				
				// spellbook required
				if (spbId > -1)
				{
					final L2ItemInstance spb = player.getInventory().getItemByItemId(spbId);
					
					if (spb == null)
					{
						// Haven't spellbook
						player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return;
					}
					
					// ok
					player.destroyItem("Consume", spb, trainer, true);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				
				return;
			}
		}
		else if (skillType == 1)
		{
			int costid = 0;
			int costcount = 0;
			// Skill Learn bug Fix
			final L2SkillLearn[] skillsc = SkillTreeTable.getInstance().getAvailableSkills(player);
			
			for (final L2SkillLearn s : skillsc)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
				{
					continue;
				}
				
				counts++;
				costid = s.getIdCost();
				costcount = s.getCostCount();
				requiredSp = s.getSpCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getSp() >= requiredSp)
			{
				if (!player.destroyItemByItemId("Consume", costid, costcount, trainer, false))
				{
					// Haven't spellbook
					player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
					return;
				}
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
				sm.addNumber(costcount);
				sm.addItemName(costid);
				sendPacket(sm);
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.NOT_ENOUGH_SP_TO_LEARN_SKILL);
				player.sendPacket(sm);
				return;
			}
		}
		else if (skillType == 2)
		{
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_IS_ENABLED);
				return;
			}
			
			int itemId = 0;
			int repCost = 100000000;
			// Skill Learn bug Fix
			final L2PledgeSkillLearn[] skills = SkillTreeTable.getInstance().getAvailablePledgeSkills(player);
			
			for (final L2PledgeSkillLearn s : skills)
			{
				final L2Skill sk = SkillTable.getInstance().getInfo(s.getId(), s.getLevel());
				
				if (sk == null || sk != skill)
				{
					continue;
				}
				
				counts++;
				itemId = s.getItemId();
				repCost = s.getRepCost();
			}
			
			if (counts == 0)
			{
				player.sendMessage("You are trying to learn skill that u can't..");
				Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " tried to learn skill that he can't!!!", IllegalPlayerAction.PUNISH_KICK);
				return;
			}
			
			if (player.getClan().getReputationScore() >= repCost)
			{
				if (Config.LIFE_CRYSTAL_NEEDED)
				{
					if (!player.destroyItemByItemId("Consume", itemId, 1, trainer, false))
					{
						// Haven't spellbook
						player.sendPacket(new SystemMessage(SystemMessageId.ITEM_MISSING_TO_LEARN_SKILL));
						return;
					}
					
					final SystemMessage sm = new SystemMessage(SystemMessageId.DISSAPEARED_ITEM);
					sm.addItemName(itemId);
					sm.addNumber(1);
					sendPacket(sm);
				}
			}
			else
			{
				final SystemMessage sm = new SystemMessage(SystemMessageId.ACQUIRE_SKILL_FAILED_BAD_CLAN_REP_SCORE);
				player.sendPacket(sm);
				return;
			}
			player.getClan().setReputationScore(player.getClan().getReputationScore() - repCost, true);
			player.getClan().addNewSkill(skill);
			
			if (Config.DEBUG)
			{
				LOGGER.debug("Learned pledge skill " + id + " for " + requiredSp + " SP.");
			}
			
			final SystemMessage cr = new SystemMessage(SystemMessageId.S1_DEDUCTED_FROM_CLAN_REP);
			cr.addNumber(repCost);
			player.sendPacket(cr);
			final SystemMessage sm = new SystemMessage(SystemMessageId.CLAN_SKILL_S1_ADDED);
			sm.addSkillName(id);
			player.sendPacket(sm);
			
			player.getClan().broadcastToOnlineMembers(new PledgeSkillList(player.getClan()));
			
			for (final L2PcInstance member : player.getClan().getOnlineMembers(""))
			{
				member.sendSkillList();
			}
			
			if (trainer instanceof L2VillageMasterInstance)
			{
				((L2VillageMasterInstance) trainer).showPledgeSkillList(player);
			}
			
			return;
		}
		
		else
		{
			LOGGER.warn("Recived Wrong Packet Data in Aquired Skill - unk1:" + skillType);
			return;
		}
		
		player.addSkill(skill, true);
		
		if (Config.DEBUG)
		{
			LOGGER.debug("Learned skill " + id + " for " + requiredSp + " SP.");
		}
		
		player.setSp(player.getSp() - requiredSp);
		
		final StatusUpdate su = new StatusUpdate(player.getObjectId());
		su.addAttribute(StatusUpdate.SP, player.getSp());
		player.sendPacket(su);
		
		final SystemMessage sp = new SystemMessage(SystemMessageId.SP_DECREASED_S1);
		sp.addNumber(requiredSp);
		sendPacket(sp);
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.LEARNED_SKILL_S1);
		sm.addSkillName(id);
		player.sendPacket(sm);
		
		// update all the shortcuts to this skill
		if (level > 1)
		{
			for (L2ShortCut sc : player.getAllShortCuts())
			{
				if (sc.getId() == id && sc.getType() == L2ShortCut.TYPE_SKILL)
				{
					L2ShortCut newsc = new L2ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), level, 1);
					player.sendPacket(new ShortCutRegister(newsc));
					player.registerShortCut(newsc);
				}
			}
		}
		
		if (trainer instanceof L2FishermanInstance)
		{
			((L2FishermanInstance) trainer).showSkillList(player);
		}
		else
		{
			trainer.showSkillList(player, player.getSkillLearningClassId());
		}
		
		if (id >= 1368 && id <= 1372) // if skill is expand sendpacket :)
		{
			final ExStorageMaxCount esmc = new ExStorageMaxCount(player);
			player.sendPacket(esmc);
		}
		
		player.sendSkillList();
	}
	
	@Override
	public String getType()
	{
		return "[C] 6C RequestAquireSkill";
	}
}
