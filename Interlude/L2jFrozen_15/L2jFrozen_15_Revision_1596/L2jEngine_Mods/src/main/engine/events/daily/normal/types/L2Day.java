package main.engine.events.daily.normal.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2MonsterInstance;
import com.l2jfrozen.util.random.Rnd;

import main.concurrent.ThreadPool;
import main.data.properties.ConfigData;
import main.engine.events.daily.AbstractEvent;
import main.enums.TeamType;
import main.holders.LocationHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilMessage;
import main.util.UtilSpawn;

/**
 * @author fissban
 */
public class L2Day extends AbstractEvent
{
	// Npc
	private final static int EVENT_HUMAN = 12260;
	private final static int EVENT_ELF = 12261;
	private final static int EVENT_DARK_ELF = 12262;
	private final static int EVENT_DWARF = 12263;
	private final static int EVENT_ORC = 12264;
	// Html
	private final static String HTML_PATH = "data/html/events/l2day/";
	// Letters
	private final static int LETTER_A = 3875;
	private final static int LETTER_C = 3876;
	private final static int LETTER_E = 3877;
	private final static int LETTER_F = 3878;
	private final static int LETTER_G = 3879;
	private final static int LETTER_H = 3880;
	private final static int LETTER_I = 3881;
	private final static int LETTER_L = 3882;
	private final static int LETTER_N = 3883;
	private final static int LETTER_O = 3884;
	private final static int LETTER_R = 3885;
	private final static int LETTER_S = 3886;
	private final static int LETTER_T = 3887;
	private final static int LETTER_II = 3888;
	
	private static final Map<Integer, String> TALKERS = new HashMap<>();
	static
	{
		TALKERS.put(EVENT_HUMAN, "event_human001.htm");
		TALKERS.put(EVENT_ELF, "event_elf001.htm");
		TALKERS.put(EVENT_DARK_ELF, "event_darkelf001.htm");
		TALKERS.put(EVENT_DWARF, "event_dwarf001.htm");
		TALKERS.put(EVENT_ORC, "event_orc001.htm");
	}
	
	private static final List<LocationHolder> SPAWNS_HUMAN = new ArrayList<>();
	static
	{
		SPAWNS_HUMAN.add(new LocationHolder(-13073, 122801, -3117, 0)); // gludio08_npc1921_lc01
		SPAWNS_HUMAN.add(new LocationHolder(-13949, 121934, -2988, 32768)); // gludio08_npc1921_lc02
		SPAWNS_HUMAN.add(new LocationHolder(-14822, 123708, -3117, 8192)); // gludio08_npc1921_lc03
		SPAWNS_HUMAN.add(new LocationHolder(18178, 145149, -3054, 7400)); // dion09_npc2022_lc01
		SPAWNS_HUMAN.add(new LocationHolder(19208, 144380, -3097, 32768)); // dion09_npc2022_lc02
		SPAWNS_HUMAN.add(new LocationHolder(19508, 145775, -3086, 48000)); // dion09_npc2022_lc03
		SPAWNS_HUMAN.add(new LocationHolder(17396, 170259, -3507, 30000)); // dion10_npc2023_lc01
		SPAWNS_HUMAN.add(new LocationHolder(79806, 55570, -1560, 0)); // oren17_npc2219_lc01
		SPAWNS_HUMAN.add(new LocationHolder(83328, 55824, -1525, 32768)); // oren17_npc2219_lc02
		SPAWNS_HUMAN.add(new LocationHolder(80986, 54504, -1525, 32768)); // oren17_npc2219_lc03
		SPAWNS_HUMAN.add(new LocationHolder(83332, 149160, -3405, 49152)); // giran11_npc2222_lc01
		SPAWNS_HUMAN.add(new LocationHolder(82277, 148598, -3467, 0)); // giran11_npc2222_lc02
		SPAWNS_HUMAN.add(new LocationHolder(81621, 148725, -3467, 32768)); // giran11_npc2222_lc03
		SPAWNS_HUMAN.add(new LocationHolder(81680, 145656, -3533, 32768)); // giran11_npc2222_lc04
		SPAWNS_HUMAN.add(new LocationHolder(-80762, 151118, -3043, 28672)); // gludio06_npc1722_lc01
		SPAWNS_HUMAN.add(new LocationHolder(-84049, 150176, -3129, 4096)); // gludio06_npc1722_lc02
		SPAWNS_HUMAN.add(new LocationHolder(-82623, 151666, -3129, 49152)); // gludio06_npc1722_lc03
		SPAWNS_HUMAN.add(new LocationHolder(117498, 76630, -2695, 38000)); // aden14_npc2320_lc01
		SPAWNS_HUMAN.add(new LocationHolder(115914, 76449, -2711, 59000)); // aden14_npc2320_lc02
		SPAWNS_HUMAN.add(new LocationHolder(119536, 76988, -2275, 40960)); // aden14_npc2320_lc03
		SPAWNS_HUMAN.add(new LocationHolder(-84516, 242971, -3730, 34000)); // gludio25_npc1725_lc01
		SPAWNS_HUMAN.add(new LocationHolder(-86003, 243205, -3730, 60000)); // gludio24_npc1725_lc02
		SPAWNS_HUMAN.add(new LocationHolder(147120, 27312, -2192, 40960)); // aden13_npc2418_lc01
		SPAWNS_HUMAN.add(new LocationHolder(147920, 25664, -2000, 16384)); // aden13_npc2418_lc02
		SPAWNS_HUMAN.add(new LocationHolder(147888, -58048, -2979, 49000)); // godard02_npc2416_lc01
		SPAWNS_HUMAN.add(new LocationHolder(147285, -56461, -2776, 11500)); // godard02_npc2416_lc02
		SPAWNS_HUMAN.add(new LocationHolder(44176, -48732, -800, 33000)); // rune02_npc2116_lc01
		SPAWNS_HUMAN.add(new LocationHolder(44294, -47642, -792, 50000)); // rune02_npc2116_lc02
	}
	
	private static final List<LocationHolder> SPAWNS_ELF = new ArrayList<>();
	static
	{
		SPAWNS_ELF.add(new LocationHolder(47151, 49436, -3059, 32000)); // oren04_npc2119_lc01
		SPAWNS_ELF.add(new LocationHolder(44122, 50784, -3059, 57344)); // oren04_npc2119_lc01
		SPAWNS_ELF.add(new LocationHolder(111776, 221104, -3543, 16384)); // innadril09_npc2324_lc01
		SPAWNS_ELF.add(new LocationHolder(107904, 218096, -3675, 0)); // innadril09_npc2324_lc02
		SPAWNS_ELF.add(new LocationHolder(114920, 220020, -3632, 32768)); // innadril09_npc2324_lc03
	}
	
	private static final List<LocationHolder> SPAWNS_DARKELF = new ArrayList<>();
	static
	{
		SPAWNS_DARKELF.add(new LocationHolder(11281, 15652, -4584, 25000)); // oren09_npc2018_lc01
		SPAWNS_DARKELF.add(new LocationHolder(11303, 17732, -4574, 57344)); // oren09_npc2018_lc02
	}
	
	private static final List<LocationHolder> SPAWNS_DWARF = new ArrayList<>();
	static
	{
		SPAWNS_DWARF.add(new LocationHolder(114733, -178691, -821, 0)); // schuttgart03_npc2312_lc01
		SPAWNS_DWARF.add(new LocationHolder(115708, -182362, -1449, 0)); // schuttgart03_npc2312_lc02
	}
	
	private static final List<LocationHolder> SPAWNS_ORC = new ArrayList<>();
	static
	{
		SPAWNS_ORC.add(new LocationHolder(-44337, -113669, -224, 0)); // schuttgart03_npc2312_lc01
		SPAWNS_ORC.add(new LocationHolder(-44628, -115409, -240, 22500)); // schuttgart03_npc2312_lc02
	}
	
	// Message
	private static final String MESSAGE_START = "L2Day Event: Collect Letters and trade them for prizes at Event Cats in every town! Hurry up, since the event will end at Feb 28th!";
	
	private static final List<NpcHolder> npcs = new ArrayList<>();
	
	public L2Day()
	{
		registerEvent(true, ConfigData.HEAVY_MEDALS_DATE_START, ConfigData.HEAVY_MEDALS_DATE_END);
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
					// Generate Spawns
					SPAWNS_HUMAN.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_HUMAN, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_ELF.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_ELF, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_DARKELF.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_DARK_ELF, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_DWARF.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_DWARF, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_ORC.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_ORC, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_HUMAN.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_HUMAN, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
					SPAWNS_HUMAN.forEach(loc -> npcs.add(UtilSpawn.npc(EVENT_HUMAN, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
				}, 20000);
				
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
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (Util.areObjectType(L2MonsterInstance.class, victim))
		{
			int itemId = 0;
			
			int chance = Rnd.get(100);
			if (chance < 10)
			{
				itemId = LETTER_A;
			}
			else if (chance < 10)
			{
				
			}
		}
	}
}
