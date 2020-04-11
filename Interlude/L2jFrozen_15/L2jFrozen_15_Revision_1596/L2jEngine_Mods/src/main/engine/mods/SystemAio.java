package main.engine.mods;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.StringTokenizer;

import com.l2jfrozen.gameserver.datatables.csv.MapRegionTable.TeleportWhereType;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2TeleporterInstance;
import com.l2jfrozen.gameserver.model.zone.L2ZoneType;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.skills.Stats;

import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.data.xml.SkillInfoData;
import main.engine.AbstractMod;
import main.holders.IntIntHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;
import main.util.builders.html.HtmlBuilder.HtmlType;

/**
 * @author fissban
 */
public class SystemAio extends AbstractMod
{
	public SystemAio()
	{
		registerMod(true);// TODO missing config
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				loadValuesFromDb();
				loadAllAios();
				break;
			case END:
				//
				break;
		}
	}
	
	@Override
	public boolean onInteract(PlayerHolder player, CharacterHolder npc)
	{
		if (player.isAio())
		{
			if (!Util.areObjectType(L2TeleporterInstance.class, npc))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onExitZone(CharacterHolder ph, L2ZoneType zone)
	{
		if (!Util.areObjectType(L2PcInstance.class, ph))
		{
			return;
		}
		
		if (!((PlayerHolder) ph).isAio())
		{
			return;
		}
		
		if (!ConfigData.AIO_CAN_EXIT_PEACE_ZONE)
		{
			startTimer("checkZone", 3000, null, (PlayerHolder) ph, false);
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder ph)
	{
		switch (timerName)
		{
			case "checkZone":
			{
				if ((ph.getInstance() != null) && !ph.getInstance().isInsideZone(L2Character.ZONE_PEACE))
				{
					ph.getInstance().teleToLocation(TeleportWhereType.Town);
				}
				break;
			}
			case "removeAio":
			{
				// TODO missing onExitWorld
				if (ph.getInstance() == null)
				{
					return;
				}
				// remove aio
				removeAio(ph);
				// informe new expire aio HTML
				informeExpireAio(ph, 1);
				break;
			}
		}
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		StringTokenizer st = new StringTokenizer(command, " ");
		
		String event = st.nextToken();
		switch (event)
		{
			case "allAio":
			{
				if (ph.getInstance().getAccessLevel().getLevel() < 1)
				{
					break;
				}
				
				getAllPlayerAios(ph, Integer.parseInt(st.nextToken()));
				break;
			}
			case "aioInfo":
			{
				informeExpireAio(ph, Integer.parseInt(st.nextToken()));
				break;
			}
		}
	}
	
	@Override
	public boolean onAdminCommand(PlayerHolder ph, String chat)
	{
		StringTokenizer st = new StringTokenizer(chat, " ");
		
		String command = st.nextToken().toLowerCase();
		switch (command)
		{
			case "allaio":
			{
				getAllPlayerAios(ph, 1);
				return true;
			}
			case "removeaio":
			{
				if (!checkTarget(ph))
				{
					return true;
				}
				
				PlayerHolder aio = (PlayerHolder) ph.getTarget();
				// Informed admin
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[System]", "player: " + aio.getName() + " is not Aio");
				// Informed player
				UtilMessage.sendCreatureMsg(aio, Say2.TELL, "[System]", "Dear " + aio.getName() + " your are not Aio");
				
				informeExpireAio(aio, 1);
				removeAio((PlayerHolder) ph.getTarget());
				return true;
			}
			case "setaio":
			{
				if (!checkTarget(ph))
				{
					return true;
				}
				
				if (!st.hasMoreTokens())
				{
					ph.getInstance().sendMessage("Correct command:");
					ph.getInstance().sendMessage("//setAio days");
					return true;
				}
				
				String days = st.nextToken();
				
				if (!Util.isNumber(days))
				{
					ph.getInstance().sendMessage("Correct command:");
					ph.getInstance().sendMessage("//setAio days");
					return true;
				}
				
				PlayerHolder aio = (PlayerHolder) ph.getTarget();
				
				// Create calendar
				GregorianCalendar time = new GregorianCalendar();
				time.add(Calendar.DAY_OF_YEAR, Integer.parseInt(days));
				// save time in DB
				setValueDB(aio, "aio", time.getTimeInMillis() + "");
				// save lvl in DB
				setValueDB(aio, "aio lvl", ph.getInstance().getLevel() + "");
				// saved state in memory
				aio.setAio(true, time.getTimeInMillis());
				
				addAio(aio, time.getTimeInMillis());
				
				// Informed admin
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[System]", "player: " + aio.getName() + " is Aio now");
				// Informed player
				UtilMessage.sendCreatureMsg(aio, Say2.TELL, "[System]", "Dear " + aio.getName() + " your are now Aio");
				
				informeExpireAio(aio, 1);
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		if (ph.isAio())
		{
			if (ph.getAioExpireDate() < System.currentTimeMillis())
			{
				removeAio(ph);
				return;
			}
			
			addAio(ph, ph.getAioExpireDate());
			informeExpireAio(ph, 1);
		}
	}
	
	@Override
	public double onStats(Stats stat, CharacterHolder character, double value)
	{
		if (!Util.areObjectType(L2PcInstance.class, character))
		{
			return value;
		}
		
		if (!ObjectData.get(PlayerHolder.class, character.getObjectId()).isAio())
		{
			return value;
		}
		
		if (ConfigData.AIO_STATS.containsKey(stat))
		{
			return value *= ConfigData.AIO_STATS.get(stat);
		}
		
		return value;
	}
	
	/**
	 * Send the character html informing the time expire AIO. (format: dd-MMM-yyyy)
	 * @param ph
	 * @param dayTime
	 */
	private void informeExpireAio(PlayerHolder ph, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append(Html.START);
		hb.append("<br>");
		hb.append(Html.head("AIO"));
		hb.append("<br>");
		
		hb.append("<font color=9900CC>AIO Expire Date: </font>", ph.getAioExpireDateFormat(), "<br>");
		hb.append("<font color=9900CC>The AIO have the skills:</font><br>");
		
		hb.append("<table>");
		int MAX_PER_PAGE = 12;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		for (IntIntHolder bh : ConfigData.AIO_LIST_SKILLS)
		{
			// min
			if (count < searchPage)
			{
				count++;
				continue;
			}
			// max
			if (count >= (searchPage + MAX_PER_PAGE))
			{
				continue;
			}
			
			hb.append("<tr>");
			hb.append("<td width=32><img src=", SkillInfoData.getSkillIcon(bh.getId()), " width=32 height=16></td>");
			hb.append("<td width=200><font color=LEVEL>", bh.getSkill().getName(), "</font></td>");
			hb.append("</tr>");
			count++;
		}
		hb.append("</table>");
		
		hb.append("<center>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("<table bgcolor=CC99FF>");
		hb.append("<tr>");
		
		int currentPage = 1;
		for (int i = 0; i < ConfigData.AIO_LIST_SKILLS.size(); i++)
		{
			if ((i % MAX_PER_PAGE) == 0)
			{
				hb.append("<td width=18 align=center><a action=\"bypass -h Engine SystemAio aioInfo ", currentPage, "\">" + currentPage, "</a></td>");
				currentPage++;
			}
		}
		
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("</center>");
		
		hb.append(Html.END);
		sendHtml(null, hb, ph);
	}
	
	public void addAio(PlayerHolder ph, long dayTime)
	{
		startTimer("removeAio", dayTime - System.currentTimeMillis(), null, ph, false);
		
		// Set Max Lvl
		if (ConfigData.AIO_SET_MAX_LVL)
		{
			ph.getInstance().getStat().addExp(ph.getInstance().getStat().getExpForLevel(78));
		}
		// clear karma
		if (ph.getInstance().getKarma() > 0)
		{
			ph.getInstance().setKarma(0);
		}
		// teleport to city
		if (!ph.getInstance().isInsideZone(L2Character.ZONE_PEACE))
		{
			ph.getInstance().teleToLocation(TeleportWhereType.Town);
		}
		
		// set custom tile
		ph.getInstance().setTitle(ConfigData.AIO_TITLE);
		// add skills for aio
		for (IntIntHolder bh : ConfigData.AIO_LIST_SKILLS)
		{
			ph.getInstance().addSkill(bh.getSkill(), false);
		}
		ph.getInstance().broadcastUserInfo();
		
		// give duals
		UtilInventory.giveItems(ph, ConfigData.AIO_ITEM_ID, 1, 15);
	}
	
	public void removeAio(PlayerHolder ph)
	{
		// remove state in memory
		ph.setAio(false, 0);
		// set old level for player
		int lvl = getValueDB(ph, "aio lvl").getInt();
		if (lvl > 0)
		{
			ph.getInstance().getStat().addExp(ph.getInstance().getStat().getExpForLevel(lvl));
		}
		// init title
		ph.getInstance().setTitle("");
		// update user info in client
		ph.getInstance().broadcastUserInfo();
		// take duals
		UtilInventory.takeItems(ph, ConfigData.AIO_ITEM_ID, 1);
		// save in memory
		setValueDB(ph, "aio", System.currentTimeMillis() + "");
	}
	
	public void getAllPlayerAios(PlayerHolder player, int page)
	{
		HtmlBuilder hb = new HtmlBuilder(HtmlType.HTML);
		
		hb.append(Html.START);
		hb.append("<br>");
		hb.append(Html.head("All AIO Players"));
		hb.append("<br>");
		
		hb.append("<table>");
		hb.append("<tr>");
		hb.append("<td width=64><font color=LEVEL>Player:</font></td><td width=200><font color=LEVEL>ExpireDate:</font></td>");
		hb.append("</tr>");
		hb.append("</table>");
		
		int MAX_PER_PAGE = 12;
		int searchPage = MAX_PER_PAGE * (page - 1);
		int count = 0;
		int countAio = 0;
		
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			if (ph.isAio())
			{
				countAio++;
				// min
				if (count < searchPage)
				{
					count++;
					continue;
				}
				// max
				if (count >= (searchPage + MAX_PER_PAGE))
				{
					continue;
				}
				
				hb.append("<table", (count % 2) == 0 ? " bgcolor=000000>" : ">");
				hb.append("<tr>");
				hb.append("<td width=64>", ph.getName(), "</td><td width=200>", ph.getAioExpireDateFormat(), "</td>");
				hb.append("</tr>");
				hb.append("</table>");
				count++;
			}
		}
		
		hb.append("<center>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("<table bgcolor=CC99FF>");
		hb.append("<tr>");
		
		int currentPage = 1;
		
		for (int i = 0; i < countAio; i++)
		{
			if ((i % MAX_PER_PAGE) == 0)
			{
				hb.append("<td width=18 align=center><a action=\"bypass -h Engine SystemAio allAio ", currentPage, "\">", currentPage, "</a></td>");
				currentPage++;
			}
		}
		
		hb.append("</tr>");
		hb.append("</table>");
		hb.append("<img src=L2UI.SquareGray width=264 height=1>");
		hb.append("</center>");
		
		hb.append(Html.END);
		sendHtml(null, hb, player);
	}
	
	private void loadAllAios()
	{
		for (PlayerHolder ph : ObjectData.getAll(PlayerHolder.class))
		{
			long dayTime = getValueDB(ph.getObjectId(), "aio").getLong();
			// Don't has value in db
			if (dayTime == 0)
			{
				continue;
			}
			
			if (dayTime < System.currentTimeMillis())
			{
				continue;
			}
			
			// saved state in memory
			ph.setAio(true, dayTime);
		}
	}
	
	private static boolean checkTarget(PlayerHolder ph)
	{
		if (ph.getInstance().getTarget() == null)
		{
			ph.getInstance().sendMessage("this command need target");
			return false;
		}
		
		if (!Util.areObjectType(L2PcInstance.class, ph.getTarget()))
		{
			ph.getInstance().sendMessage("this command need player target");
			return false;
		}
		
		return true;
	}
}
