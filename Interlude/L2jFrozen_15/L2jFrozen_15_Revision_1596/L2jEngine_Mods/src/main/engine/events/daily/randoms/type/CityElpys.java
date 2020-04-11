package main.engine.events.daily.randoms.type;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.Location;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.util.random.Rnd;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.enums.TeamType;
import main.holders.RewardHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.NpcHolder;
import main.packets.ObjectPosition;
import main.util.Util;
import main.util.UtilInventory;
import main.util.UtilMessage;
import main.util.UtilSpawn;

/**
 * @author fissban
 */
public class CityElpys extends AbstractMod
{
	// lista de los elpys que se spawnean en el evento
	private static final List<NpcHolder> elpys = new CopyOnWriteArrayList<>();
	
	public CityElpys()
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
				unspawn();
				break;
		}
	}
	
	private void spawn()
	{
		// Se remueve todos los elpys del evento anterior
		unspawn();
		// Se obtiene un lugar random para el evento
		Location loc = ConfigData.ELPY_LOC.get(Rnd.get(ConfigData.ELPY_LOC.size()));
		// Se anuncia donde se generaran los spawns
		String locName = Util.getClosestTownName(loc.getX(), loc.getY());
		UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Elpys spawn near " + locName);
		// Se generan los nuevos spawns
		for (int i = 0; i < ConfigData.ELPY_COUNT; i++)
		{
			int x = loc.getX() + Rnd.get(-ConfigData.ELPY_RANGE_SPAWN, ConfigData.ELPY_RANGE_SPAWN);
			int y = loc.getY() + Rnd.get(-ConfigData.ELPY_RANGE_SPAWN, ConfigData.ELPY_RANGE_SPAWN);
			int z = loc.getZ();
			
			NpcHolder nh = UtilSpawn.npc(ConfigData.ELPY, x, y, z, 0, 0, 0, TeamType.NONE, 0);
			
			if (nh != null)
			{
				elpys.add(nh);
			}
		}
		
		// Send packet ObjectPosition
		UtilMessage.toAllOnline(new ObjectPosition(elpys));
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (elpys.contains(victim))
		{
			elpys.remove(victim);
			for (RewardHolder reward : ConfigData.ELPY_REWARDS)
			{
				if (Rnd.get(100) <= reward.getRewardChance())
				{
					UtilMessage.sendCreatureMsg(killer, Say2.TELL, "[Engine]", "Have won " + reward.getRewardCount() + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
					UtilInventory.giveItems(killer.getActingPlayer(), reward.getRewardId(), reward.getRewardCount(), 0);
				}
			}
		}
	}
	
	private static void unspawn()
	{
		for (NpcHolder mob : elpys)
		{
			mob.getInstance().deleteMe();
		}
		// Se limpia la variable
		elpys.clear();
	}
}
