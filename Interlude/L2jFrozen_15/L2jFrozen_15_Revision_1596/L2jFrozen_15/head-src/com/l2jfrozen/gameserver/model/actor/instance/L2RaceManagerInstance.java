package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.idfactory.IdFactory;
import com.l2jfrozen.gameserver.model.actor.knownlist.RaceManagerKnownList;
import com.l2jfrozen.gameserver.model.entity.MonsterRace;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.DeleteObject;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.MonRaceInfo;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.gameserver.util.Broadcast;

public class L2RaceManagerInstance extends L2NpcInstance
{
	public static final int LANES = 8;
	public static final int WINDOW_START = 0;
	
	// private static List<Race> history;
	private static List<L2RaceManagerInstance> managers;
	protected static int raceNumber = 4;
	
	// Time Constants
	private final static long SECOND = 1000;
	private final static long MINUTE = 60 * SECOND;
	
	private static int minutes = 5;
	
	// States
	private static final int ACCEPTING_BETS = 0;
	private static final int WAITING = 1;
	private static final int STARTING_RACE = 2;
	private static final int RACE_END = 3;
	private static int state = RACE_END;
	
	protected static final int[][] CODES =
	{
		{
			-1,
			0
		},
		{
			0,
			15322
		},
		{
			13765,
			-1
		}
	};
	private static boolean notInitialized = true;
	protected static MonRaceInfo packet;
	protected static final int COST[] =
	{
		100,
		500,
		1000,
		5000,
		10000,
		20000,
		50000,
		100000
	};
	
	public L2RaceManagerInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
		getKnownList(); // init knownlist
		if (notInitialized)
		{
			notInitialized = false;
			managers = new ArrayList<>();
			
			final ThreadPoolManager s = ThreadPoolManager.getInstance();
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), 0, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), 30 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE), MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE), MINUTE + 30 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 2 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 3 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 4 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 5 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES), 6 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_TICKET_SALES_CLOSED), 7 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_MINUTES), 7 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_MINUTES), 8 * MINUTE, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_30_SECONDS), 8 * MINUTE + 30 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_COUNTDOWN_IN_FIVE_SECONDS), 8 * MINUTE + 50 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 55 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 56 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 57 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 58 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS), 8 * MINUTE + 59 * SECOND, 10 * MINUTE);
			s.scheduleGeneralAtFixedRate(new Announcement(SystemMessageId.MONSRACE_RACE_START), 9 * MINUTE, 10 * MINUTE);
		}
		managers.add(this);
	}
	
	@Override
	public final RaceManagerKnownList getKnownList()
	{
		if (super.getKnownList() == null || !(super.getKnownList() instanceof RaceManagerKnownList))
		{
			setKnownList(new RaceManagerKnownList(this));
		}
		return (RaceManagerKnownList) super.getKnownList();
	}
	
	class Announcement implements Runnable
	{
		private final SystemMessageId type;
		
		public Announcement(final SystemMessageId pType)
		{
			type = pType;
		}
		
		@Override
		public void run()
		{
			makeAnnouncement(type);
		}
	}
	
	public void makeAnnouncement(final SystemMessageId type)
	{
		SystemMessage sm = new SystemMessage(type);
		switch (type.getId())
		{
			case 816: // SystemMessageId.MONSRACE_TICKETS_AVAILABLE_FOR_S1_RACE
			case 817: // SystemMessageId.MONSRACE_TICKETS_NOW_AVAILABLE_FOR_S1_RACE
				if (state != ACCEPTING_BETS)
				{// LOGGER.info("Race Initializing");
					state = ACCEPTING_BETS;
					startRace();
				} // else{LOGGER.info("Race open");}
				sm.addNumber(raceNumber);
				break;
			case 818: // SystemMessageId.MONSRACE_TICKETS_STOP_IN_S1_MINUTES
			case 820: // SystemMessageId.MONSRACE_BEGINS_IN_S1_MINUTES
			case 823: // SystemMessageId.MONSRACE_BEGINS_IN_S1_SECONDS
				sm.addNumber(minutes);
				sm.addNumber(raceNumber);
				minutes--;
				break;
			case 819: // SystemMessageId.MONSRACE_TICKET_SALES_CLOSED
				// LOGGER.info("Sales closed");
				sm.addNumber(raceNumber);
				state = WAITING;
				minutes = 2;
				break;
			case 822: // SystemMessageId.MONSRACE_COUNTDOWN_IN_FIVE_SECONDS
			case 825: // SystemMessageId.MONSRACE_RACE_END
				sm.addNumber(raceNumber);
				minutes = 5;
				break;
			case 826: // SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2
				// LOGGER.info("Placing");
				state = RACE_END;
				sm.addNumber(MonsterRace.getInstance().getFirstPlace());
				sm.addNumber(MonsterRace.getInstance().getSecondPlace());
				break;
		}
		// LOGGER.info("Counter: "+minutes);
		// LOGGER.info("State: "+state);
		broadcast(sm);
		sm = null;
		// LOGGER.info("Player's known: "+getKnownPlayers().size());
		
		if (type == SystemMessageId.MONSRACE_RACE_START)
		{
			// LOGGER.info("Starting race");
			state = STARTING_RACE;
			startRace();
			minutes = 5;
		}
	}
	
	protected void broadcast(final L2GameServerPacket pkt)
	{
		for (final L2RaceManagerInstance manager : managers)
		{
			if (!manager.isDead())
			{
				Broadcast.toKnownPlayers(manager, pkt);
			}
		}
	}
	
	public void sendMonsterInfo()
	{
		broadcast(packet);
	}
	
	private void startRace()
	{
		MonsterRace race = MonsterRace.getInstance();
		if (state == STARTING_RACE)
		{
			// state++;
			PlaySound SRace = new PlaySound(1, "S_Race", 0, 0, 0, 0, 0);
			broadcast(SRace);
			SRace = null;
			PlaySound SRace2 = new PlaySound(0, "ItemSound2.race_start", 1, 121209259, 12125, 182487, -3559);
			broadcast(SRace2);
			SRace2 = null;
			packet = new MonRaceInfo(CODES[1][0], CODES[1][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
			
			ThreadPoolManager.getInstance().scheduleGeneral(new RunRace(), 5000);
		}
		else
		{
			// state++;
			race.newRace();
			race.newSpeeds();
			packet = new MonRaceInfo(CODES[0][0], CODES[0][1], race.getMonsters(), race.getSpeeds());
			sendMonsterInfo();
		}
		race = null;
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, String command)
	{
		if (command.startsWith("BuyTicket") && state != ACCEPTING_BETS)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.MONSRACE_TICKETS_NOT_AVAILABLE));
			command = "Chat 0";
		}
		if (command.startsWith("ShowOdds") && state == ACCEPTING_BETS)
		{
			player.sendPacket(new SystemMessage(SystemMessageId.MONSRACE_NO_PAYOUT_INFO));
			command = "Chat 0";
		}
		
		if (command.startsWith("BuyTicket"))
		{
			int val = Integer.parseInt(command.substring(10));
			if (val == 0)
			{
				player.setRace(0, 0);
				player.setRace(1, 0);
			}
			if (val == 10 && player.getRace(0) == 0 || val == 20 && player.getRace(0) == 0 && player.getRace(1) == 0)
			{
				val = 0;
			}
			showBuyTicket(player, val);
		}
		else if (command.equals("ShowOdds"))
		{
			showOdds(player);
		}
		else if (command.equals("ShowInfo"))
		{
			showMonsterInfo(player);
		}
		else if (command.equals("calculateWin"))
		{
			// displayCalculateWinnings(player);
		}
		else if (command.equals("viewHistory"))
		{
			// displayHistory(player);
		}
		else
		{
			// getKnownList().removeKnownObject(player);
			super.onBypassFeedback(player, command);
		}
	}
	
	public void showOdds(final L2PcInstance player)
	{
		if (state == ACCEPTING_BETS)
		{
			return;
		}
		final int npcId = getTemplate().npcId;
		String filename, search;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		filename = getHtmlPath(npcId, 5);
		html.setFile(filename);
		for (int i = 0; i < 8; i++)
		{
			final int n = i + 1;
			search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		html.replace("1race", String.valueOf(raceNumber));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		search = null;
		filename = null;
		html = null;
	}
	
	public void showMonsterInfo(final L2PcInstance player)
	{
		final int npcId = getTemplate().npcId;
		String filename, search;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		filename = getHtmlPath(npcId, 6);
		html.setFile(filename);
		for (int i = 0; i < 8; i++)
		{
			final int n = i + 1;
			search = "Mob" + n;
			html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
		}
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		search = null;
		filename = null;
		html = null;
	}
	
	public void showBuyTicket(final L2PcInstance player, final int val)
	{
		if (state != ACCEPTING_BETS)
		{
			return;
		}
		final int npcId = getTemplate().npcId;
		SystemMessage sm;
		String filename, search, replace;
		NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		if (val < 10)
		{
			filename = getHtmlPath(npcId, 2);
			html.setFile(filename);
			for (int i = 0; i < 8; i++)
			{
				final int n = i + 1;
				search = "Mob" + n;
				html.replace(search, MonsterRace.getInstance().getMonsters()[i].getTemplate().name);
			}
			search = "No1";
			if (val == 0)
			{
				html.replace(search, "");
			}
			else
			{
				html.replace(search, "" + val);
				player.setRace(0, val);
			}
		}
		else if (val < 20)
		{
			if (player.getRace(0) == 0)
			{
				return;
			}
			filename = getHtmlPath(npcId, 3);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			search = "Mob1";
			replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			if (val == 10)
			{
				html.replace(search, "");
			}
			else
			{
				html.replace(search, "" + COST[val - 11]);
				player.setRace(1, val - 10);
			}
		}
		else if (val == 20)
		{
			if (player.getRace(0) == 0 || player.getRace(1) == 0)
			{
				return;
			}
			filename = getHtmlPath(npcId, 4);
			html.setFile(filename);
			html.replace("0place", "" + player.getRace(0));
			search = "Mob1";
			replace = MonsterRace.getInstance().getMonsters()[player.getRace(0) - 1].getTemplate().name;
			html.replace(search, replace);
			search = "0adena";
			final int price = COST[player.getRace(1) - 1];
			html.replace(search, "" + price);
			search = "0tax";
			final int tax = 0;
			html.replace(search, "" + tax);
			search = "0total";
			final int total = price + tax;
			html.replace(search, "" + total);
		}
		else
		{
			if (player.getRace(0) == 0 || player.getRace(1) == 0)
			{
				return;
			}
			final int ticket = player.getRace(0);
			final int priceId = player.getRace(1);
			if (!player.reduceAdena("Race", COST[priceId - 1], this, true))
			{
				return;
			}
			player.setRace(0, 0);
			player.setRace(1, 0);
			sm = new SystemMessage(SystemMessageId.ACQUIRED);
			sm.addNumber(raceNumber);
			sm.addItemName(4443);
			player.sendPacket(sm);
			L2ItemInstance item = new L2ItemInstance(IdFactory.getInstance().getNextId(), 4443);
			item.setCount(1);
			item.setEnchantLevel(raceNumber);
			item.setCustomType1(ticket);
			item.setCustomType2(COST[priceId - 1] / 100);
			player.getInventory().addItem("Race", item, player, this);
			InventoryUpdate iu = new InventoryUpdate();
			iu.addItem(item);
			final L2ItemInstance adenaupdate = player.getInventory().getItemByItemId(57);
			iu.addModifiedItem(adenaupdate);
			player.sendPacket(iu);
			iu = null;
			item = null;
			return;
		}
		html.replace("1race", String.valueOf(raceNumber));
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
		player.sendPacket(ActionFailed.STATIC_PACKET);
		
		sm = null;
		html = null;
		filename = null;
		search = null;
		replace = null;
	}
	
	public class Race
	{
		private final Info[] info;
		
		public Race(final Info[] pInfo)
		{
			info = pInfo;
		}
		
		public Info getLaneInfo(final int lane)
		{
			return info[lane];
		}
		
		public class Info
		{
			private final int id;
			private final int place;
			private final int odds;
			private final int payout;
			
			public Info(final int pId, final int pPlace, final int pOdds, final int pPayout)
			{
				id = pId;
				place = pPlace;
				odds = pOdds;
				payout = pPayout;
			}
			
			public int getId()
			{
				return id;
			}
			
			public int getOdds()
			{
				return odds;
			}
			
			public int getPayout()
			{
				return payout;
			}
			
			public int getPlace()
			{
				return place;
			}
		}
		
	}
	
	class RunRace implements Runnable
	{
		@Override
		public void run()
		{
			packet = new MonRaceInfo(CODES[2][0], CODES[2][1], MonsterRace.getInstance().getMonsters(), MonsterRace.getInstance().getSpeeds());
			sendMonsterInfo();
			ThreadPoolManager.getInstance().scheduleGeneral(new RunEnd(), 30000);
		}
	}
	
	class RunEnd implements Runnable
	{
		@Override
		public void run()
		{
			makeAnnouncement(SystemMessageId.MONSRACE_FIRST_PLACE_S1_SECOND_S2);
			makeAnnouncement(SystemMessageId.MONSRACE_RACE_END);
			raceNumber++;
			
			DeleteObject obj = null;
			for (int i = 0; i < 8; i++)
			{
				obj = new DeleteObject(MonsterRace.getInstance().getMonsters()[i]);
				broadcast(obj);
			}
		}
	}
	
}
