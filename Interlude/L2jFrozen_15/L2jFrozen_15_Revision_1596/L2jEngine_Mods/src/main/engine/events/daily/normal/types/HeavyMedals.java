package main.engine.events.daily.normal.types;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.util.random.Rnd;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.daily.AbstractEvent;
import main.enums.PlaySoundType;
import main.enums.SocialActionType;
import main.enums.TeamType;
import main.holders.LocationHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.UtilSpawn;
import main.util.builders.html.HtmlBuilder;

/**
 * @author fissban
 * @author Reynald0
 */
public class HeavyMedals extends AbstractEvent
{
	// Time in seconds
	private static int TRAVEL_TIME = 240;
	// Skill
	private final static int SKILL_ID = 67;
	// Npc
	private final static int CAT_ROY = 70435;
	private final static int CAT_WINNIE = 70436;
	private final static int CAT_WENDY = 70437;
	// Rewards
	private final static int EVENT_GLITTERING_MEDAL = 6393;
	private final static int EVENT_MEDAL = 6392;
	// Html
	private final static String HTML_PATH = "data/html/engine/events/heavymedals/";
	// Win chance
	private final static byte WIN_CHANCE = 50;
	
	private final static int[] MEDALS =
	{
		5,
		10,
		20,
		40
	};
	// Badges
	private final static int BADGE_OF_RABBIT = 6399;
	private final static int BADGE_OF_HYENA = 6400;
	private final static int BADGE_OF_FOX = 6401;
	private final static int BADGE_OF_WOLF = 6402;
	// Spawns
	private static final List<LocationHolder> SPAWNS_WENDY = new ArrayList<>();
	{
		SPAWNS_WENDY.add(new LocationHolder(-14472, 126597, -3141));// Gludio
		SPAWNS_WENDY.add(new LocationHolder(147213, 31920, -2481));// Aden
		SPAWNS_WENDY.add(new LocationHolder(21515, 145670, -3141));// Dion
		SPAWNS_WENDY.add(new LocationHolder(42538, -46610, -798));// Rune
		SPAWNS_WENDY.add(new LocationHolder(81106, 52966, -1560));// Oren
		SPAWNS_WENDY.add(new LocationHolder(147535, -59528, -2982));// Goddard
	}
	
	private static final List<LocationHolder> SPAWNS_WINNIE = new ArrayList<>();
	{
		SPAWNS_WINNIE.add(new LocationHolder(-44595, -113628, -199, 0));
		SPAWNS_WINNIE.add(new LocationHolder(-44671, -115437, -240, 22500));
		SPAWNS_WINNIE.add(new LocationHolder(-13073, 122841, -3117, 0));
		SPAWNS_WINNIE.add(new LocationHolder(-13972, 121893, -2988, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(-14843, 123710, -3117, 8192));
		SPAWNS_WINNIE.add(new LocationHolder(11327, 15682, -4584, 25000));
		SPAWNS_WINNIE.add(new LocationHolder(11243, 17712, -4574, 57344));
		SPAWNS_WINNIE.add(new LocationHolder(18154, 145192, -3054, 7400));
		SPAWNS_WINNIE.add(new LocationHolder(19214, 144327, -3097, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(19459, 145775, -3086, 48000));
		SPAWNS_WINNIE.add(new LocationHolder(17418, 170217, -3507, 36000));
		SPAWNS_WINNIE.add(new LocationHolder(47146, 49382, -3059, 32000));
		SPAWNS_WINNIE.add(new LocationHolder(44157, 50827, -3059, 57344));
		SPAWNS_WINNIE.add(new LocationHolder(79798, 55629, -1560, 0));
		SPAWNS_WINNIE.add(new LocationHolder(83328, 55769, -1525, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(83304, 149176, -3400, 0));
		SPAWNS_WINNIE.add(new LocationHolder(80986, 54452, -1525, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(81691, 145610, -3467, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(114719, -178742, -821, 0));
		SPAWNS_WINNIE.add(new LocationHolder(115708, -182422, -1449, 0));
		SPAWNS_WINNIE.add(new LocationHolder(-80731, 151152, -3043, 28672));
		SPAWNS_WINNIE.add(new LocationHolder(-84097, 150171, -3129, 4096));
		SPAWNS_WINNIE.add(new LocationHolder(-82678, 151666, -3129, 49152));
		SPAWNS_WINNIE.add(new LocationHolder(117459, 76664, -2695, 38000));
		SPAWNS_WINNIE.add(new LocationHolder(115936, 76488, -2711, 59000));
		SPAWNS_WINNIE.add(new LocationHolder(119576, 76940, -2275, 40960));
		SPAWNS_WINNIE.add(new LocationHolder(-84516, 243015, -3730, 34000));
		SPAWNS_WINNIE.add(new LocationHolder(-86031, 243153, -3730, 60000));
		SPAWNS_WINNIE.add(new LocationHolder(147124, 27401, -2192, 40960));
		SPAWNS_WINNIE.add(new LocationHolder(147985, 25664, -2000, 16384));
		SPAWNS_WINNIE.add(new LocationHolder(111724, 221111, -3543, 16384));
		SPAWNS_WINNIE.add(new LocationHolder(107899, 218149, -3675, 0));
		SPAWNS_WINNIE.add(new LocationHolder(114920, 220080, -3632, 32768));
		SPAWNS_WINNIE.add(new LocationHolder(147924, -58052, -2979, 49000));
		SPAWNS_WINNIE.add(new LocationHolder(147285, -56461, -2776, 33000));
		SPAWNS_WINNIE.add(new LocationHolder(44176, -48688, -800, 33000));
		SPAWNS_WINNIE.add(new LocationHolder(44294, -47642, -792, 50000));
	}
	
	private static final List<LocationHolder> SPAWNS_ROY = new ArrayList<>();
	{
		SPAWNS_ROY.add(new LocationHolder(-44599, -113576, -199, 0));
		SPAWNS_ROY.add(new LocationHolder(-44628, -115409, -240, 22500));
		SPAWNS_ROY.add(new LocationHolder(-13073, 122801, -3117, 0));
		SPAWNS_ROY.add(new LocationHolder(-13949, 121934, -2988, 32768));
		SPAWNS_ROY.add(new LocationHolder(-14786, 123686, -3117, 8192));
		SPAWNS_ROY.add(new LocationHolder(11281, 15652, -4584, 25000));
		SPAWNS_ROY.add(new LocationHolder(11303, 17732, -4574, 57344));
		SPAWNS_ROY.add(new LocationHolder(18178, 145149, -3054, 7400));
		SPAWNS_ROY.add(new LocationHolder(19208, 144380, -3097, 32768));
		SPAWNS_ROY.add(new LocationHolder(19508, 145775, -3086, 48000));
		SPAWNS_ROY.add(new LocationHolder(17396, 170259, -3507, 36000));
		SPAWNS_ROY.add(new LocationHolder(47151, 49436, -3059, 32000));
		SPAWNS_ROY.add(new LocationHolder(44122, 50784, -3059, 57344));
		SPAWNS_ROY.add(new LocationHolder(79806, 55570, -1560, 0));
		SPAWNS_ROY.add(new LocationHolder(83328, 55824, -1525, 32768));
		SPAWNS_ROY.add(new LocationHolder(83304, 149224, -3400, 0));
		SPAWNS_ROY.add(new LocationHolder(80986, 54504, -1525, 32768));
		SPAWNS_ROY.add(new LocationHolder(81680, 145656, -3467, 32768));
		SPAWNS_ROY.add(new LocationHolder(114733, -178691, -821, 0));
		SPAWNS_ROY.add(new LocationHolder(115708, -182362, -1449, 0));
		SPAWNS_ROY.add(new LocationHolder(-80789, 151073, -3043, 28672));
		SPAWNS_ROY.add(new LocationHolder(-84049, 150176, -3129, 4096));
		SPAWNS_ROY.add(new LocationHolder(-82623, 151666, -3129, 49152));
		SPAWNS_ROY.add(new LocationHolder(117498, 76630, -2695, 38000));
		SPAWNS_ROY.add(new LocationHolder(115914, 76449, -2711, 59000));
		SPAWNS_ROY.add(new LocationHolder(119536, 76988, -2275, 40960));
		SPAWNS_ROY.add(new LocationHolder(-84516, 242971, -3730, 34000));
		SPAWNS_ROY.add(new LocationHolder(-86003, 243205, -3730, 60000));
		SPAWNS_ROY.add(new LocationHolder(147184, 27405, -2192, 17000));
		SPAWNS_ROY.add(new LocationHolder(147920, 25664, -2000, 16384));
		SPAWNS_ROY.add(new LocationHolder(111776, 221104, -3543, 16384));
		SPAWNS_ROY.add(new LocationHolder(107904, 218096, -3675, 0));
		SPAWNS_ROY.add(new LocationHolder(114920, 220020, -3632, 32768));
		SPAWNS_ROY.add(new LocationHolder(147888, -58048, -2979, 49000));
		SPAWNS_ROY.add(new LocationHolder(147262, -56450, -2776, 33000));
		SPAWNS_ROY.add(new LocationHolder(44176, -48732, -800, 33000));
		SPAWNS_ROY.add(new LocationHolder(44319, -47640, -792, 50000));
	}
	// Message
	private static final String MESSAGE_START = ConfigData.HEAVY_MEDALS_MESSAGE_START;
	// Npc instances
	private static List<NpcHolder> npcs = new ArrayList<>();
	
	public HeavyMedals()
	{
		registerEvent(ConfigData.ENABLE_HeavyMedals, ConfigData.HEAVY_MEDALS_DATE_START, ConfigData.HEAVY_MEDALS_DATE_END);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				// send message
				UtilMessage.sendAnnounceMsg(MESSAGE_START, L2World.getInstance().getAllPlayers());
				// add npc spawns
				ThreadPool.schedule(() ->
				{
					SPAWNS_ROY.forEach(loc -> npcs.add(UtilSpawn.npc(CAT_ROY, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_WINNIE.forEach(loc -> npcs.add(UtilSpawn.npc(CAT_WINNIE, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
				}, 20000);
				
				startTimer("spawn_wendy", TRAVEL_TIME * 1000, null, null, true);
				break;
			case END:
				// remove npc spawns
				npcs.stream().filter(npc -> npc.getInstance() != null).forEach(npc -> npc.getInstance().deleteMe());
				npcs.clear();
				break;
		}
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		UtilMessage.sendAnnounceMsg(MESSAGE_START, ph);
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (Util.areObjectType(L2NpcInstance.class, character))
		{
			L2NpcInstance npc = (L2NpcInstance) character.getInstance();
			switch (npc.getNpcId())
			{
				case CAT_WINNIE:
					sendHtmlFile(ph, npc, HTML_PATH + CAT_WINNIE + ".htm");
					return true;
				case CAT_ROY:
					sendHtmlFile(ph, npc, HTML_PATH + CAT_ROY + ".htm");
					return true;
			}
		}
		return false;
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (Util.areObjectType(L2Attackable.class, victim) && Util.checkLvlDifference(killer, victim, 9))
		{
			int chance = Rnd.get(100);
			
			int itemId = 0;
			if (chance < 5) // 5%
			{
				itemId = EVENT_GLITTERING_MEDAL;
			}
			else if (chance < 50) // 45%
			{
				itemId = EVENT_MEDAL;
			}
			
			if (itemId > 0)
			{
				if (Config.AUTO_LOOT)
				{
					UtilInventory.giveItems((PlayerHolder) killer, itemId, 1);
				}
				else
				{
					((L2MonsterInstance) victim.getInstance()).DropItem(killer.getInstance().getActingPlayer(), itemId, 1);
				}
			}
		}
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String event)
	{
		int level = 0;
		
		switch (event)
		{
			case "spawn_wendy":
			{
				// spawn location
				LocationHolder spawnLoc = SPAWNS_WENDY.get(Rnd.get(SPAWNS_WENDY.size()));
				// spawn generate
				NpcHolder spawnNpc = UtilSpawn.npc(CAT_WENDY, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ(), spawnLoc.getHeading(), 0, TRAVEL_TIME * 1000, TeamType.NONE, 0);
				// spawn animation
				spawnNpc.getInstance().broadcastPacket(new MagicSkillUser(spawnNpc.getInstance(), spawnNpc.getInstance(), SKILL_ID, 1, 1000, 1000));
				// spawn announcement
				String nearestTown = Util.getClosestTownName(spawnNpc.getInstance());
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "The " + spawnNpc.getInstance().getName() + " has spawned near " + nearestTown + "!");
				break;
			}
			case "game":
			{
				level = getLevel(ph);
				
				String html = HTML_PATH + "8229-game.htm";
				if (UtilInventory.getItemsCount(ph, EVENT_GLITTERING_MEDAL) < MEDALS[level])
				{
					html = HTML_PATH + "noMedal.htm";
				}
				
				sendHtmlFile(ph, (L2NpcInstance) npc.getInstance(), html);
				break;
			}
			case "Heads":
			case "Tails":
			{
				level = getLevel(ph);
				if (UtilInventory.getItemsCount(ph, EVENT_GLITTERING_MEDAL) < MEDALS[level])
				{
					sendHtmlFile(ph, (L2NpcInstance) npc.getInstance(), HTML_PATH + "noMedal.htm");
					return;
				}
				
				UtilInventory.takeItems(ph, EVENT_GLITTERING_MEDAL, MEDALS[level]);
				
				if (Rnd.get(100) <= WIN_CHANCE)
				{
					ph.getInstance().sendPacket(new SocialAction(ph.getInstance().getObjectId(), SocialActionType.SAD.ordinal()));
					ph.getInstance().sendPacket(new PlaySound(PlaySoundType.SYS_FISHING_FAILED.getName()));
					sendHtmlFile(ph, (L2NpcInstance) npc.getInstance(), HTML_PATH + "fail.htm");
				}
				else
				{
					switch (level)
					{
						case 0:
							UtilInventory.giveItems(ph, BADGE_OF_RABBIT, 1);
							break;
						case 1:
							UtilInventory.takeItems(ph, BADGE_OF_RABBIT, 1);
							UtilInventory.giveItems(ph, BADGE_OF_HYENA, 1);
							break;
						case 2:
							UtilInventory.takeItems(ph, BADGE_OF_HYENA, 1);
							UtilInventory.giveItems(ph, BADGE_OF_FOX, 1);
							break;
						case 3:
							UtilInventory.takeItems(ph, BADGE_OF_FOX, 1);
							UtilInventory.giveItems(ph, BADGE_OF_WOLF, 1);
							break;
					}
					ph.getInstance().sendPacket(new PlaySound(PlaySoundType.QUEST_JACKPOT.getName()));
					ph.getInstance().sendPacket(new SocialAction(ph.getInstance().getObjectId(), SocialActionType.VICTORY.ordinal()));
					
					HtmlBuilder html = new HtmlBuilder();
					html.append("<html><body>");
					html.append("Event Manager Winnie the Cat:<br>");
					html.append("Let's see...<br>");
					html.append(event, " it is, congratulations!!, you've reached <font color=LEVEL>", getStatus(ph), " level</font>!. Keep this mark with you to prove it.<br>");
					html.append("</body></html>");
					sendHtml((NpcHolder) npc, ph, html);
				}
				break;
			}
			case "talk":
			{
				level = getLevel(ph);
				sendHtmlFile(ph, (L2NpcInstance) npc.getInstance(), HTML_PATH + ((L2NpcInstance) npc.getInstance()).getNpcId() + "-lvl-" + String.valueOf(level) + ".htm");
				break;
			}
			case "info":
			{
				HtmlBuilder html = new HtmlBuilder();
				html.append("<html><body>");
				html.append("<font color=LEVEL>1 - Medals and Prizes</font><br>");
				html.append("Hunting the monsters that dwell outside the village will yield medals. Bring them to me and I'll reward you with a fabulous prize, personally selected for you by Collector Bashtal! You'll find two types of medals: Regular medals and glittering medals. Glittering medals are quite rare, and therefore most valuable, especially for your future.<br><br>");
				html.append("<font color=LEVEL>2 - Advancing Levels</font><br>");
				html.append("A collector's level reflects that player's contribution to the medal collection and collecting skill.");
				
				if (((L2NpcInstance) npc.getInstance()).getNpcId() == CAT_WINNIE)
				{
					html.append(" You should raise your level as high as you can! Meow!! <font color=LEVEL>To raise your level, just bring me glittering medals and pass a simple test.</font> Meow~!<br>");
				}
				else
				{
					html.append(" Meow! Higher levels are eligible for a greater selection of prizes.<font color=LEVEL> Winnie over there can tell you more about raising your level.</font> Meow~!<br>");
				}
				
				html.append("</body></html>");
				sendHtml((NpcHolder) npc, ph, html);
				break;
			}
			case "prizes":
			{
				HtmlBuilder html = new HtmlBuilder();
				html.append("<html><body>");
				html.append("These are the <font color=LEVEL>prizes</font> that were selected by my Lord Bashtal:<br>");
				switch (((NpcHolder) npc).getId())
				{
					case CAT_WENDY:
						html.append("<font color=LEVEL>Greater Haste Potion, Greater Swift Attack Potion, Greater Magic Haste Potion, Quick Healing Potion, Blessed Scroll of Escape, Blessed Scroll of Resurrection, Scroll: Enchant Weapon (All Grades), Red Party Mask.</font><br>");
						html.append("You'll don't need to raise your collector level to be eligible for some of these items.");
						break;
					
					case CAT_WINNIE:
						html.append("<font color=LEVEL>Greater Haste Potion, Greater Swift Attack Potion, Greater Magic Haste Potion, Quick Healing Potion, Blessed Scroll of Escape, Blessed Scroll of Resurrection, Scroll: Enchant Armor (All Grades), Scroll: Enchant Weapon (All Grades), Red Party Mask, Soul Crystal (All Colors) - Stage 11, Soul Crystal (All Colors) - Stage 12, Sealed Majestic Necklace, Sealed Majestic Earring, and Sealed Majestic Ring</font>. Whew! An impressive list, eh? Meow!<br>");
						html.append("I don't think I missed anything. <font color=LEVEL>Roy the Cat over there will trade medals for prizes.</font> Meow~!");
						break;
					
					case CAT_ROY:
						html.append("<font color=LEVEL>Greater Haste Potion, Greater Swift Attack Potion, Greater Magic Haste Potion, Quick Healing Potion, Blessed Scroll of Escape, Blessed Scroll of Resurrection, Scroll: Enchant Armor (All Grades), Scroll: Enchant Weapon (All Grades), Red Party Mask, Soul Crystal (All Colors) - Stage 11, Soul Crystal (All Colors) - Stage 12, Sealed Majestic Necklace, Sealed Majestic Earring, and Sealed Majestic Ring</font>.<br>");
						html.append("You'll need to raise your collector level to be eligible for some of these items. If you wish to raise your level, go see <font color=LEVEL>Winnie the Cat!</font>");
						break;
				}
				
				html.append("</body></html>");
				sendHtml((NpcHolder) npc, ph, html);
				break;
			}
		}
	}
	
	private byte getLevel(PlayerHolder ph)
	{
		if (UtilInventory.hasItems(ph, BADGE_OF_WOLF))
		{
			return 4;
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_FOX))
		{
			return 3;
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_HYENA))
		{
			return 2;
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_RABBIT))
		{
			return 1;
		}
		
		return 0;
	}
	
	private String getStatus(PlayerHolder ph)
	{
		if (UtilInventory.hasItems(ph, BADGE_OF_WOLF))
		{
			return "Wolf";
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_FOX))
		{
			return "Fox";
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_HYENA))
		{
			return "Hyena";
		}
		if (UtilInventory.hasItems(ph, BADGE_OF_RABBIT))
		{
			return "Rabbit";
		}
		
		return "Beginner";
	}
}
