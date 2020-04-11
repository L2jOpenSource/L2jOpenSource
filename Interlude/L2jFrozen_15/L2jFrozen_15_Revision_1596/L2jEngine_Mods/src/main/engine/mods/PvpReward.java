package main.engine.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.l2jfrozen.gameserver.datatables.sql.ItemTable;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;
import com.l2jfrozen.util.random.Rnd;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.RewardHolder;
import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;
import main.util.Util;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class PvpReward extends AbstractMod
{
	public class PvPHolder
	{
		public int victim;
		public long time;
		
		public PvPHolder(int victim, long time)
		{
			this.victim = victim;
			this.time = time;
		}
	}
	
	// Variable in charge of carrying the victims and the time in which they died.
	private static Map<Integer, List<PvPHolder>> pvp = new HashMap<>();
	
	/**
	 * Constructor
	 */
	public PvpReward()
	{
		registerMod(ConfigData.ENABLE_PvpReward);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (!Util.areObjectType(L2PcInstance.class, victim) || (killer.getActingPlayer() == null))
		{
			return;
		}
		
		PlayerHolder phKiller = killer.getActingPlayer();
		PlayerHolder phVictim = victim.getActingPlayer();
		
		String ip1 = phKiller.getInstance().getClient().getConnection().getInetAddress().getHostAddress();
		String ip2 = phVictim.getInstance().getClient().getConnection().getInetAddress().getHostAddress();
		
		if (ip1.equals(ip2))
		{
			return;
		}
		
		// Check if this character won some PvP
		if (!pvp.containsKey(phKiller.getObjectId()))
		{
			// The list of victims is initialized
			pvp.put(phKiller.getObjectId(), new ArrayList<>());
		}
		
		// Check the list of killer victims and the time elapsed.
		for (PvPHolder pvp : pvp.get(phKiller.getObjectId()))
		{
			// If you find that I ever kill this player, it checks how long it was.
			if (pvp.victim == phVictim.getObjectId())
			{
				if ((pvp.time + ConfigData.PVP_TIME) < System.currentTimeMillis())
				{
					// The prize is awarded
					giveRewards(phKiller);
					// The time is reset
					pvp.time = System.currentTimeMillis();
				}
				return;
			}
		}
		
		// If we get here it's because it's the first kill to this player.
		pvp.get(phKiller.getObjectId()).add(new PvPHolder(victim.getObjectId(), System.currentTimeMillis()));
	}
	
	/**
	 * Prizes are delivered and a custom message is sent for each prize.
	 * @param ph
	 * @param victim
	 */
	private static void giveRewards(PlayerHolder ph)
	{
		L2PcInstance player = ph.getInstance();
		
		for (RewardHolder reward : ConfigData.PVP_REWARDS)
		{
			if (Rnd.get(100) <= reward.getRewardChance())
			{
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "", "Have won " + reward.getRewardCount() + " " + ItemTable.getInstance().getTemplate(reward.getRewardId()).getName());
				player.getInventory().addItem("PvpReward", reward.getRewardId(), reward.getRewardCount(), player, null);
			}
		}
	}
}
