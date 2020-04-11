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
 * Format (ch) dd c: (id) 0xD0 h: (subid) 0x34 d: skill id d: skill lvl
 * @author -Wooden-
 */
public final class RequestExEnchantSkillRouteChange extends L2GameClientPacket
{
	private static final String _C__D0_34_REQUESTEXENCHANTSKILLROUTECHANGE = "[C] D0:34 RequestExEnchantSkillRouteChange";
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
		
		int reqItemId = EnchantSkillGroupsData.CHANGE_ENCHANT_BOOK;
		
		L2EnchantSkillLearn s = EnchantSkillGroupsData.getInstance().getSkillEnchantmentBySkillId(_skillId);
		if (s == null)
		{
			return;
		}
		
		final int beforeEnchantSkillLevel = player.getSkillLevel(_skillId);
		// do u have this skill enchanted?
		if (beforeEnchantSkillLevel <= 100)
		{
			return;
		}
		
		int currentEnchantLevel = beforeEnchantSkillLevel % 100;
		// is the requested level valid?
		if (currentEnchantLevel != (_skillLvl % 100))
		{
			return;
		}
		EnchantSkillHolder esd = s.getEnchantSkillHolder(_skillLvl);
		
		int requiredSp = esd.getSpCost();
		int requireditems = esd.getAdenaCost();
		
		if (player.getSp() >= requiredSp)
		{
			// only first lvl requires book
			L2ItemInstance spb = player.getInventory().getItemByItemId(reqItemId);
			if (Config.ES_SP_BOOK_NEEDED)
			{
				if (spb == null)// Haven't spellbook
				{
					player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_ITENS_NEEDED_TO_CHANGE_SKILL_ENCHANT_ROUTE);
					return;
				}
			}
			
			if (player.getInventory().getAdena() < requireditems)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			boolean check;
			check = player.getStat().removeExpAndSp(0, requiredSp, false);
			if (Config.ES_SP_BOOK_NEEDED)
			{
				check &= player.destroyItem("Consume", spb.getObjectId(), 1, player, true);
			}
			
			check &= player.destroyItemByItemId("Consume", Inventory.ADENA_ID, requireditems, player, true);
			
			if (!check)
			{
				player.sendPacket(SystemMessageId.YOU_DONT_HAVE_ALL_OF_THE_ITEMS_NEEDED_TO_ENCHANT_THAT_SKILL);
				return;
			}
			
			int levelPenalty = Rnd.get(Math.min(4, currentEnchantLevel));
			_skillLvl -= levelPenalty;
			if ((_skillLvl % 100) == 0)
			{
				_skillLvl = s.getBaseLevel();
			}
			
			skill = SkillData.getInstance().getInfo(_skillId, _skillLvl);
			
			if (skill != null)
			{
				if (Config.LOG_SKILL_ENCHANTS)
				{
					LogRecord record = new LogRecord(Level.INFO, "Route Change");
					record.setParameters(new Object[]
					{
						player,
						skill,
						spb
					});
					record.setLoggerName("skill");
					_logEnchant.log(record);
				}
				
				player.addSkill(skill, true);
				player.sendPacket(ExEnchantSkillResult.valueOf(true));
			}
			
			if (Config.DEBUG)
			{
				_log.info("Learned skill ID: " + _skillId + " Level: " + _skillLvl + " for " + requiredSp + " SP, " + requireditems + " Adena.");
			}
			
			player.sendPacket(new UserInfo(player));
			player.sendPacket(new ExBrExtraUserInfo(player));
			
			if (levelPenalty == 0)
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WILL_REMAIN);
				sm.addSkillName(_skillId);
				player.sendPacket(sm);
			}
			else
			{
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.SKILL_ENCHANT_CHANGE_SUCCESSFUL_S1_LEVEL_WAS_DECREASED_BY_S2);
				sm.addSkillName(_skillId);
				sm.addInt(levelPenalty);
				player.sendPacket(sm);
			}
			player.sendSkillList();
			final int afterEnchantSkillLevel = player.getSkillLevel(_skillId);
			player.sendPacket(new ExEnchantSkillInfo(_skillId, afterEnchantSkillLevel));
			player.sendPacket(new ExEnchantSkillInfoDetail(3, _skillId, afterEnchantSkillLevel, player));
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
		return _C__D0_34_REQUESTEXENCHANTSKILLROUTECHANGE;
	}
}
