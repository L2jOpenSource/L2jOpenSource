/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripts.ai.npc.Teleports;

import java.util.Map;

import javolution.util.FastMap;
import l2r.gameserver.instancemanager.TownManager;
import l2r.gameserver.model.Location;
import l2r.gameserver.model.actor.L2Npc;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.quest.Quest;
import l2r.gameserver.model.quest.QuestState;
import l2r.gameserver.model.zone.type.L2TownZone;
import l2r.util.Rnd;

public class DelusionChamberTeleport extends Quest
{
	private final static int GUARDIAN_OF_EASTERN_SEAL = 32658;
	private final static int GUARDIAN_OF_WESTERN_SEAL = 32659;
	private final static int GUARDIAN_OF_SOUTHERN_SEAL = 32660;
	private final static int GUARDIAN_OF_NORTHERN_SEAL = 32661;
	private final static int GUARDIAN_OF_TOWER_OF_SEAL = 32663;
	private final static int GUARDIAN_OF_GREATER_SEAL = 32662;
	private final static int PATHFINDER_WORKER = 32484;
	
	private final static int[][] HALL_LOCATION =
	{
		{
			-114597,
			-152501,
			-6750
		},
		{
			-114589,
			-154162,
			-6750
		}
	};
	
	private final static Map<Integer, Location> RETURN_LOCATION = new FastMap<>();
	
	static
	{
		RETURN_LOCATION.put(0, new Location(43835, -47749, -792, 0)); // If undefined origin, return to Rune
		RETURN_LOCATION.put(7, new Location(-14023, 123677, -3112, 0)); // Gludio Castle Town
		RETURN_LOCATION.put(8, new Location(18101, 145936, -3088, 0)); // Dion Castle Town
		RETURN_LOCATION.put(10, new Location(80905, 56361, -1552, 0)); // Oren Castle Town
		RETURN_LOCATION.put(14, new Location(42772, -48062, -792, 0)); // Rune Township
		RETURN_LOCATION.put(15, new Location(108469, 221690, -3592, 0)); // Heine
		RETURN_LOCATION.put(17, new Location(85991, -142234, -1336, 0)); // Schuttgart
	}
	
	public DelusionChamberTeleport(int questId, String name, String descr)
	{
		super(questId, name, descr);
		addStartNpc(PATHFINDER_WORKER);
		addTalkId(PATHFINDER_WORKER);
		addTalkId(GUARDIAN_OF_EASTERN_SEAL);
		addTalkId(GUARDIAN_OF_WESTERN_SEAL);
		addTalkId(GUARDIAN_OF_SOUTHERN_SEAL);
		addTalkId(GUARDIAN_OF_NORTHERN_SEAL);
		addTalkId(GUARDIAN_OF_GREATER_SEAL);
		addTalkId(GUARDIAN_OF_TOWER_OF_SEAL);
	}
	
	@Override
	public String onTalk(L2Npc npc, L2PcInstance player)
	{
		QuestState st = player.getQuestState(getName());
		
		if (npc.getId() == PATHFINDER_WORKER)
		{
			int townId = 0;
			L2TownZone town = TownManager.getTown(npc.getX(), npc.getY(), npc.getZ());
			
			if (town != null)
			{
				townId = town.getTownId();
			}
			
			st.set("return_loc", Integer.toString(townId));
			int rand = Rnd.get(2);
			player.teleToLocation(HALL_LOCATION[rand][0], HALL_LOCATION[rand][1], HALL_LOCATION[rand][2]);
		}
		
		else if (npc.getId() == GUARDIAN_OF_EASTERN_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		else if (npc.getId() == GUARDIAN_OF_WESTERN_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		else if (npc.getId() == GUARDIAN_OF_SOUTHERN_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		else if (npc.getId() == GUARDIAN_OF_NORTHERN_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		else if (npc.getId() == GUARDIAN_OF_GREATER_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		else if (npc.getId() == GUARDIAN_OF_TOWER_OF_SEAL)
		{
			int townId = 0;
			
			if (!st.get("return_loc").isEmpty())
			{
				townId = Integer.parseInt(st.get("return_loc"));
			}
			if (!RETURN_LOCATION.containsKey(townId))
			{
				townId = 0;
			}
			
			Location loc = RETURN_LOCATION.get(townId);
			player.teleToLocation(loc, false);
			
			st.exitQuest(true);
		}
		
		return "";
	}
	
	public static void main(String[] args)
	{
		new DelusionChamberTeleport(-1, DelusionChamberTeleport.class.getSimpleName(), "ai/npc/Teleports");
	}
}