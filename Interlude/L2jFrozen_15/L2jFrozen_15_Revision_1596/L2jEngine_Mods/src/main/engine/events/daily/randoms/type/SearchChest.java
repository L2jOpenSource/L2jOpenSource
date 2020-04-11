package main.engine.events.daily.randoms.type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.util.random.Rnd;

import main.data.memory.ObjectData;
import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.enums.TeamType;
import main.holders.LocationHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;
import main.packets.ObjectPosition;
import main.util.UtilMessage;
import main.util.UtilSpawn;
import main.util.builders.html.Html;
import main.util.builders.html.HtmlBuilder;

/**
 * @author fissban
 */
public class SearchChest extends AbstractMod
{
	private enum LocType
	{
		CEMETERY,
		// SILENT_VALLEY,
		// DEATH_PASS,
		// DRAGON_VALLEY,
		// TOWER_OF_INSOLENCE,
		BLAZING_SWAMP,
		// PLAINS_OF_GLORY,
		// BREKA_STRONGHOLD,
		// FIELD_OF_MASSACRE,
	}
	
	// List of zones
	public static final Map<LocType, List<LocationHolder>> LOCS = new LinkedHashMap<>();
	{
		// ------------------------------------------------------------------------
		LOCS.put(LocType.CEMETERY, new ArrayList<>());
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(173038, 20344, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(170281, 23058, -3457));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(169338, 18103, -3457));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172573, 15801, -3301));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(174954, 17307, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(178219, 17631, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180247, 15870, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182184, 15922, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182247, 18168, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182374, 22078, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182122, 24536, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180195, 24719, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175567, 24557, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172820, 24180, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(173917, 20912, -3255));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175751, 19843, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175323, 18839, -3247));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175230, 15314, -3243));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(170825, 15181, -3328));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(168212, 16912, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(167871, 18172, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(168794, 20340, -3328));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(169967, 24198, -3395));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172751, 25668, -3253));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175474, 24810, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(177211, 24014, -3420));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(178170, 23443, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179421, 25166, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180224, 23720, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179820, 22578, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179121, 22117, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179986, 21602, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181251, 22106, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181621, 23352, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181964, 24367, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182132, 25114, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183154, 23900, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182294, 22530, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181014, 22564, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181279, 21054, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181257, 19437, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180440, 19052, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182121, 18077, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182539, 18541, -3170));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183245, 18452, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183924, 19231, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183364, 16414, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182650, 15991, -3170));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181493, 15363, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180180, 15344, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(178402, 15078, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179180, 15281, -3177));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(176726, 15925, -3336));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(177149, 17220, -3420));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175571, 17951, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(174363, 18475, -3336));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171871, 18979, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171036, 19598, -3327));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(170890, 20789, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(170120, 22309, -3457));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(170983, 22171, -3395));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171394, 20875, -3212));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171007, 21483, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171440, 24495, -3395));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172104, 24134, -3328));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(169210, 19811, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(168556, 18411, -3395));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(169930, 18507, -3457));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(169613, 19402, -3394));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171117, 16206, -3395));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172004, 16206, -3329));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172038, 17460, -3328));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172203, 19074, -3328));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(171299, 19968, -3321));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172085, 20341, -3326));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(172634, 20329, -3331));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(173958, 19725, -3246));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(174799, 20857, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(174004, 20937, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175870, 20693, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(175149, 20684, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(174967, 21542, -3257));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(176037, 21546, -3252));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(177262, 25500, -3253));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(178466, 25548, -3248));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180237, 23448, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182341, 23417, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181524, 23405, -3171));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181410, 22384, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181980, 21141, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183594, 21089, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183684, 19418, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(183646, 18633, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(182832, 16645, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181463, 16732, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(181178, 15566, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180609, 16934, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179660, 16715, -3172));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(180312, 15884, -3170));
		LOCS.get(LocType.CEMETERY).add(new LocationHolder(179220, 15465, -3172));
		
		// ------------------------------------------------------------------------
		LOCS.put(LocType.BLAZING_SWAMP, new ArrayList<>());
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142085, -18114, -3231));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143236, -20535, -3163));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145300, -21378, -3176));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149386, -23931, -3432));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144639, -25477, -2155));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143405, -25055, -2066));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(148154, -20274, -3049));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(150048, -17955, -3044));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152342, -11796, -4483));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152425, -8279, -4492));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(151391, -7563, -4461));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(150069, -6352, -4376));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(147601, -5950, -4550));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146238, -7036, -4496));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143215, -8306, -4636));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(139472, -10414, -4656));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(137645, -12104, -4287));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(137864, -13163, -4294));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(139132, -13934, -4327));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(138927, -15927, -4299));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140554, -15782, -4422));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142994, -14669, -4439));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144522, -16152, -4147));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146336, -16757, -3777));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149626, -16429, -3091));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(151635, -15976, -2929));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153734, -14038, -3746));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(154818, -11801, -3994));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(154852, -9708, -4236));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(155657, -6926, -4085));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153094, -16560, -2955));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146155, -9388, -4446));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145330, -10950, -4447));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145132, -12112, -4434));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144651, -12501, -4378));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144333, -14525, -4343));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144012, -15937, -4259));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145288, -16169, -4025));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146727, -15983, -4062));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(147839, -15992, -4306));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(148698, -15210, -4450));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149801, -14660, -4434));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(150152, -14597, -4464));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(151305, -13845, -4516));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152095, -13914, -4458));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152413, -13002, -4457));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153099, -12243, -4505));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(147504, -17241, -3348));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(148583, -17219, -3094));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146317, -19304, -3442));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146425, -19299, -3443));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145537, -20635, -3158));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144771, -21653, -3136));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143060, -21486, -3151));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(141506, -19838, -3214));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(141690, -18305, -3211));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140768, -18034, -3173));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(139095, -15698, -4285));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(138352, -15226, -4280));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(137501, -13594, -4295));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(137396, -12341, -4175));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(138590, -10545, -4624));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(137535, -10498, -3955));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(138607, -9138, -4407));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140096, -8822, -4680));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140922, -9044, -4569));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140769, -8009, -4629));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140979, -6620, -4773));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(139267, -6912, -4759));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(139951, -5786, -4805));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(140010, -4901, -4780));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(141300, -5982, -4782));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142735, -5755, -4765));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142735, -5755, -4765));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143746, -5066, -4662));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142558, -4465, -4765));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143940, -4408, -4690));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145139, -4277, -4555));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(145965, -4587, -4504));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(146551, -5802, -4503));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(147661, -6197, -4398));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(148820, -5439, -4539));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149097, -6689, -4424));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149307, -7918, -4380));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(151157, -7614, -4485));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(151892, -6289, -4368));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152241, -7053, -4473));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153694, -7964, -4483));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(154275, -7484, -4472));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153906, -8368, -4482));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(153284, -9849, -4445));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152906, -10818, -4417));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(154112, -11378, -4417));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(152305, -12215, -4473));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(149152, -11586, -4448));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(148476, -13337, -4389));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(144990, -12759, -4430));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(143693, -10567, -4627));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(142818, -8733, -4573));
		LOCS.get(LocType.BLAZING_SWAMP).add(new LocationHolder(141121, -10344, -4618));
		// ------------------------------------------------------------------------
	}
	
	// lista de los chest que se spawnean en el evento
	private static final List<NpcHolder> chests = new CopyOnWriteArrayList<>();
	
	// puntos de cada player
	private static final Map<String, Integer> playerPoints = new ConcurrentHashMap<>();
	
	public SearchChest()
	{
		registerMod(false);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				spawn();
				break;
			case END:
				
				playerPoints.clear();
				
				unspawn();
				break;
		}
	}
	
	public static void giveRewards()
	{
		HashMap<String, Integer> pointsOrdered = new LinkedHashMap<>();
		
		// Sort the list according to your scores
		int LIMIT = playerPoints.size() > 1 ? playerPoints.size() / 2 : 1;
		
		playerPoints.entrySet().stream().sorted(Entry.<String, Integer> comparingByValue().reversed()).limit(LIMIT).forEach(e ->
		{
			pointsOrdered.put(e.getKey(), e.getValue());
		});
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (pointsOrdered.containsKey(player.getName()))
			{
				// A message is sent to all participants informing the winning team.
				UtilMessage.toAllOnline(Say2.TELL, "Congratulations winner!");
				// Prizes are awarded.
				ConfigData.CHEST_REWARDS.forEach(rh -> player.getInventory().addItem("Event", rh.getRewardId(), rh.getRewardCount(), player, null));
			}
		}
		
		// The html of the ranking is generated
		HtmlBuilder hb = Html.eventRanking(pointsOrdered);
		
		for (L2PcInstance player : L2World.getInstance().getAllPlayers())
		{
			if (playerPoints.containsKey(player.getName()))
			{
				// Send html.
				sendHtml(null, hb, ObjectData.get(PlayerHolder.class, player));
			}
			else
			{
				// UtilMessage.sendScreenMessage("Ganador de la busqueda del tesoro: " + pointsOrdered.get(key), time, list);
			}
		}
	}
	
	private void spawn()
	{
		// Delete all old chest.
		unspawn();
		// location type random.
		LocType locRandom = LocType.values()[Rnd.get(LocType.values().length)];
		// List of locations.
		List<LocationHolder> locList = LOCS.get(locRandom);
		// Place name
		String locName = locRandom.name().replaceAll("_", " ").toLowerCase();
		
		UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Chest spawn near " + locName);
		// Generate spawns
		for (LocationHolder loc : locList)
		{
			int x = loc.getX();
			int y = loc.getY();
			int z = loc.getZ();
			
			NpcHolder chest = UtilSpawn.npc(ConfigData.CHEST, x, y, z, 0, 0, 0, TeamType.NONE, 0);
			chest.getInstance().startAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLAME);
			chest.getInstance().setIsInvul(true);
			chests.add(chest);
		}
		
		// Send custom package for map
		UtilMessage.toAllOnline(new ObjectPosition(chests.subList(0, 8)));// TODO 8?
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (chests.contains(character))
		{
			NpcHolder npc = (NpcHolder) character;
			// remove from list.
			chests.remove(npc);
			// Remove custom effect
			npc.getInstance().stopAbnormalEffect(L2Character.ABNORMAL_EFFECT_FLAME);
			// Remove chest
			npc.getInstance().deleteMe();
			// Increases by one the number of points of a character
			increasePlayerPoint(ph);
			
			// TODO missing html
			return true;
		}
		
		return false;
	}
	
	/**
	 * Increases by one the number of points of a character
	 * @param player
	 */
	private static void increasePlayerPoint(PlayerHolder player)
	{
		if (!playerPoints.containsKey(player.getName()))
		{
			playerPoints.put(player.getName(), 0);
		}
		
		int points = playerPoints.get(player.getName());
		playerPoints.put(player.getName(), ++points);
	}
	
	/**
	 * Delete all old chest.
	 */
	private static void unspawn()
	{
		// delete all chest
		chests.forEach(chest -> chest.getInstance().deleteMe());
		// clear variable.
		chests.clear();
	}
}
