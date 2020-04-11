package services.community;

import java.io.Serializable;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import actions.RewardListInfo;
import l2f.gameserver.Config;
import l2f.gameserver.cache.ImagesCache;
import l2f.gameserver.data.htm.HtmCache;
import l2f.gameserver.data.xml.holder.NpcHolder;
import l2f.gameserver.handler.bbs.CommunityBoardManager;
import l2f.gameserver.handler.bbs.ICommunityBoardHandler;
import l2f.gameserver.instancemanager.RaidBossSpawnManager;
import l2f.gameserver.model.GameObjectsStorage;
import l2f.gameserver.model.Player;
import l2f.gameserver.model.entity.olympiad.Olympiad;
import l2f.gameserver.model.instances.NpcInstance;
import l2f.gameserver.network.serverpackets.RadarControl;
import l2f.gameserver.network.serverpackets.ShowBoard;
import l2f.gameserver.scripts.ScriptFile;
import l2f.gameserver.taskmanager.AutoImageSenderManager;
import l2f.gameserver.templates.StatsSet;
import l2f.gameserver.templates.npc.MinionData;
import l2f.gameserver.templates.npc.NpcTemplate;
import l2f.gameserver.utils.Util;

public class CommunityBosses implements ScriptFile, ICommunityBoardHandler
{
	private static final Logger _log = LoggerFactory.getLogger(CommunityBosses.class);

	private static final int BOSSES_PER_PAGE = 10;
	private static final int[] BOSSES_TO_NOT_SHOW = { 29006,//Core
			29001,//Queen Ant
			29014,//Orfen
			25692,//Aenkinel
			25423,//Fairy Queen Timiniel
			25010,//Furious Thieles
			25532,//Kechi
			25119,//Messenger of Fairy Queen Berun
			25159,//Paniel the Unicorn
			25163,//Roaring Skylancer
			25070, //Enchanted Forest Watcher Ruell
			25603, //Darion
			25544 //Tully
	};

	@Override
	public void onLoad()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
		{
			_log.info("CommunityBoard: Bosses loaded.");
			CommunityBoardManager.getInstance().registerHandler(this);
		}
	}

	@Override
	public void onReload()
	{
		if(Config.COMMUNITYBOARD_ENABLED)
			CommunityBoardManager.getInstance().removeHandler(this);
	}

	@Override
	public void onShutdown()
	{}

	@Override
	public String[] getBypassCommands()
	{
		return new String[] { "_bbsmemo", "_bbsbosslist", "_bbsboss" };
	}

	@Override
	public void onBypassCommand(Player player, String bypass)
	{
		StringTokenizer st = new StringTokenizer(bypass, "_");
		String cmd = st.nextToken();
		player.setSessionVar("add_fav", null);

		if("bbsmemo".equals(cmd) || "bbsbosslist".equals(cmd))//_bbsbosslist_sort_page_search
		{
			int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "1");
			int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
			String search = st.hasMoreTokens() ? st.nextToken().trim() : "";

			sendBossListPage(player, getSortByIndex(sort), page, search);
		}
		else if("bbsboss".equals(cmd))//_bbsboss_sort_page_search_rbId_btn
		{
			int sort = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "3");
			int page = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");
			String search = st.hasMoreTokens() ? st.nextToken().trim() : "";
			int bossId = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "25044");
			int buttonClick = Integer.parseInt(st.hasMoreTokens() ? st.nextToken() : "0");

			manageButtons(player, buttonClick, bossId);

			sendBossDetails(player, getSortByIndex(sort), page, search, bossId);
		}
	}

	/**
	 * Showing list of bosses in Community Board with their Name, Level, Status and Show Details button
	 * @param player guy that will receive list
	 * @param sort index of the sorting type
	 * @param page number of the page(Starting from 0)
	 * @param search word in Name of the boss
	 */
	private static void sendBossListPage(Player player, SortType sort, int page, String search)
	{
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "bbs_boss_list.htm", player);

		Map<Integer, StatsSet> allBosses = getSearchedBosses(sort, search);
		Map<Integer, StatsSet> bossesToShow = getBossesToShow(allBosses, page);
		boolean isThereNextPage = allBosses.size() > bossesToShow.size();

		html = getBossListReplacements(html, page, bossesToShow, isThereNextPage);

		html = getNormalReplacements(html, page, sort, search, -1);
		ShowBoard.separateAndSend(html, player);
	}

	/**
	 * Replacing %x% words in bbs_bbslink_list.htm file
	 * @param html existing file
	 * @param page number of the page(Starting from 0)
	 * @param allBosses Map<BossId, BossStatsSet> of bosses that will be shown
	 * @param nextPage Is the next page?
	 * @return ready HTML
	 */
	private static String getBossListReplacements(String html, int page, Map<Integer, StatsSet> allBosses, boolean nextPage)
	{
		String newHtml = html;

		int i = 0;

		for(Entry<Integer, StatsSet> entry : allBosses.entrySet())
		{
			StatsSet boss = entry.getValue();
			NpcTemplate temp = NpcHolder.getInstance().getTemplate(entry.getKey().intValue());

			boolean isAlive = isBossAlive(boss);

			newHtml = newHtml.replace("<?name_" + i + "?>", temp.getName());
			newHtml = newHtml.replace("<?level_" + i + "?>", String.valueOf(temp.level));
			newHtml = newHtml.replace("<?status_" + i + "?>", isAlive ? "Alive" : getRespawnTime(boss));
			newHtml = newHtml.replace("<?status_color_" + i + "?>", getTextColor(isAlive));
			newHtml = newHtml.replace("<?bp_" + i + "?>", "<button value=\"show\" action=\"bypass _bbsboss_<?sort?>_" + page + "_ <?search?> _" + entry.getKey() + "\" width=40 height=12 back=\"L2UI_CT1.ListCTRL_DF_Title_Down\" fore=\"L2UI_CT1.ListCTRL_DF_Title\">");
			i++;
		}

		for(int j = i; j < BOSSES_PER_PAGE; j++)
		{
			newHtml = newHtml.replace("<?name_" + j + "?>", "...");
			newHtml = newHtml.replace("<?level_" + j + "?>", "...");
			newHtml = newHtml.replace("<?status_" + j + "?>", "...");
			newHtml = newHtml.replace("<?status_color_" + j + "?>", "FFFFFF");
			newHtml = newHtml.replace("<?bp_" + j + "?>", "...");

		}

		newHtml = newHtml.replace("<?previous?>", page > 0 ? "<button action=\"bypass _bbsbosslist_<?sort?>_" + (page - 1) + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_prev_down\" fore=\"L2UI_CH3.shortcut_prev\">" : "<br>");
		newHtml = newHtml.replace("<?next?>", nextPage && i == BOSSES_PER_PAGE ? "<button action=\"bypass _bbsbosslist_<?sort?>_" + (page + 1) + "_<?search?>\" width=16 height=16 back=\"L2UI_CH3.shortcut_next_down\" fore=\"L2UI_CH3.shortcut_next\">" : "<br>");
		newHtml = newHtml.replace("<?pages?>", String.valueOf(page + 1));

		return newHtml;
	}

	/**
	 * Getting all bosses to show(checking only page)
	 * @param page number of the page(Starting from 0)
	 * @return Bosses
	 */
	private static Map<Integer, StatsSet> getBossesToShow(Map<Integer, StatsSet> allBosses, int page)
	{
		Map<Integer, StatsSet> bossesToShow = new LinkedHashMap<Integer, StatsSet>();
		int i = 0;
		for(Entry<Integer, StatsSet> entry : allBosses.entrySet())
		{
			if(i < page * BOSSES_PER_PAGE)
			{
				i++;
			}
			else
			{
				StatsSet boss = entry.getValue();
				NpcTemplate temp = NpcHolder.getInstance().getTemplate(entry.getKey().intValue());
				if(boss != null && temp != null)
				{
					i++;
					bossesToShow.put(entry.getKey(), entry.getValue());
					if(i > (page * BOSSES_PER_PAGE + BOSSES_PER_PAGE - 1)){ return bossesToShow; }
				}
			}
		}
		return bossesToShow;
	}

	/**
	 * Showing detailed info about Boss in Community Board. Including name, level, status, stats, image
	 * @param player guy that will receive details
	 * @param sort index of the sorting type
	 * @param page number of the page(Starting from 0)
	 * @param search word in Name of the boss
	 * @param bossId Id of the boss to show
	 */
	private static void sendBossDetails(Player player, SortType sort, int page, CharSequence search, int bossId)
	{
		String html = HtmCache.getInstance().getNotNull(Config.BBS_HOME_DIR + "bbs_boss_details.htm", player);
		StatsSet bossSet = RaidBossSpawnManager.getInstance().getAllBosses().get(bossId);

		if(bossSet == null)
		{
			ShowBoard.separateAndSend(html, player);
			return;
		}

		NpcTemplate bossTemplate = NpcHolder.getInstance().getTemplate(bossId);
		NpcInstance bossInstance = getAliveBoss(bossId);

		html = getDetailedBossReplacements(html, bossSet, bossTemplate, bossInstance);
		html = getNormalReplacements(html, page, sort, search, bossId);

		if(!AutoImageSenderManager.isImageAutoSendable(bossId))
			ImagesCache.getInstance().sendImageToPlayer(player, bossId);

		ShowBoard.separateAndSend(html, player);
	}

	/**
	 * Managing buttons that were clicking in Boss Details page
	 * @param player that clicked button
	 * @param buttonIndex 1: Showing Location of the boss. 2: Showing Drops
	 * @param bossId Id of the boss that player was looking into
	 */
	private static void manageButtons(Player player, int buttonIndex, int bossId)
	{
		switch(buttonIndex)
		{
			case 1://Show Location
				RaidBossSpawnManager.showBossLocation(player, bossId);
				break;
			case 2://Show Drops
				if(Config.ALLOW_DROP_CALCULATOR)
					RewardListInfo.showInfo(player, NpcHolder.getInstance().getTemplate(bossId), true, false, 1.0);
				break;
			case 3://Go to Boss
				if(!player.isInZonePeace() || Olympiad.isRegistered(player))
				{
					player.sendMessage("You can do it only in safe zone!");
					return;
				}
				NpcInstance aliveInstance = getAliveBoss(bossId);
				if(aliveInstance != null)
					player.teleToLocation(aliveInstance.getLoc());
				else
					player.sendMessage("Boss isn't alive!");
			case 4://Show Location
				player.sendPacket(new RadarControl(2, 2, 0, 0, 0));
		}
	}

	/**
	 * Replacing all %a% words by real Values in Detailed Boss Page
	 * @param html current Html
	 * @param bossSet StatsSet of the boss
	 * @param bossTemplate NpcTemplate of the boss
	 * @param bossInstance any Instance of the boss(can be null)
	 * @return filled HTML
	 */
	private static String getDetailedBossReplacements(String html, StatsSet bossSet, NpcTemplate bossTemplate, NpcInstance bossInstance)
	{
		String newHtml = html;

		boolean isAlive = isBossAlive(bossSet);

		newHtml = newHtml.replace("<?name?>", bossTemplate.getName());
		newHtml = newHtml.replace("<?level?>", String.valueOf(bossTemplate.level));
		newHtml = newHtml.replace("<?status?>", isAlive ? "Alive" : getRespawnTime(bossSet));
		newHtml = newHtml.replace("<?status_color?>", getTextColor(isAlive));
		newHtml = newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));

		newHtml = newHtml.replace("<?currentHp?>", Util.formatAdena((int) (bossInstance != null ? (int) bossInstance.getCurrentHp() : 0)));
		newHtml = newHtml.replace("<?maxHp?>", Util.formatAdena((int) bossTemplate.baseHpMax));
		newHtml = newHtml.replace("<?minions?>", String.valueOf(getMinionsCount(bossTemplate)));

		return newHtml;
	}

	/**
	 * Replacing page, sorts, bossId, search
	 * @param html to fill
	 * @param page number
	 * @param sort type
	 * @param search word
	 * @param bossId If of the boss, set -1 if doesn't matter
	 * @return new Html page
	 */
	private static String getNormalReplacements(String html, int page, SortType sort, CharSequence search, int bossId)
	{
		String newHtml = html;
		newHtml = newHtml.replace("<?page?>", String.valueOf(page));
		newHtml = newHtml.replace("<?sort?>", String.valueOf(sort.index));
		newHtml = newHtml.replace("<?bossId?>", String.valueOf(bossId));
		newHtml = newHtml.replace("<?search?>", search);

		for(int i = 1; i <= 6; i++)
		{
			if(Math.abs(sort.index) == i)
				newHtml = newHtml.replace("<?sort" + i + "?>", String.valueOf(-sort.index));
			else
				newHtml = newHtml.replace("<?sort" + i + "?>", String.valueOf(i));
		}

		return newHtml;
	}

	private static boolean isBossAlive(StatsSet set)
	{
		return (long) set.getInteger("respawn_delay", 0) < System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(1L);
	}

	private static String getRespawnTime(StatsSet set)
	{
		if(set.getInteger("respawn_delay", 0) < System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(1L))
			return "isAlive";
		
		long delay = set.getInteger("respawn_delay", 0)-(System.currentTimeMillis() / TimeUnit.SECONDS.toMillis(1L));

		//System.out.println(delay);
		int hours = (int) (delay / 60 / 60);
		int mins = (int) ((delay - (hours * 60 * 60)) / 60);
		int secs = (int) ((delay - ((hours * 60 * 60) + (mins * 60))));
		
		String Strhours = hours < 10 ? "0"+hours : ""+hours;
		String Strmins = mins < 10 ? "0"+mins : ""+mins;
		String Strsecs = secs < 10 ? "0"+secs : ""+secs;

		return "<font color=\"b02e31\">"+Strhours+":"+Strmins+":"+Strsecs+"</font>";
	}	
	
	/**
	 * Getting alive and visible instance of the bossId
	 * @param bossId Id of the boss
	 * @return Instance of the boss
	 */
	private static NpcInstance getAliveBoss(int bossId)
	{
		List<NpcInstance> instances = GameObjectsStorage.getAllByNpcId(bossId, true, true);
		return instances.isEmpty() ? null : instances.get(0);
	}

	private static int getMinionsCount(NpcTemplate template)
	{
		int minionsCount = 0;
		for(MinionData minion : template.getMinionData())
			minionsCount += minion.getAmount();
		return minionsCount;
	}

	private static String getTextColor(boolean alive)
	{
		if(alive)
			return "259a30";//"327b39";
		else
			return "b02e31";//"8f3d3f";
	}

	/**
	 * Getting List of Bosses that player is looking for(including sort and search)
	 * @param sort Type of sorting he want to use
	 * @param search word that he is looking for
	 * @return Map of Bosses
	 */
	private static Map<Integer, StatsSet> getSearchedBosses(SortType sort, String search)
	{
		Map<Integer, StatsSet> result = getBossesMapBySearch(search);

		for(int id : BOSSES_TO_NOT_SHOW)
			result.remove(id);

		result = sortResults(result, sort);

		return result;
	}

	/**
	 * Getting List of Bosses that player is looking for(including search)
	 * @param search String that boss Name needs to contains(can be Empty)
	 * @return MapMap of Bosses
	 */
	private static Map<Integer, StatsSet> getBossesMapBySearch(String search)
	{
		Map<Integer, StatsSet> finalResult = new HashMap<Integer, StatsSet>();
		if(search.isEmpty())
		{
			finalResult = RaidBossSpawnManager.getInstance().getAllBosses();
		}
		else
		{
			for(Entry<Integer, StatsSet> entry : RaidBossSpawnManager.getInstance().getAllBosses().entrySet())
			{
				NpcTemplate temp = NpcHolder.getInstance().getTemplate(entry.getKey().intValue());
				if(StringUtils.containsIgnoreCase(temp.getName(), search))
					finalResult.put(entry.getKey(), entry.getValue());
			}
		}
		return finalResult;
	}

	/**
	 * Sorting results by sort type
	 * @param result map to sort
	 * @param sort type
	 * @return sorted Map
	 */
	private static Map<Integer, StatsSet> sortResults(Map<Integer, StatsSet> result, SortType sort)
	{
		ValueComparator bvc = new ValueComparator(result, sort);
		Map<Integer, StatsSet> sortedMap = new TreeMap<Integer, StatsSet>(bvc);
		sortedMap.putAll(result);
		return sortedMap;
	}

	/**
	 * Comparator of Bosses
	 */
	private static class ValueComparator implements Comparator<Integer>, Serializable
	{
		private static final long serialVersionUID = 4782405190873267622L;
		private final Map<Integer, StatsSet> base;
		private final SortType sortType;

		private ValueComparator(Map<Integer, StatsSet> base, SortType sortType)
		{
			this.base = base;
			this.sortType = sortType;
		}

		@Override
		public int compare(Integer o1, Integer o2)
		{
			int sortResult = sortById(o1, o2, sortType);
			if(sortResult == 0 && !o1.equals(o2) && Math.abs(sortType.index) != 1)
				sortResult = sortById(o1, o2, SortType.NAME_ASC);
			return sortResult;
		}

		/**
		 * Comparing a and b but sorting
		 * @param a first variable
		 * @param b second variable
		 * @param sorting type of sorting
		 * @return result of comparing
		 */
		private int sortById(Integer a, Integer b, SortType sorting)
		{
			NpcTemplate temp1 = NpcHolder.getInstance().getTemplate(a.intValue());
			NpcTemplate temp2 = NpcHolder.getInstance().getTemplate(b.intValue());
			StatsSet set1 = base.get(a);
			StatsSet set2 = base.get(b);
			switch(sorting)
			{
				case NAME_ASC:
					return temp1.getName().compareTo(temp2.getName());
				case NAME_DESC:
					return temp2.getName().compareTo(temp1.getName());
				case LEVEL_ASC:
					return Integer.compare(temp1.level, temp2.level);
				case LEVEL_DESC:
					return Integer.compare(temp2.level, temp1.level);
				case STATUS_ASC:
					return Integer.compare(set1.getInteger("respawn_delay", 0), set2.getInteger("respawn_delay", 0));
				case STATUS_DESC:
					return Integer.compare(set2.getInteger("respawn_delay", 0), set1.getInteger("respawn_delay", 0));
			}
			return 0;
		}
	}

	private enum SortType
	{
		NAME_ASC(1),
		NAME_DESC(-1),
		LEVEL_ASC(2),
		LEVEL_DESC(-2),
		STATUS_ASC(3),
		STATUS_DESC(-3);

		public final int index;

		SortType(int index)
		{
			this.index = index;
		}
	}

	/**
	 * Getting SortType by index
	 * @param i index
	 * @return SortType
	 */
	private static SortType getSortByIndex(int i)
	{
		for(SortType type : SortType.values())
			if(type.index == i)
				return type;
		return SortType.NAME_ASC;
	}

	@Override
	public void onWriteCommand(Player player, String bypass, String arg1, String arg2, String arg3, String arg4, String arg5)
	{}
}