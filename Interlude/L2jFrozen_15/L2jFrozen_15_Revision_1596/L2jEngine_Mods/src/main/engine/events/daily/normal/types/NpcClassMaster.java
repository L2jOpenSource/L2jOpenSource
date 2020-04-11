package main.engine.events.daily.normal.types;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.base.ClassId;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.TutorialShowQuestionMark;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.daily.AbstractEvent;
import main.enums.PlaySoundType;
import main.enums.TeamType;
import main.holders.LocationHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilMessage;
import main.util.UtilSpawn;
import main.util.builders.html.HtmlBuilder;

/**
 * @author fissban
 */
public class NpcClassMaster extends AbstractEvent
{
	private static final int NPC = 70434;
	
	// format:
	// PriceItemId
	// PriceItemCount
	// ReardItemId
	// RewardItemCount
	
	// Usar el 0 para no definir ningun valor
	private static final List<ClassMasterList> ITEM_LIST = new ArrayList<>();
	{
		// Primer cambio de clase ----------------------
		ClassMasterList job1 = new ClassMasterList();
		job1.setPriceItemId(ConfigData.CLASSMASTER_PRICE_JOB1.getRewardId());
		job1.setPriceItemCount(ConfigData.CLASSMASTER_PRICE_JOB1.getRewardCount());
		job1.setRewardItemId(ConfigData.CLASSMASTER_REWARD_JOB1.getRewardId());
		job1.setRewardItemCount(ConfigData.CLASSMASTER_REWARD_JOB1.getRewardCount());
		ITEM_LIST.add(job1);
		// Segundo cambio de clase ----------------------
		ClassMasterList job2 = new ClassMasterList();
		job2.setPriceItemId(ConfigData.CLASSMASTER_PRICE_JOB2.getRewardId());
		job2.setPriceItemCount(ConfigData.CLASSMASTER_PRICE_JOB2.getRewardCount());
		job2.setRewardItemId(ConfigData.CLASSMASTER_REWARD_JOB2.getRewardCount());
		job2.setRewardItemCount(ConfigData.CLASSMASTER_REWARD_JOB2.getRewardCount());
		ITEM_LIST.add(job2);
		// Tercer cambio de clase -----------------------
		ClassMasterList job3 = new ClassMasterList();
		job3.setPriceItemId(ConfigData.CLASSMASTER_PRICE_JOB3.getRewardId());
		job3.setPriceItemCount(ConfigData.CLASSMASTER_PRICE_JOB3.getRewardCount());
		job3.setRewardItemId(ConfigData.CLASSMASTER_REWARD_JOB3.getRewardCount());
		job3.setRewardItemCount(ConfigData.CLASSMASTER_REWARD_JOB3.getRewardCount());
		ITEM_LIST.add(job3);
	}
	// Spawns
	private static final List<LocationHolder> SPAWNS = new ArrayList<>();
	{
		SPAWNS.add(new LocationHolder(11283, 15951, -4584));// ClassChange_DE
		SPAWNS.add(new LocationHolder(115774, -178666, -958));// ClassChange_DW
		SPAWNS.add(new LocationHolder(45036, 48384, -3060));// ClassChange_E
		SPAWNS.add(new LocationHolder(-44747, -113865, -208));// ClassChange_ORC
		SPAWNS.add(new LocationHolder(-84466, 243171, -3729));// ClassChange_TI
	}
	// Html
	private static final String HTML_PATH = "data/html/engine/events/classmaster/";
	
	private static final List<NpcHolder> npcs = new ArrayList<>();
	
	public NpcClassMaster()
	{
		registerEvent(ConfigData.ENABLE_ClassMaster, ConfigData.CLASSMASTER_DATE_START, ConfigData.CLASSMASTER_DATE_END);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				UtilMessage.sendAnnounceMsg("Event Class Master: Started!", L2World.getInstance().getAllPlayers());
				
				ThreadPool.schedule(() ->
				{
					SPAWNS.forEach(loc -> npcs.add(UtilSpawn.npc(NPC, loc.getX(), loc.getY(), loc.getZ(), 0, 0, 0, TeamType.NONE, 0)));
				}, 20000);
				
				break;
			case END:
				UtilMessage.sendAnnounceMsg("Event Class Master: End!", L2World.getInstance().getAllPlayers());
				npcs.forEach(npc -> npc.getInstance().deleteMe());
				break;
		}
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		UtilMessage.sendAnnounceMsg("Event Class Master: Started!", ph);
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (Util.areObjectType(L2NpcInstance.class, character))
		{
			L2NpcInstance npc = (L2NpcInstance) character.getInstance();
			
			if (npc.getNpcId() == NPC)
			{
				sendHtmlFile(ph, npc, HTML_PATH + "index.htm");
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String event)
	{
		if (((NpcHolder) npc).getId() != NPC)
		{
			return;
		}
		
		if (event.startsWith("1stClass"))
		{
			showHtmlMenu(ph, npc.getObjectId(), 1);
		}
		else if (event.startsWith("2ndClass"))
		{
			showHtmlMenu(ph, npc.getObjectId(), 2);
		}
		else if (event.startsWith("3rdClass"))
		{
			showHtmlMenu(ph, npc.getObjectId(), 3);
		}
		else if (event.startsWith("change_class"))
		{
			try
			{
				int val = Integer.parseInt(event.split(" ")[1]);
				
				if (checkAndChangeClass(ph.getInstance(), val))
				{
					// Send sound
					ph.getInstance().sendPacket(new PlaySound(PlaySoundType.CHAR_CHANGE.getName()));
					// Send html
					NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
					html.setFile(HTML_PATH + "ok.htm");
					html.replace("%name%", ClassId.values()[val].name());
					ph.getInstance().sendPacket(html);
				}
			}
			catch (Exception e)
			{
				//
			}
		}
	}
	
	public static final void showHtmlMenu(PlayerHolder ph, int objectId, int level)
	{
		NpcHtmlMessage html = new NpcHtmlMessage(objectId);
		
		ClassId currentClassId = ph.getInstance().getClassId();
		if (currentClassId.level() >= level)
		{
			html.setFile(HTML_PATH + "nomore.htm");
		}
		else
		{
			int minLevel = getMinLevel(currentClassId.level());
			if (ph.getInstance().getLevel() >= minLevel)
			{
				HtmlBuilder menu = new HtmlBuilder();
				for (ClassId cid : ClassId.values())
				{
					if (validateClassId(currentClassId, cid) && (cid.level() == level))
					{
						String className = cid.name();
						menu.append("<img src=L2UI_CH3.br_bar2_mp width=204 height=1>");
						menu.append("<table width=204>");
						menu.append("<tr>");
						menu.append("<td><button value=\"", className, "\" action=\"bypass -h Engine NpcClassMaster change_class ", cid.getId() + "\" width=204 height=20 back=L2UI_CH3.br_party1_back2 fore=L2UI_CH3.balloon2_2></td>>");
						menu.append("</tr>");
						menu.append("</table>");
					}
				}
				
				if (menu.toString().length() > 0)
				{
					menu.append("<img src=L2UI_CH3.br_bar2_mp width=204 height=1>");
					
					html.setFile(HTML_PATH + "template.htm");
					html.replace("%name%", currentClassId.name());
					html.replace("%menu%", menu.toString());
				}
				else
				{
					html.setFile(HTML_PATH + "comebacklater.htm");
					html.replace("%level%", String.valueOf(getMinLevel(level - 1)));
				}
			}
			else
			{
				if (minLevel < Integer.MAX_VALUE)
				{
					html.setFile(HTML_PATH + "comebacklater.htm");
					html.replace("%level%", String.valueOf(minLevel));
				}
				else
				{
					html.setFile(HTML_PATH + "nomore.htm");
				}
			}
		}
		
		html.replace("%objectId%", String.valueOf(objectId));
		html.replace("%req_items%", getRequiredItems(level));
		ph.getInstance().sendPacket(html);
	}
	
	private static final void showQuestionMark(L2PcInstance player)
	{
		ClassId classId = player.getClassId();
		if (getMinLevel(classId.level()) > player.getLevel())
		{
			return;
		}
		
		player.sendPacket(new TutorialShowQuestionMark(1001));
	}
	
	/**
	 * Returns minimum player level required for next class transfer
	 * @param  level - current skillId level (0 - start, 1 - first, etc)
	 * @return
	 */
	private static final int getMinLevel(int level)
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
	 * @param  oldCID current player ClassId
	 * @param  val    new class index
	 * @return
	 */
	private static final boolean validateClassId(ClassId oldCID, int val)
	{
		try
		{
			return validateClassId(oldCID, ClassId.values()[val]);
		}
		catch (Exception e)
		{
			// possible ArrayOutOfBoundsException
		}
		return false;
	}
	
	/**
	 * Returns true if class change is possible
	 * @param  oldCID current player ClassId
	 * @param  newCID new ClassId
	 * @return        true if class change is possible
	 */
	private static final boolean validateClassId(ClassId oldCID, ClassId newCID)
	{
		if ((newCID == null) || (newCID.getRace() == null))
		{
			return false;
		}
		
		if (oldCID.equals(newCID.getParent()))
		{
			return true;
		}
		
		return false;
	}
	
	private static String getRequiredItems(int level)
	{
		level--;
		StringBuilder sb = new StringBuilder();
		if ((ITEM_LIST.get(level).getPriceItemId() != 0) && (ITEM_LIST.get(level).getPriceItemCount() != 0))
		{
			int count = ITEM_LIST.get(level).getPriceItemCount();
			String itemName = ItemTable.getInstance().getTemplate(ITEM_LIST.get(level).getPriceItemId()).getName();
			sb.append("<tr><td><img src=L2UI_CH3.mapicon_mark width=32 height=32></td>");
			sb.append("<td><font color=\"LEVEL\">[" + count + "]</font></td>");
			sb.append("<td>[" + itemName + "]</td>");
			sb.append("<td><img src=L2UI_CH3.mapicon_mark width=32 height=32></td></tr>");
		}
		else
		{
			sb.append("<tr><td>None</td></tr>");
		}
		return sb.toString();
	}
	
	private static final boolean checkAndChangeClass(L2PcInstance player, int val)
	{
		ClassId currentClassId = player.getClassId();
		if (getMinLevel(currentClassId.level()) > player.getLevel())
		{
			return false;
		}
		
		if (!validateClassId(currentClassId, val))
		{
			return false;
		}
		
		int newJobLevel = currentClassId.level();
		
		// Weight/Inventory check
		if (ITEM_LIST.get(newJobLevel).getRewardItemId() != 0)
		{
			if ((player.getWeightPenalty() >= 3) || ((player.getInventoryLimit() * 0.8) <= player.getInventory().getSize()))
			{
				player.sendPacket(SystemMessageId.INVENTORY_LESS_THAN_80_PERCENT);
				return false;
			}
		}
		
		// get all required items for class transfer
		int priceCount = ITEM_LIST.get(newJobLevel).getPriceItemCount();
		int priceItemId = ITEM_LIST.get(newJobLevel).getPriceItemId();
		
		if (player.getInventory().destroyItemByItemId("ClassMaster", priceItemId, priceCount, player, null) == null)
		{
			player.sendPacket(SystemMessageId.NOT_ENOUGH_ITEMS);
			return false;
		}
		
		// reward player with items
		int rewardCount = ITEM_LIST.get(newJobLevel).getRewardItemCount();
		int rewardItemId = ITEM_LIST.get(newJobLevel).getRewardItemId();
		player.getInventory().addItem("ClassMaster", rewardItemId, rewardCount, player, null);
		
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
		
		if (((player.getClassId().level() == 1) && (player.getLevel() >= 40)) || ((player.getClassId().level() == 2) && (player.getLevel() >= 76)))
		{
			showQuestionMark(player);
		}
		
		return true;
	}
	
	public class ClassMasterList
	{
		private int priceItemId;
		private int priceItemCount;
		
		private int rewardItemId;
		private int rewardItemCount;
		
		public ClassMasterList()
		{
			//
		}
		
		public int getPriceItemId()
		{
			return priceItemId;
		}
		
		public void setPriceItemId(int priceItemId)
		{
			this.priceItemId = priceItemId;
		}
		
		public int getPriceItemCount()
		{
			return priceItemCount;
		}
		
		public void setPriceItemCount(int priceItemCount)
		{
			this.priceItemCount = priceItemCount;
		}
		
		public int getRewardItemId()
		{
			return rewardItemId;
		}
		
		public void setRewardItemId(int rewardItemId)
		{
			this.rewardItemId = rewardItemId;
		}
		
		public int getRewardItemCount()
		{
			return rewardItemCount;
		}
		
		public void setRewardItemCount(int rewardItemCount)
		{
			this.rewardItemCount = rewardItemCount;
		}
	}
}
