/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package ai.npc.ClassMaster;

import static com.l2jserver.Config.ALLOW_CLASS_MASTERS;
import static com.l2jserver.Config.ALLOW_ENTIRE_TREE;
import static com.l2jserver.Config.ALTERNATE_CLASS_MASTER;
import static com.l2jserver.Config.AUTO_LEARN_FS_SKILLS;
import static com.l2jserver.Config.CLASS_MASTER_SETTINGS;
import static com.l2jserver.gameserver.model.events.EventType.ON_PLAYER_LEVEL_CHANGED;
import static com.l2jserver.gameserver.network.SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT;
import static com.l2jserver.gameserver.network.SystemMessageId.NOT_ENOUGH_ITEMS;
import static com.l2jserver.gameserver.network.serverpackets.TutorialCloseHtml.STATIC_PACKET;

import com.l2jserver.Config;
import com.l2jserver.gameserver.data.xml.impl.ClassListData;
import com.l2jserver.gameserver.datatables.ItemTable;
import com.l2jserver.gameserver.model.Location;
import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.base.ClassId;
import com.l2jserver.gameserver.model.events.Containers;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerLevelChanged;
import com.l2jserver.gameserver.model.events.listeners.ConsumerEventListener;
import com.l2jserver.gameserver.model.holders.ItemHolder;
import com.l2jserver.gameserver.network.serverpackets.ExBrExtraUserInfo;
import com.l2jserver.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jserver.gameserver.network.serverpackets.TutorialShowHtml;
import com.l2jserver.gameserver.network.serverpackets.TutorialShowQuestionMark;
import com.l2jserver.gameserver.network.serverpackets.UserInfo;
import com.l2jserver.util.StringUtil;

import ZeuS.ZeuS;
import ai.npc.AbstractNpcAI;

/**
 * Handle both NPC and tutorial window.
 * @author Zealar
 * @since 2.6.0.0
 */
public final class ClassMaster extends AbstractNpcAI
{
	// NPCs
	private static final int MR_CAT = 31756;
	private static final int MISS_QUEEN = 31757;
	// Vars
	private static final int CUSTOM_EVENT_ID = 1001;
	
	private ClassMaster()
	{
		super(ClassMaster.class.getSimpleName(), "ai/npc");
		addStartNpc(MR_CAT, MISS_QUEEN);
		addFirstTalkId(MR_CAT, MISS_QUEEN);
		addTalkId(MR_CAT, MISS_QUEEN);
		if (ALTERNATE_CLASS_MASTER)
		{
			setOnEnterWorld(true);
			registerTutorialEvent();
			registerTutorialQuestionMark();
		}
		
		if (ALLOW_CLASS_MASTERS)
		{
			addSpawn(MR_CAT, new Location(147728, 27408, -2198, 16500));
			addSpawn(MISS_QUEEN, new Location(147761, 27408, -2198, 16500));
			addSpawn(MR_CAT, new Location(148560, -57952, -2974, 53000));
			addSpawn(MISS_QUEEN, new Location(148514, -57972, -2974, 53000));
			addSpawn(MR_CAT, new Location(110592, 220400, -3667, 0));
			addSpawn(MISS_QUEEN, new Location(110592, 220443, -3667, 0));
			addSpawn(MR_CAT, new Location(117200, 75824, -2725, 25000));
			addSpawn(MISS_QUEEN, new Location(117160, 75784, -2725, 25000));
			addSpawn(MR_CAT, new Location(116224, -181728, -1373, 0));
			addSpawn(MISS_QUEEN, new Location(116218, -181793, -1379, 0));
			addSpawn(MR_CAT, new Location(114880, -178144, -827, 0));
			addSpawn(MISS_QUEEN, new Location(114880, -178196, -827, 0));
			addSpawn(MR_CAT, new Location(83076, 147912, -3467, 32000));
			addSpawn(MISS_QUEEN, new Location(83082, 147845, -3467, 32000));
			addSpawn(MR_CAT, new Location(81136, 54576, -1517, 32000));
			addSpawn(MISS_QUEEN, new Location(81126, 54519, -1517, 32000));
			addSpawn(MR_CAT, new Location(45472, 49312, -3067, 53000));
			addSpawn(MISS_QUEEN, new Location(45414, 49296, -3067, 53000));
			addSpawn(MR_CAT, new Location(47648, 51296, -2989, 38500));
			addSpawn(MISS_QUEEN, new Location(47680, 51255, -2989, 38500));
			addSpawn(MR_CAT, new Location(17956, 170536, -3499, 48000));
			addSpawn(MISS_QUEEN, new Location(17913, 170536, -3499, 48000));
			addSpawn(MR_CAT, new Location(15584, 142784, -2699, 16500));
			addSpawn(MISS_QUEEN, new Location(15631, 142778, -2699, 16500));
			addSpawn(MR_CAT, new Location(11340, 15972, -4577, 14000));
			addSpawn(MISS_QUEEN, new Location(11353, 16022, -4577, 14000));
			addSpawn(MR_CAT, new Location(10968, 17540, -4567, 55000));
			addSpawn(MISS_QUEEN, new Location(10918, 17511, -4567, 55000));
			addSpawn(MR_CAT, new Location(-14048, 123184, -3115, 32000));
			addSpawn(MISS_QUEEN, new Location(-14050, 123229, -3115, 32000));
			addSpawn(MR_CAT, new Location(-44979, -113508, -194, 32000));
			addSpawn(MISS_QUEEN, new Location(-44983, -113554, -194, 32000));
			addSpawn(MR_CAT, new Location(-84119, 243254, -3725, 8000));
			addSpawn(MISS_QUEEN, new Location(-84047, 243193, -3725, 8000));
			addSpawn(MR_CAT, new Location(-84336, 242156, -3725, 24500));
			addSpawn(MISS_QUEEN, new Location(-84294, 242204, -3725, 24500));
			addSpawn(MR_CAT, new Location(-82032, 150160, -3122, 16500));
			addSpawn(MISS_QUEEN, new Location(-81967, 150160, -3122, 16500));
			addSpawn(MR_CAT, new Location(147865, -58047, -2979, 48999));
			addSpawn(MISS_QUEEN, new Location(147906, -58047, -2979, 48999));
			addSpawn(MR_CAT, new Location(147300, -56466, -2779, 11500));
			addSpawn(MISS_QUEEN, new Location(147333, -56483, -2784, 11500));
			addSpawn(MR_CAT, new Location(44176, -48732, -800, 33000));
			addSpawn(MISS_QUEEN, new Location(44176, -48688, -800, 33000));
			addSpawn(MR_CAT, new Location(44333, -47639, -800, 49999));
			addSpawn(MISS_QUEEN, new Location(44371, -47638, -800, 49999));
			addSpawn(MR_CAT, new Location(87596, -140674, -1542, 16500));
			addSpawn(MISS_QUEEN, new Location(87644, -140674, -1542, 16500));
			addSpawn(MR_CAT, new Location(87824, -142256, -1343, 44000));
			addSpawn(MISS_QUEEN, new Location(87856, -142272, -1344, 44000));
			addSpawn(MR_CAT, new Location(-116948, 46841, 367, 49151));
			addSpawn(MISS_QUEEN, new Location(-116902, 46841, 367, 49151));
		}
	}
	
	@Override
	public void onTutorialEvent(L2PcInstance player, String command)
	{
		if (Config.ZEUS_ACTIVE && ZeuS.zeusByPass(player, command))
		{
			return;
		}
		if (command.startsWith("CO"))
		{
			onTutorialLink(player, command);
		}
		super.onTutorialEvent(player, command);
	}
	
	@Override
	public String onEnterWorld(L2PcInstance player)
	{
		showQuestionMark(player);
		Containers.Players().addListener(new ConsumerEventListener(Containers.Players(), ON_PLAYER_LEVEL_CHANGED, (OnPlayerLevelChanged event) ->
		{
			showQuestionMark(event.getActiveChar());
		}, this));
		
		return super.onEnterWorld(player);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.endsWith(".htm"))
		{
			return event;
		}
		if (event.startsWith("1stClass"))
		{
			showHtmlMenu(player, npc.getObjectId(), 1);
		}
		else if (event.startsWith("2ndClass"))
		{
			showHtmlMenu(player, npc.getObjectId(), 2);
		}
		else if (event.startsWith("3rdClass"))
		{
			showHtmlMenu(player, npc.getObjectId(), 3);
		}
		else if (event.startsWith("change_class"))
		{
			int val = Integer.parseInt(event.substring(13));
			if (checkAndChangeClass(player, val))
			{
				String msg = getHtm(player.getHtmlPrefix(), "ok.htm").replace("%name%", ClassListData.getInstance().getClass(val).getClientCode());
				showResult(player, msg);
				return "";
			}
		}
		else if (event.startsWith("become_noble"))
		{
			if (!player.isNoble())
			{
				player.setNoble(true);
				player.sendPacket(new UserInfo(player));
				player.sendPacket(new ExBrExtraUserInfo(player));
				return "nobleok.htm";
			}
		}
		else if (event.startsWith("learn_skills"))
		{
			player.giveAvailableSkills(AUTO_LEARN_FS_SKILLS, true);
		}
		else if (event.startsWith("increase_clan_level"))
		{
			if (!player.isClanLeader())
			{
				return "noclanleader.htm";
			}
			if (player.getClan().getLevel() >= 5)
			{
				return "noclanlevel.htm";
			}
			player.getClan().changeLevel(5);
		}
		else
		{
			_log.warning("Player " + player + " send invalid request [" + event + "]");
		}
		return "";
	}
	
	@Override
	public String onFirstTalk(L2Npc npc, L2PcInstance player)
	{
		return npc.getId() + ".htm";
	}
	
	private void onTutorialLink(L2PcInstance player, String request)
	{
		if (!ALTERNATE_CLASS_MASTER || (request == null) || !request.startsWith("CO"))
		{
			return;
		}
		
		if (!player.getFloodProtectors().getServerBypass().tryPerformAction("changeclass"))
		{
			return;
		}
		
		try
		{
			int val = Integer.parseInt(request.substring(2));
			checkAndChangeClass(player, val);
		}
		catch (NumberFormatException e)
		{
			_log.warning("Player " + player + " send invalid class change request [" + request + "]!");
		}
		player.sendPacket(STATIC_PACKET);
	}
	
	@Override
	public String onTutorialQuestionMark(L2PcInstance player, int number)
	{
		if (!ALTERNATE_CLASS_MASTER || (number != CUSTOM_EVENT_ID))
		{
			return "";
		}
		
		showTutorialHtml(player);
		return "";
	}
	
	private void showQuestionMark(L2PcInstance player)
	{
		if (!ALTERNATE_CLASS_MASTER)
		{
			return;
		}
		
		final ClassId classId = player.getClassId();
		if (getMinLevel(classId.level()) > player.getLevel())
		{
			return;
		}
		
		if (!CLASS_MASTER_SETTINGS.isAllowed(classId.level() + 1))
		{
			return;
		}
		
		player.sendPacket(new TutorialShowQuestionMark(CUSTOM_EVENT_ID));
	}
	
	private void showHtmlMenu(L2PcInstance player, int objectId, int level)
	{
		if (!ALLOW_CLASS_MASTERS)
		{
			String msg = getHtm(player.getHtmlPrefix(), "disabled.htm");
			showResult(player, msg);
			return;
		}
		if (!CLASS_MASTER_SETTINGS.isAllowed(level))
		{
			final NpcHtmlMessage html = new NpcHtmlMessage(objectId);
			final int jobLevel = player.getClassId().level();
			final StringBuilder sb = new StringBuilder(100);
			sb.append("<html><body>");
			switch (jobLevel)
			{
				case 0:
					if (CLASS_MASTER_SETTINGS.isAllowed(1))
					{
						sb.append("Come back here when you reached level 20 to change your class.<br>");
					}
					else if (CLASS_MASTER_SETTINGS.isAllowed(2))
					{
						sb.append("Come back after your first occupation change.<br>");
					}
					else if (CLASS_MASTER_SETTINGS.isAllowed(3))
					{
						sb.append("Come back after your second occupation change.<br>");
					}
					else
					{
						sb.append("I can't change your occupation.<br>");
					}
					break;
				case 1:
					if (CLASS_MASTER_SETTINGS.isAllowed(2))
					{
						sb.append("Come back here when you reached level 40 to change your class.<br>");
					}
					else if (CLASS_MASTER_SETTINGS.isAllowed(3))
					{
						sb.append("Come back after your second occupation change.<br>");
					}
					else
					{
						sb.append("I can't change your occupation.<br>");
					}
					break;
				case 2:
					if (CLASS_MASTER_SETTINGS.isAllowed(3))
					{
						sb.append("Come back here when you reached level 76 to change your class.<br>");
					}
					else
					{
						sb.append("I can't change your occupation.<br>");
					}
					break;
				case 3:
					sb.append("There is no class change available for you anymore.<br>");
					break;
			}
			sb.append("</body></html>");
			html.setHtml(sb.toString());
			html.replace("%req_items%", getRequiredItems(level));
			player.sendPacket(html);
			return;
		}
		
		final ClassId currentClassId = player.getClassId();
		if (currentClassId.level() >= level)
		{
			String msg = getHtm(player.getHtmlPrefix(), "nomore.htm");
			showResult(player, msg);
			return;
		}
		
		final int minLevel = getMinLevel(currentClassId.level());
		if ((player.getLevel() >= minLevel) || ALLOW_ENTIRE_TREE)
		{
			final StringBuilder menu = new StringBuilder(100);
			for (ClassId cid : ClassId.values())
			{
				if ((cid == ClassId.inspector) && (player.getTotalSubClasses() < 2))
				{
					continue;
				}
				if (validateClassId(currentClassId, cid) && (cid.level() == level))
				{
					StringUtil.append(menu, "<a action=\"bypass -h Quest ClassMaster change_class ", String.valueOf(cid.getId()), "\">", ClassListData.getInstance().getClass(cid).getClientCode(), "</a><br>");
				}
			}
			
			if (menu.length() > 0)
			{
				String msg = getHtm(player.getHtmlPrefix(), "template.htm").replace("%name%", ClassListData.getInstance().getClass(currentClassId).getClientCode()).replace("%menu%", menu.toString());
				showResult(player, msg);
				return;
				
			}
			String msg = getHtm(player.getHtmlPrefix(), "comebacklater.htm").replace("%level%", String.valueOf(getMinLevel(level - 1)));
			showResult(player, msg);
			return;
		}
		
		if (minLevel < Integer.MAX_VALUE)
		{
			String msg = getHtm(player.getHtmlPrefix(), "comebacklater.htm").replace("%level%", String.valueOf(minLevel));
			showResult(player, msg);
			return;
		}
		
		showResult(player, getHtm(player.getHtmlPrefix(), "nomore.htm"));
	}
	
	private void showTutorialHtml(L2PcInstance player)
	{
		final ClassId currentClassId = player.getClassId();
		if ((getMinLevel(currentClassId.level()) > player.getLevel()) && !ALLOW_ENTIRE_TREE)
		{
			return;
		}
		
		String msg = getHtm(player.getHtmlPrefix(), "tutorialtemplate.htm");
		msg = msg.replaceAll("%name%", ClassListData.getInstance().getClass(currentClassId).getEscapedClientCode());
		
		final StringBuilder menu = new StringBuilder(100);
		for (ClassId cid : ClassId.values())
		{
			if ((cid == ClassId.inspector) && (player.getTotalSubClasses() < 2))
			{
				continue;
			}
			if (validateClassId(currentClassId, cid))
			{
				StringUtil.append(menu, "<a action=\"link CO", String.valueOf(cid.getId()), "\">", ClassListData.getInstance().getClass(cid).getEscapedClientCode(), "</a><br>");
			}
		}
		
		msg = msg.replaceAll("%menu%", menu.toString());
		msg = msg.replace("%req_items%", getRequiredItems(currentClassId.level() + 1));
		player.sendPacket(new TutorialShowHtml(msg));
	}
	
	private boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		final ClassId currentClassId = player.getClassId();
		if ((getMinLevel(currentClassId.level()) > player.getLevel()) && !ALLOW_ENTIRE_TREE)
		{
			return false;
		}
		
		if (!validateClassId(currentClassId, val))
		{
			return false;
		}
		
		final int newJobLevel = currentClassId.level() + 1;
		
		// Weight/Inventory check
		if (!CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel).isEmpty() && !player.isInventoryUnder90(false))
		{
			player.sendPacket(INVENTORY_LESS_THAN_80_PERCENT);
			return false;
		}
		
		// check if player have all required items for class transfer
		for (ItemHolder holder : CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel))
		{
			if (player.getInventory().getInventoryItemCount(holder.getId(), -1) < holder.getCount())
			{
				player.sendPacket(NOT_ENOUGH_ITEMS);
				return false;
			}
		}
		
		// get all required items for class transfer
		for (ItemHolder holder : CLASS_MASTER_SETTINGS.getRequireItems(newJobLevel))
		{
			if (!player.destroyItemByItemId("ClassMaster", holder.getId(), holder.getCount(), player, true))
			{
				return false;
			}
		}
		
		// reward player with items
		for (ItemHolder holder : CLASS_MASTER_SETTINGS.getRewardItems(newJobLevel))
		{
			player.addItem("ClassMaster", holder.getId(), holder.getCount(), player, true);
		}
		
		player.setClassId(val);
		
		if (player.isSubClassActive())
		{
			player.getSubClasses().get(player.getClassIndex()).setClassId(player.getActiveClass());
		}
		else
		{
			player.setBaseClass(player.getActiveClass());
		}
		
		player.broadcastUserInfo();
		
		if (CLASS_MASTER_SETTINGS.isAllowed(player.getClassId().level() + 1) && ALTERNATE_CLASS_MASTER && (((player.getClassId().level() == 1) && (player.getLevel() >= 40)) || ((player.getClassId().level() == 2) && (player.getLevel() >= 76))))
		{
			showQuestionMark(player);
		}
		
		return true;
	}
	
	/**
	 * @param level - current skillId level (0 - start, 1 - first, etc)
	 * @return minimum player level required for next class transfer
	 */
	private static int getMinLevel(int level)
	{
		switch (level)
		{
			case 0:
				return 20;
			case 1:
				return 40;
			case 2:
				return 76;
			default:
				return Integer.MAX_VALUE;
		}
	}
	
	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param val new class index
	 * @return {@code true} if the class ID is valid
	 */
	private static boolean validateClassId(ClassId oldCID, int val)
	{
		return validateClassId(oldCID, ClassId.getClassId(val));
	}
	
	/**
	 * Returns true if class change is possible
	 * @param oldCID current player ClassId
	 * @param newCID new ClassId
	 * @return true if class change is possible
	 */
	private static boolean validateClassId(ClassId oldCID, ClassId newCID)
	{
		return (newCID != null) && (newCID.getRace() != null) && ((oldCID.equals(newCID.getParent()) || (ALLOW_ENTIRE_TREE && newCID.childOf(oldCID))));
	}
	
	private static String getRequiredItems(int level)
	{
		if ((CLASS_MASTER_SETTINGS.getRequireItems(level) == null) || CLASS_MASTER_SETTINGS.getRequireItems(level).isEmpty())
		{
			return "<tr><td>none</td></tr>";
		}
		final StringBuilder sb = new StringBuilder();
		for (ItemHolder holder : CLASS_MASTER_SETTINGS.getRequireItems(level))
		{
			sb.append("<tr><td><font color=\"LEVEL\">");
			sb.append(holder.getCount());
			sb.append("</font></td><td>");
			sb.append(ItemTable.getInstance().getTemplate(holder.getId()).getName());
			sb.append("</td></tr>");
		}
		return sb.toString();
	}
	
	public static void main(String[] args)
	{
		new ClassMaster();
	}
}