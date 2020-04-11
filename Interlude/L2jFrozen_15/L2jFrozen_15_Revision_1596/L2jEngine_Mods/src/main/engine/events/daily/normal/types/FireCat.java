package main.engine.events.daily.normal.types;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Attackable;
import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.model.actor.instance.L2NpcInstance;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
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
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.UtilSpawn;

/**
 * @author Micr0, fissban
 */
public class FireCat extends AbstractEvent
{
	// Npc
	private static final int CAT = 70433;
	// Items
	private static final int ELVEN_FIRECRACKER = 6403;
	private static final int GUNPOWDER = 6404;
	private static final int MAGNESIUM = 6405;
	private static final int FIREWORK = 6406;
	private static final int LARGE_FIREWORK = 6407;
	
	// Html
	private static final String HTML_PATH = "data/html/engine/events/firecat/";
	
	private static final List<LocationHolder> SPAWNS_CAT = new ArrayList<>();
	{
		SPAWNS_CAT.add(new LocationHolder(-44162, -112238, -240, 41000)); // lyonn03_npc1814_fi01
		SPAWNS_CAT.add(new LocationHolder(-14796, 123305, -3112, 0)); // gludio08_npc1921_fi01
		SPAWNS_CAT.add(new LocationHolder(-14295, 122438, -3104, 16000)); // gludio08_npc1921_fi02
		SPAWNS_CAT.add(new LocationHolder(-13065, 122916, -3112, 49000)); // gludio08_npc1921_fi03
		SPAWNS_CAT.add(new LocationHolder(11658, 16027, -4568, 22000)); // oren09_npc2018_fi01
		SPAWNS_CAT.add(new LocationHolder(19421, 145801, -3080, 48000)); // dion09_npc2022_fi01
		SPAWNS_CAT.add(new LocationHolder(15849, 142855, -2696, 15500)); // dion09_npc2022_fi02
		SPAWNS_CAT.add(new LocationHolder(17240, 170540, -3496, 54000)); // dion10_npc2023_fi01
		SPAWNS_CAT.add(new LocationHolder(43051, 50108, -2992, 63500)); // oren04_npc2119_fi01
		SPAWNS_CAT.add(new LocationHolder(83185, 53480, -1448, 32000)); // oren17_npc2219_fi01
		SPAWNS_CAT.add(new LocationHolder(80225, 55052, -1552, 0)); // oren17_npc2219_fi02
		SPAWNS_CAT.add(new LocationHolder(83362, 55473, -1520, 32000)); // oren17_npc2219_fi03
		SPAWNS_CAT.add(new LocationHolder(83691, 148386, -3400, 32000)); // giran11_npc2222_fi01
		SPAWNS_CAT.add(new LocationHolder(80239, 146832, -3528, 0)); // giran11_npc2222_fi02
		SPAWNS_CAT.add(new LocationHolder(81923, 148916, -3464, 16000)); // giran11_npc2222_fi03
		SPAWNS_CAT.add(new LocationHolder(114891, -178149, -832, 0)); // schuttgart03_npc2312_fi01
		SPAWNS_CAT.add(new LocationHolder(-81435, 151717, -3120, 48000)); // gludio06_npc1722_fi01
		SPAWNS_CAT.add(new LocationHolder(-83118, 150925, -3120, 0)); // gludio06_npc1722_fi02
		SPAWNS_CAT.add(new LocationHolder(117368, 76729, -2688, 44000)); // aden14_npc2320_fi01
		SPAWNS_CAT.add(new LocationHolder(115626, 75730, -2592, 5000)); // aden14_npc2320_fi02
		SPAWNS_CAT.add(new LocationHolder(-84019, 242875, -3728, 59000)); // gludio25_npc1725_fi01
		SPAWNS_CAT.add(new LocationHolder(147450, 27763, -2264, 16000)); // aden13_npc2418_fi01
		SPAWNS_CAT.add(new LocationHolder(149779, 25431, -2136, 0)); // aden13_npc2418_fi02
		SPAWNS_CAT.add(new LocationHolder(147231, 29931, -2456, 16000)); // aden13_npc2418_fi03
		SPAWNS_CAT.add(new LocationHolder(111262, 219619, -3664, 16000)); // innadril09_npc2324_fi01
		SPAWNS_CAT.add(new LocationHolder(111591, 223109, -3672, 48000)); // innadril09_npc2324_fi02
		SPAWNS_CAT.add(new LocationHolder(147888, -58048, -2979, 49000)); // godard02_npc2416_fi01
		SPAWNS_CAT.add(new LocationHolder(147285, -56461, -2776, 11500)); // godard02_npc2416_fi02
		SPAWNS_CAT.add(new LocationHolder(44176, -48732, -800, 33000)); // rune02_npc2116_fi01
		SPAWNS_CAT.add(new LocationHolder(44294, -47642, -792, 50000)); // rune02_npc2116_fi02
	}
	
	private static List<NpcHolder> npcs = new ArrayList<>();
	
	public FireCat()
	{
		registerEvent(ConfigData.ENABLE_FireCat, ConfigData.FIRE_CAT_DATE_START, ConfigData.FIRE_CAT_DATE_END);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				// send message
				UtilMessage.sendAnnounceMsg(ConfigData.FIRE_CAT_MESSAGE_START, L2World.getInstance().getAllPlayers());
				// add npc spawns
				ThreadPool.schedule(() ->
				{
					SPAWNS_CAT.forEach(loc -> npcs.add(UtilSpawn.npc(CAT, loc.getX(), loc.getY(), loc.getZ(), loc.getHeading(), 0, 0, TeamType.NONE, 0)));
				}, 20000);
				break;
			case END:
				// remove npc spawns
				npcs.stream().filter(npc -> npc.getInstance() != null).forEach(npc ->
				{
					L2Spawn spawn = npc.getInstance().getSpawn();
					if (spawn != null)
					{
						// spawn.stopRespawn();
					}
					npc.getInstance().deleteMe();
				});
				npcs.clear();
				break;
		}
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (Util.areObjectType(L2Attackable.class, victim) && Util.checkLvlDifference(killer, victim, 9))
		{
			int chance = Rnd.get(100);
			
			if (chance > 75) // 25%
			{
				UtilInventory.giveItems((PlayerHolder) killer, ELVEN_FIRECRACKER, 1);
			}
			else if (chance > 55) // 20%
			{
				UtilInventory.giveItems((PlayerHolder) killer, GUNPOWDER, 1);
			}
			else if (chance > 40) // 15%
			{
				UtilInventory.giveItems((PlayerHolder) killer, GUNPOWDER, 1);
			}
			
		}
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		UtilMessage.sendAnnounceMsg(ConfigData.FIRE_CAT_MESSAGE_START, ph);
	}
	
	@Override
	public boolean onInteract(PlayerHolder ph, CharacterHolder character)
	{
		if (Util.areObjectType(L2NpcInstance.class, character))
		{
			L2NpcInstance npc = (L2NpcInstance) character.getInstance();
			
			if (npc.getNpcId() == CAT)
			{
				sendHtmlFile(ph, npc, HTML_PATH + CAT + ".htm");
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onEvent(PlayerHolder ph, CharacterHolder npc, String command)
	{
		if (((NpcHolder) npc).getId() != CAT)
		{
			return;
		}
		
		String htmltext = "";
		
		switch (command)
		{
			case "create":
				htmltext = "wantcreate.htm";
				break;
			case "regular":
				if ((UtilInventory.getItemsCount(ph, ELVEN_FIRECRACKER) >= 2) && (UtilInventory.getItemsCount(ph, GUNPOWDER) >= 2))
				{
					// take items
					UtilInventory.takeItems(ph, ELVEN_FIRECRACKER, 2);
					UtilInventory.takeItems(ph, GUNPOWDER, 2);
					// give item
					UtilInventory.giveItems(ph, FIREWORK, 1);
					
					htmltext = "regular.htm";
				}
				else
				{
					htmltext = "no-item.htm";
				}
				break;
			case "large":
				if ((UtilInventory.getItemsCount(ph, ELVEN_FIRECRACKER) >= 4) && (UtilInventory.getItemsCount(ph, GUNPOWDER) >= 4) && (UtilInventory.getItemsCount(ph, MAGNESIUM) >= 1))
				{
					// take items
					UtilInventory.takeItems(ph, ELVEN_FIRECRACKER, 4);
					UtilInventory.takeItems(ph, GUNPOWDER, 4);
					UtilInventory.takeItems(ph, MAGNESIUM, 1);
					// give item
					UtilInventory.giveItems(ph, LARGE_FIREWORK, 1);
					
					htmltext = "large.htm";
				}
				else
				{
					
					htmltext = "no-item.htm";
				}
				break;
			case "info":
				htmltext = "info.htm";
				break;
		}
		
		sendHtmlFile(ph, (L2NpcInstance) npc.getInstance(), HTML_PATH + htmltext);
	}
}
