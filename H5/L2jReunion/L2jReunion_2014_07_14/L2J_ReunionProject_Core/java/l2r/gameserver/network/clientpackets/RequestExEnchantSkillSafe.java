/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.network.clientpackets;

import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import l2r.Config;
import l2r.gameserver.datatables.xml.EnchantSkillGroupsData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.model.L2EnchantSkillGroup.EnchantSkillHolder;
import l2r.gameserver.model.L2EnchantSkillLearn;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.itemcontainer.Inventory;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExBrExtraUserInfo;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfo;
import l2r.gameserver.network.serverpackets.ExEnchantSkillInfoDetail;
import l2r.gameserver.network.serverpackets.ExEnchantSkillResult;
import l2r.gameserver.network.serverpackets.SystemMessage;
import l2r.gameserver.network.serverpackets.UserInfo;
import l2r.util.Rnd;

/**
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x32 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillSafe extends L2GameClientPacket
{
	private static final String _C__D0_32_REQUESTEXENCHANTSKILLSAFE = "[C] D0:32 RequestExEnchantSkillSafe";
	private static final Logger _logEnchant = Logger.getLogger("enchant");
	
	private int _skillId;
	private int _skillLvl;
	
	@Override
	protected void readImpl()
	{
		_skillId = readD();
		_skillLvl = readD();
	}
	
	@Override
	protected void runImpl()
	{
		if ((_skillId <= 0) || (_skillLvl <= 0))
		{
			return;
		}
		
		L2PcInstance player = getClient().getActiveChar();
		if (player == null)
		{
			return;
		}
		
		if (player.getClassId().level() < 3) // requires to have 3rd class quest completed
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_IN_THIS_CLASS);
			return;
		}
		
		if (player.getLevel() < 76)
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ON_THIS_LEVEL);
			return;
		}
		
		if (!player.isAllowedToEnchantSkills())
		{
			player.sendPacket(SystemMessageId.YOU_CANNOT_USE_SKILL_ENCHANT_ATTACKING_TRANSFORMED_BOAT);
			return;
		}
		
		L2Skill skill = SkillData.getInstance().getInfo(_skillId, _skillLvl);
		if (skill == null)
		{
			return;
		}
		
		int costMultiplier = EnchantSkillGroupsData.SAFE_ENCHANT_COST_MULTIPLIER;
		int reqItemId = EnchantSkillGroupsData.SAFE_ENCHANT_BOOK;
		
		L2EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		final EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
		final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		if (beforeEnchantSkillLevel != s.getMinSkillLevel(_skillLvl))
		{
			return;
		}
		
		int requiredSp = esd.getSpCost() * costMultiplier;
		int requireditems = esd.getAdenaCost() * costMultiplier;
		int rate = esd.getRate(player);
		
		if (player.getSp() >= requiredSp)
		{
			// No config option for safe enchant book consume
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (spb == null)// Haven't spellbook
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			if (player.getInventory().getAdena() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			boolean check = player.getStat().removeExpAndSp(0, requiredSp, false);
			check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			
			check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
			
			if (!check)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			// ok. Destroy ONE copy of the book
			if (Rnd.get(100) <= rate)
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					LogRecord record = new LogRecord(Level.INFO, "Safe Success");
					record.setParameters(new Object[]
					{
						player,
						skill,
						spb,
						rate
					});
					record.setLoggerName("skill");
					_logEnchant.log(record);
				}
				
				player.addSkill(skill, true);
				
				if (Config.DEBUG)
				{
					_log.info("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
				}
				
				player.sendPacket(ExEnchantSkillResult.valueOf(true));
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_HAVE_SUCCEEDED_IN_ENCHANTING_THE_SKILL_S1);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					LogRecord record = new LogRecord(Level.INFO, "Safe Fail");
					record.setParameters(new Object[]
					{
						player,
						skill,
						spb,
						rate
					});
					record.setLoggerName("skill");
					_logEnchant.log(record);
				}
				
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_FAILED_S1_LEVEL_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
				player.sendPacket(ExEnchantSkillResult.valueOf(false));
			}
			
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			player.sendSkillList();
			final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(1, _skillId, afterEnchantSkillLevel + 1, player));
			player.updateShortCuts(_skillId, afterEnchantSkillLevel);
		}
		else
		{
			SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.YOU_DONT_HAVE_ENOUGH_SP_TO_ENCHANT_THAT_SKILL);
			player.sendPacket(sm);
		}
	}
	
	@Override
	public String getType()
	{
		return _C__D0_32_REQUESTEXENCHANTSKILLSAFE;
	}
}
