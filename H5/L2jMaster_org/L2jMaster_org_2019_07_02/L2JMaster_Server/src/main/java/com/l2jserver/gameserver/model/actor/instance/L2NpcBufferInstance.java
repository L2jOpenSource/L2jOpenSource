/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.model.actor.instance;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.l2jserver.gameserver.cache.HtmCache;
import com.l2jserver.gameserver.data.sql.impl.NpcBufferTable;
import com.l2jserver.gameserver.data.sql.impl.NpcBufferTable.NpcBufferData;
import com.l2jserver.gameserver.datatables.SkillData;
import com.l2jserver.gameserver.enums.InstanceType;
import com.l2jserver.gameserver.model.actor.L2Character;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.templates.L2NpcTemplate;
import com.l2jserver.gameserver.model.items.instance.L2ItemInstance;
import com.l2jserver.gameserver.model.skills.Skill;
import com.l2jserver.gameserver.network.SystemMessageId;
import com.l2jserver.gameserver.network.serverpackets.ActionFailed;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.SystemMessage;
import com.l2jserver.gameserver.taskmanager.AttackStanceTaskManager;

/**
 * The Class L2NpcBufferInstance.<br>
 * Zoey76: TODO: Unhardcode as DP base script for NPC Buffers.
 */
public class L2NpcBufferInstance extends L2Npc
{
	private static final Logger _log = Logger.getLogger(L2NpcBufferInstance.class.getName());
	
	private static final Map<Integer, Integer> pageVal = new HashMap<>();
	
	/**
	 * Creates a NPC buffer.
	 * @param template the template
	 */
	public L2NpcBufferInstance(L2NpcTemplate template)
	{
		super(template);
		setInstanceType(InstanceType.L2NpcBufferInstance);
	}
	
	@Override
	public void showChatWindow(L2PcInstance player, int val)
	{
		if (player == null)
		{
			return;
		}
		
		String htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/NpcBuffer.htm");
		if (val > 0)
		{
			htmContent = HtmCache.getInstance().getHtm(player.getHtmlPrefix(), "data/html/mods/NpcBuffer-" + val + ".htm");
		}
		
		if (htmContent != null)
		{
			final NpcHtmlMessage npcHtmlMessage = new NpcHtmlMessage(getObjectId());
			npcHtmlMessage.setHtml(htmContent);
			npcHtmlMessage.replace("%objectId%", String.valueOf(getObjectId()));
			player.sendPacket(npcHtmlMessage);
		}
		player.sendPacket(ActionFailed.STATIC_PACKET);
	}
	
	@Override
	public void onBypassFeedback(L2PcInstance player, String command)
	{
		// BypassValidation Exploit plug.
		if ((player == null) || (player.getLastFolkNPC() == null) || (player.getLastFolkNPC().getObjectId() != getObjectId()))
		{
			return;
		}
		
		L2Character target = player;
		if (command.startsWith("Pet"))
		{
			if (!player.hasSummon()) // TODO: Should be hasPet() ?
			{
				player.sendPacket(SystemMessageId.DONT_HAVE_PET);
				showChatWindow(player, 0); // 0 = main window
				return;
			}
			target = player.getSummon();
		}
		
		int npcId = getId();
		if (command.startsWith("Chat"))
		{
			int val = Integer.parseInt(command.substring(5));
			
			pageVal.put(player.getObjectId(), val);
			
			showChatWindow(player, val);
		}
		else if (command.startsWith("Buff") || command.startsWith("PetBuff"))
		{
			String[] buffGroupArray = command.substring(command.indexOf("Buff") + 5).split(" ");
			
			for (String buffGroupList : buffGroupArray)
			{
				if (buffGroupList == null)
				{
					_log.warning("NPC Buffer Warning: npcId = " + npcId + " has no buffGroup set in the bypass for the buff selected.");
					return;
				}
				
				int buffGroup = Integer.parseInt(buffGroupList);
				
				final NpcBufferData npcBuffGroupInfo = NpcBufferTable.getInstance().getSkillInfo(npcId, buffGroup);
				if (npcBuffGroupInfo == null)
				{
					_log.warning("NPC Buffer Warning: npcId = " + npcId + " Location: " + getX() + ", " + getY() + ", " + getZ() + " Player: " + player.getName() + " has tried to use skill group (" + buffGroup + ") not assigned to the NPC Buffer!");
					return;
				}
				
				if (npcBuffGroupInfo.getFee().getId() != 0)
				{
					L2ItemInstance itemInstance = player.getInventory().getItemByItemId(npcBuffGroupInfo.getFee().getId());
					if ((itemInstance == null) || (!itemInstance.isStackable() && (player.getInventory().getInventoryItemCount(npcBuffGroupInfo.getFee().getId(), -1) < npcBuffGroupInfo.getFee().getCount())))
					{
						SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
						player.sendPacket(sm);
						continue;
					}
					
					if (itemInstance.isStackable())
					{
						if (!player.destroyItemByItemId("Npc Buffer", npcBuffGroupInfo.getFee().getId(), npcBuffGroupInfo.getFee().getCount(), player.getTarget(), true))
						{
							SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.THERE_ARE_NOT_ENOUGH_NECESSARY_ITEMS_TO_USE_THE_SKILL);
							player.sendPacket(sm);
							continue;
						}
					}
					else
					{
						for (int i = 0; i < npcBuffGroupInfo.getFee().getCount(); ++i)
						{
							player.destroyItemByItemId("Npc Buffer", npcBuffGroupInfo.getFee().getId(), 1, player.getTarget(), true);
						}
					}
				}
				
				final Skill skill = SkillData.getInstance().getSkill(npcBuffGroupInfo.getSkill().getSkillId(), npcBuffGroupInfo.getSkill().getSkillLvl());
				if (skill != null)
				{
					skill.applyEffects(player, target);
				}
			}
			
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else if (command.startsWith("Heal") || command.startsWith("PetHeal"))
		{
			if (!target.isInCombat() && !AttackStanceTaskManager.getInstance().hasAttackStanceTask(target))
			{
				String[] healArray = command.substring(command.indexOf("Heal") + 5).split(" ");
				
				for (String healType : healArray)
				{
					if (healType.equalsIgnoreCase("HP"))
					{
						target.setCurrentHp(target.getMaxHp());
					}
					else if (healType.equalsIgnoreCase("MP"))
					{
						target.setCurrentMp(target.getMaxMp());
					}
					else if (healType.equalsIgnoreCase("CP"))
					{
						target.setCurrentCp(target.getMaxCp());
					}
				}
			}
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else if (command.startsWith("RemoveBuffs") || command.startsWith("PetRemoveBuffs"))
		{
			target.stopAllEffectsExceptThoseThatLastThroughDeath();
			showChatWindow(player, pageVal.get(player.getObjectId()));
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
}
