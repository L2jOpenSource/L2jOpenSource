package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.awt.Color;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;

import net.sf.l2j.commons.lang.StringUtil;
import net.sf.l2j.commons.math.MathUtil;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.data.manager.BuyListManager;
import net.sf.l2j.gameserver.data.xml.ItemData;
import net.sf.l2j.gameserver.data.xml.NpcData;
import net.sf.l2j.gameserver.data.xml.WalkerRouteData;
import net.sf.l2j.gameserver.enums.ScriptEventType;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.enums.skills.ElementType;
import net.sf.l2j.gameserver.enums.skills.SkillType;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.MercenaryManagerNpc;
import net.sf.l2j.gameserver.model.actor.instance.Merchant;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.buylist.NpcBuyList;
import net.sf.l2j.gameserver.model.buylist.Product;
import net.sf.l2j.gameserver.model.item.DropData;
import net.sf.l2j.gameserver.model.item.kind.Item;
import net.sf.l2j.gameserver.model.location.Location;
import net.sf.l2j.gameserver.model.location.WalkerLocation;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.ExServerPrimitive;
import net.sf.l2j.gameserver.network.serverpackets.NpcHtmlMessage;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.skills.L2Skill;

public class AdminNpc implements IAdminCommandHandler
{
	private static final int PAGE_LIMIT = 7;
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_npcinfo",
		"admin_show_shop",
		"admin_show_shoplist",
		"admin_walker"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		st.nextToken();
		
		if (command.startsWith("admin_npcinfo"))
		{
			// You need to target a Monster.
			final WorldObject target = activeChar.getTarget();
			if (!(target instanceof Npc))
			{
				activeChar.sendPacket(SystemMessageId.INCORRECT_TARGET);
				return false;
			}
			
			final Npc npc = (Npc) target;
			if (!st.hasMoreTokens())
				sendGeneralInfos(activeChar, npc);
			else
			{
				final String subCommand = st.nextToken();
				switch (subCommand)
				{
					case "ai":
						sendAiInfos(activeChar, npc);
						break;
					
					case "drop":
					case "spoil":
						try
						{
							final int page = (st.hasMoreTokens()) ? Integer.parseInt(st.nextToken()) : 1;
							
							sendDropInfos(activeChar, npc, page, subCommand.equalsIgnoreCase("drop"));
						}
						catch (Exception e)
						{
							sendDropInfos(activeChar, npc, 1, true);
						}
						break;
					
					case "skill":
						sendSkillInfos(activeChar, npc);
						break;
					
					case "spawn":
						sendSpawnInfos(activeChar, npc);
						break;
					
					case "stat":
						sendStatsInfos(activeChar, npc);
						break;
					
					default:
						sendGeneralInfos(activeChar, npc);
				}
			}
		}
		else if (command.startsWith("admin_show_shoplist"))
		{
			try
			{
				showShopList(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_shoplist <list_id>");
			}
		}
		else if (command.startsWith("admin_show_shop"))
		{
			try
			{
				showShop(activeChar, Integer.parseInt(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //show_shop <npc_id>");
			}
		}
		else if (command.startsWith("admin_walker"))
		{
			// More tokens, we try to check parameter.
			if (st.hasMoreTokens())
			{
				try
				{
					final int npcId = Integer.parseInt(st.nextToken());
					final List<WalkerLocation> route = WalkerRouteData.getInstance().getWalkerRoute(npcId);
					if (route == null)
					{
						activeChar.sendMessage("The npcId " + npcId + " isn't linked to any WalkerRoute.");
						return false;
					}
					
					final ExServerPrimitive esp = activeChar.getMove().getDebugMovePacket();
					esp.reset();
					
					// Draw the path.
					for (int i = 0; i < route.size(); i++)
					{
						final int nextIndex = i + 1;
						esp.addLine("Segment #" + nextIndex, Color.ORANGE, true, route.get(i), (nextIndex == route.size()) ? route.get(0) : route.get(nextIndex));
					}
					activeChar.sendPacket(esp);
					
				}
				catch (Exception e)
				{
					activeChar.sendMessage("Usage: //walker <npc_id>");
				}
			}
			
			// Send HTM no matter what.
			sendWalkerInfos(activeChar);
		}
		
		return true;
	}
	
	/**
	 * Send to the {@link Player} all <b>AI</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 */
	private static void sendAiInfos(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/admin/npcinfo/ai.htm");
		
		if (npc.hasAI())
		{
			html.replace("%ai_intention%", "<font color=\"LEVEL\">Intention</font><table width=\"100%\"><tr><td><font color=\"LEVEL\">Intention:</font></td><td>" + npc.getAI().getCurrentIntention().getType().name() + "</td></tr>");
			html.replace("%ai%", "<tr><td><font color=\"LEVEL\">AI:</font></td><td>" + npc.getAI().getClass().getSimpleName() + "</td></tr></table><br>");
		}
		else
		{
			html.replace("%ai_intention%", "");
			html.replace("%ai%", "");
		}
		
		html.replace("%ai_type%", npc.getTemplate().getAiType().name());
		html.replace("%ai_clan%", (npc.getTemplate().getClans() != null) ? "<tr><td><font color=\"LEVEL\">Clan:</font></td><td>" + Arrays.toString(npc.getTemplate().getClans()) + " " + npc.getTemplate().getClanRange() + "</td></tr>" + ((npc.getTemplate().getIgnoredIds() != null) ? "<tr><td><font color=\"LEVEL\">Ignored ids:</font></td><td>" + Arrays.toString(npc.getTemplate().getIgnoredIds()) + "</td></tr>" : "") : "<tr><td>This NPC got no clan informations.</td></tr>");
		html.replace("%ai_move%", String.valueOf(npc.getTemplate().canMove()));
		html.replace("%ai_seed%", String.valueOf(npc.getTemplate().isSeedable()));
		html.replace("%ai_ssinfo%", npc.getCurrentSsCount() + "[" + npc.getTemplate().getSsCount() + "] - " + npc.getTemplate().getSsRate() + "%");
		html.replace("%ai_spsinfo%", npc.getCurrentSpsCount() + "[" + npc.getTemplate().getSpsCount() + "] - " + npc.getTemplate().getSpsRate() + "%");
		html.replace("%aggro%", npc.getTemplate().getAggroRange());
		html.replace("%enchant%", npc.getTemplate().getEnchantEffect());
		
		final StringBuilder sb = new StringBuilder(500);
		
		if (npc.getTemplate().getEventQuests().isEmpty())
			sb.append("This NPC isn't affected by scripts.");
		else
		{
			ScriptEventType type = null; // Used to see if we moved of type.
			
			// For any type of EventType
			for (Map.Entry<ScriptEventType, List<Quest>> entry : npc.getTemplate().getEventQuests().entrySet())
			{
				if (type != entry.getKey())
				{
					type = entry.getKey();
					StringUtil.append(sb, "<br><font color=\"LEVEL\">", type.name(), "</font><br1>");
				}
				
				for (Quest quest : entry.getValue())
					StringUtil.append(sb, quest.getName(), "<br1>");
			}
		}
		html.replace("%script%", sb.toString());
		
		player.sendPacket(html);
	}
	
	/**
	 * Send to the {@link Player} all <b>DROPS</b> or <b>SPOILS</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 * @param page : The current page we are checking.
	 * @param isDrop : If true, we check drops only. If false, we check spoils.
	 */
	private static void sendDropInfos(Player player, Npc npc, int page, boolean isDrop)
	{
		List<DropData> list = (isDrop) ? npc.getTemplate().getAllDropData() : npc.getTemplate().getAllSpoilData();
		
		// Load static Htm.
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/npcinfo/drop.htm");
		
		if (list.isEmpty())
		{
			html.replace("%drop%", "<tr><td>This NPC has no " + ((isDrop) ? "drops" : "spoils") + ".</td></tr>");
			player.sendPacket(html);
			return;
		}
		
		final int max = MathUtil.countPagesNumber(list.size(), PAGE_LIMIT);
		if (page > max)
			page = max;
		
		list = list.subList((page - 1) * PAGE_LIMIT, Math.min(page * PAGE_LIMIT, list.size()));
		
		final StringBuilder sb = new StringBuilder(2000);
		
		int row = 0;
		for (DropData drop : list)
		{
			sb.append(((row % 2) == 0 ? "<table width=\"280\" bgcolor=\"000000\"><tr>" : "<table width=\"280\"><tr>"));
			
			final double chance = Math.min(100, (((drop.getItemId() == 57) ? drop.getChance() * Config.RATE_DROP_ADENA : drop.getChance() * Config.RATE_DROP_ITEMS) / 10000));
			final Item item = ItemData.getInstance().getTemplate(drop.getItemId());
			
			String name = item.getName();
			if (name.startsWith("Recipe: "))
				name = "R: " + name.substring(8);
			
			if (name.length() >= 45)
				name = name.substring(0, 42) + "...";
			
			StringUtil.append(sb, "<td width=34 height=34><img src=icon.noimage width=32 height=32></td>");
			StringUtil.append(sb, "<td width=246 height=34>", name, "<br1><font color=B09878>", ((isDrop) ? "Drop" : "Spoil"), ": ", chance, "% Min: ", drop.getMinDrop(), " Max: ", drop.getMaxDrop(), "</font></td>");
			
			sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
			row++;
		}
		
		// Build page footer.
		sb.append("<br><img src=\"L2UI.SquareGray\" width=277 height=1><table width=\"100%\" bgcolor=000000><tr>");
		
		if (page > 1)
			StringUtil.append(sb, "<td align=left width=70><a action=\"bypass admin_npcinfo ", ((isDrop) ? "drop" : "spoil"), " ", page - 1, "\">Previous</a></td>");
		else
			StringUtil.append(sb, "<td align=left width=70>Previous</td>");
		
		StringUtil.append(sb, "<td align=center width=100>Page ", page, "</td>");
		
		if (page < max)
			StringUtil.append(sb, "<td align=right width=70><a action=\"bypass admin_npcinfo ", ((isDrop) ? "drop" : "spoil"), " ", page + 1, "\">Next</a></td>");
		else
			StringUtil.append(sb, "<td align=right width=70>Next</td>");
		
		sb.append("</tr></table><img src=\"L2UI.SquareGray\" width=277 height=1>");
		
		html.replace("%drop%", sb.toString());
		player.sendPacket(html);
	}
	
	/**
	 * Send to the {@link Player} all <b>GENERAL</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 */
	public static void sendGeneralInfos(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/admin/npcinfo/general.htm");
		
		html.replace("%class%", npc.getClass().getSimpleName());
		html.replace("%id%", npc.getTemplate().getNpcId());
		html.replace("%lvl%", npc.getTemplate().getLevel());
		html.replace("%name%", npc.getName());
		html.replace("%race%", npc.getTemplate().getRace().toString());
		html.replace("%tmplid%", npc.getTemplate().getIdTemplate());
		html.replace("%script%", npc.getScriptValue());
		html.replace("%castle%", (npc.getCastle() != null) ? npc.getCastle().getName() : "none");
		html.replace("%clanhall%", (npc.getClanHall() != null) ? npc.getClanHall().getName() : "none");
		html.replace("%siegablehall%", (npc.getSiegableHall() != null) ? npc.getSiegableHall().getName() : "none");
		html.replace("%shop%", ((npc instanceof Merchant || npc instanceof MercenaryManagerNpc) ? "<button value=\"Shop\" action=\"bypass -h admin_show_shop " + npc.getNpcId() + "\" width=65 height=19 back=\"L2UI_ch3.smallbutton2_over\" fore=\"L2UI_ch3.smallbutton2\">" : ""));
		
		player.sendPacket(html);
	}
	
	/**
	 * Send to the {@link Player} all <b>SPAWN</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 */
	private static void sendSpawnInfos(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/admin/npcinfo/spawn.htm");
		
		html.replace("%loc%", npc.getX() + " " + npc.getY() + " " + npc.getZ());
		html.replace("%dist%", (int) player.distance3D(npc));
		html.replace("%corpse%", StringUtil.getTimeStamp(npc.getTemplate().getCorpseTime()));
		
		if (npc.getSpawn() != null)
		{
			html.replace("%spawn%", npc.getSpawn().getLoc().toString());
			html.replace("%loc2d%", (int) npc.distance2D(npc.getSpawn().getLoc()));
			html.replace("%loc3d%", (int) npc.distance3D(npc.getSpawn().getLoc()));
			html.replace("%resp%", StringUtil.getTimeStamp(npc.getSpawn().getRespawnDelay()));
			html.replace("%rand_resp%", StringUtil.getTimeStamp(npc.getSpawn().getRespawnRandom()));
		}
		else
		{
			html.replace("%spawn%", "<font color=FF0000>null</font>");
			html.replace("%loc2d%", "<font color=FF0000>--</font>");
			html.replace("%loc3d%", "<font color=FF0000>--</font>");
			html.replace("%resp%", "<font color=FF0000>--</font>");
			html.replace("%rand_resp%", "<font color=FF0000>--</font>");
		}
		
		final StringBuilder sb = new StringBuilder(500);
		
		if (npc instanceof Monster)
		{
			final Monster monster = (Monster) npc;
			
			// Monster is a minion, deliver boss state.
			final Monster master = monster.getMaster();
			if (master != null)
			{
				html.replace("%type%", "minion");
				StringUtil.append(sb, "<tr><td><font color=", ((master.isDead()) ? "FF4040>" : "6161FF>"), master.toString(), "</td></tr>");
			}
			// Monster is a master, find back minions informations.
			else if (monster.hasMinions())
			{
				html.replace("%type%", "master");
				
				for (Entry<Monster, Boolean> data : monster.getMinionList().getMinions().entrySet())
					StringUtil.append(sb, "<tr><td><font color=", ((data.getValue()) ? "6161FF>" : "FF4040>"), data.getKey().toString(), "</td></tr>");
			}
			// Monster isn't anything.
			else
				html.replace("%type%", "regular monster");
		}
		else
			html.replace("%type%", "regular NPC");
		
		html.replace("%minion%", sb.toString());
		
		player.sendPacket(html);
	}
	
	/**
	 * Send to the {@link Player} all <b>STATS</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 */
	private static void sendStatsInfos(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/admin/npcinfo/stat.htm");
		
		html.replace("%hp%", (int) npc.getCurrentHp());
		html.replace("%hpmax%", npc.getMaxHp());
		html.replace("%mp%", (int) npc.getCurrentMp());
		html.replace("%mpmax%", npc.getMaxMp());
		html.replace("%patk%", npc.getPAtk(null));
		html.replace("%matk%", npc.getMAtk(null, null));
		html.replace("%pdef%", npc.getPDef(null));
		html.replace("%mdef%", npc.getMDef(null, null));
		html.replace("%accu%", npc.getAccuracy());
		html.replace("%evas%", npc.getEvasionRate(null));
		html.replace("%crit%", npc.getCriticalHit(null, null));
		html.replace("%rspd%", npc.getMoveSpeed());
		html.replace("%aspd%", npc.getPAtkSpd());
		html.replace("%cspd%", npc.getMAtkSpd());
		html.replace("%str%", npc.getSTR());
		html.replace("%dex%", npc.getDEX());
		html.replace("%con%", npc.getCON());
		html.replace("%int%", npc.getINT());
		html.replace("%wit%", npc.getWIT());
		html.replace("%men%", npc.getMEN());
		html.replace("%ele_fire%", npc.getStat().getDefenseElementValue(ElementType.FIRE));
		html.replace("%ele_water%", npc.getStat().getDefenseElementValue(ElementType.WATER));
		html.replace("%ele_wind%", npc.getStat().getDefenseElementValue(ElementType.WIND));
		html.replace("%ele_earth%", npc.getStat().getDefenseElementValue(ElementType.EARTH));
		html.replace("%ele_holy%", npc.getStat().getDefenseElementValue(ElementType.HOLY));
		html.replace("%ele_dark%", npc.getStat().getDefenseElementValue(ElementType.DARK));
		
		player.sendPacket(html);
	}
	
	/**
	 * Send to the {@link Player} all <b>SKILLS</b> informations regarding one {@link Npc}.
	 * @param player : The Player used as reference.
	 * @param npc : The Npc used as reference.
	 */
	private static void sendSkillInfos(Player player, Npc npc)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(npc.getObjectId());
		html.setFile("data/html/admin/npcinfo/skill.htm");
		
		if (npc.getTemplate().getSkills().isEmpty())
		{
			html.replace("%skill%", "This NPC doesn't hold any skill.");
			player.sendPacket(html);
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		
		NpcSkillType type = null; // Used to see if we moved of type.
		
		// For any type of SkillType
		for (Map.Entry<NpcSkillType, List<L2Skill>> entry : npc.getTemplate().getSkills().entrySet())
		{
			if (type != entry.getKey())
			{
				type = entry.getKey();
				StringUtil.append(sb, "<br><font color=\"LEVEL\">", type.name(), "</font><br1>");
			}
			
			for (L2Skill skill : entry.getValue())
				StringUtil.append(sb, ((skill.getSkillType() == SkillType.NOTDONE) ? ("<font color=\"777777\">" + skill.getName() + "</font>") : skill.getName()), " [", skill.getId(), "-", skill.getLevel(), "]<br1>");
		}
		
		html.replace("%skill%", sb.toString());
		player.sendPacket(html);
	}
	
	private static void showShopList(Player activeChar, int listId)
	{
		final NpcBuyList buyList = BuyListManager.getInstance().getBuyList(listId);
		if (buyList == null)
		{
			activeChar.sendMessage("BuyList template is unknown for id: " + listId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><body><center><font color=\"LEVEL\">", NpcData.getInstance().getTemplate(buyList.getNpcId()).getName(), " (", buyList.getNpcId(), ") buylist id: ", buyList.getListId(), "</font></center><br><table width=\"100%\"><tr><td width=200>Item</td><td width=80>Price</td></tr>");
		
		for (Product product : buyList.getProducts())
			StringUtil.append(sb, "<tr><td>", product.getItem().getName(), "</td><td>", product.getPrice(), "</td></tr>");
		
		sb.append("</table></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void showShop(Player activeChar, int npcId)
	{
		final List<NpcBuyList> buyLists = BuyListManager.getInstance().getBuyListsByNpcId(npcId);
		if (buyLists.isEmpty())
		{
			activeChar.sendMessage("No buyLists found for id: " + npcId + ".");
			return;
		}
		
		final StringBuilder sb = new StringBuilder(500);
		StringUtil.append(sb, "<html><title>Merchant Shop Lists</title><body>");
		
		if (activeChar.getTarget() instanceof Merchant)
		{
			Npc merchant = (Npc) activeChar.getTarget();
			int taxRate = merchant.getCastle().getTaxPercent();
			
			StringUtil.append(sb, "<center><font color=\"LEVEL\">", merchant.getName(), " (", npcId, ")</font></center><br>Tax rate: ", taxRate, "%");
		}
		
		StringUtil.append(sb, "<table width=\"100%\">");
		
		for (NpcBuyList buyList : buyLists)
			StringUtil.append(sb, "<tr><td><a action=\"bypass -h admin_show_shoplist ", buyList.getListId(), " 1\">Buylist id: ", buyList.getListId(), "</a></td></tr>");
		
		StringUtil.append(sb, "</table></body></html>");
		
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setHtml(sb.toString());
		activeChar.sendPacket(html);
	}
	
	private static void sendWalkerInfos(Player activeChar)
	{
		final NpcHtmlMessage html = new NpcHtmlMessage(0);
		html.setFile("data/html/admin/walker.htm");
		
		final StringBuilder sb = new StringBuilder(500);
		
		for (Entry<Integer, List<WalkerLocation>> entry : WalkerRouteData.getInstance().getWalkerRoutes().entrySet())
		{
			final Location initialLoc = entry.getValue().get(0);
			final String teleLoc = initialLoc.toString().replaceAll(",", "");
			
			StringUtil.append(sb, "<tr><td width=180>NpcId: ", entry.getKey(), " - Path size: ", entry.getValue().size(), "</td><td width=50><a action=\"bypass admin_move_to ", teleLoc, "\">Tele. To</a></td><td width=50 align=right><a action=\"bypass admin_walker ", entry.getKey(), "\">Show</a></td></tr>");
		}
		
		html.replace("%routes%", sb.toString());
		activeChar.sendPacket(html);
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}