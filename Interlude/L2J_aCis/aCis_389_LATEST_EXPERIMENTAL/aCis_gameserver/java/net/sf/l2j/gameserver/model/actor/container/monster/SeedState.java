package net.sf.l2j.gameserver.model.actor.container.monster;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2j.Config;
import net.sf.l2j.gameserver.enums.actors.NpcSkillType;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.actor.instance.Monster;
import net.sf.l2j.gameserver.model.holder.IntIntHolder;
import net.sf.l2j.gameserver.model.manor.Seed;
import net.sf.l2j.gameserver.skills.L2Skill;

/**
 * A container holding all related informations of a {@link Monster} seed state.<br>
 * <br>
 * A seed occurs when a {@link Player} uses a seed over a Monster.
 */
public class SeedState
{
	private final Monster _owner;
	
	private final List<IntIntHolder> _harvestItems = new ArrayList<>();
	
	private Seed _seed;
	private int _seederId;
	
	public SeedState(Monster owner)
	{
		_owner = owner;
	}
	
	public int getSeederId()
	{
		return _seederId;
	}
	
	public Seed getSeed()
	{
		return _seed;
	}
	
	public boolean isSeeded()
	{
		return _seed != null;
	}
	
	/**
	 * @param player : The Player to test.
	 * @return true if the given {@link Player} set as parameter is the actual seeder.
	 */
	public boolean isActualSeeder(Player player)
	{
		return player != null && player.getObjectId() == _seederId;
	}
	
	/**
	 * @return a {@link List} containing all {@link IntIntHolder}s that can be harvested.
	 */
	public List<IntIntHolder> getHarvestItems()
	{
		return _harvestItems;
	}
	
	/**
	 * Sets state of the mob to seeded.
	 * @param objectId : The player object id to check.
	 */
	public void calculateHarvestItems(int objectId)
	{
		if (_seed != null && _seederId == objectId)
		{
			int count = 1;
			
			for (L2Skill skill : _owner.getTemplate().getSkills(NpcSkillType.PASSIVE))
			{
				switch (skill.getId())
				{
					case 4303: // Strong type x2
						count *= 2;
						break;
					case 4304: // Strong type x3
						count *= 3;
						break;
					case 4305: // Strong type x4
						count *= 4;
						break;
					case 4306: // Strong type x5
						count *= 5;
						break;
					case 4307: // Strong type x6
						count *= 6;
						break;
					case 4308: // Strong type x7
						count *= 7;
						break;
					case 4309: // Strong type x8
						count *= 8;
						break;
					case 4310: // Strong type x9
						count *= 9;
						break;
				}
			}
			
			final int diff = _owner.getLevel() - _seed.getLevel() - 5;
			if (diff > 0)
				count += diff;
			
			_harvestItems.add(new IntIntHolder(_seed.getCropId(), count * Config.RATE_DROP_MANOR));
		}
	}
	
	/**
	 * Set the seed parameters.
	 * @param seed : The Seed to set.
	 * @param objectId : The Player objectId who is sowing the seed.
	 */
	public void setSeeded(Seed seed, int objectId)
	{
		_seed = seed;
		_seederId = objectId;
	}
	
	/**
	 * Clear all seed related variables.
	 */
	public void clear()
	{
		_seed = null;
		_seederId = 0;
		_harvestItems.clear();
	}
}