/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.model.actor.transform;

import java.util.ArrayList;
import java.util.List;

import l2r.gameserver.datatables.xml.SkillTreesData;
import l2r.gameserver.model.L2SkillLearn;
import l2r.gameserver.model.StatsSet;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.holders.AdditionalItemHolder;
import l2r.gameserver.model.holders.AdditionalSkillHolder;
import l2r.gameserver.model.holders.SkillHolder;
import l2r.gameserver.model.interfaces.IIdentifiable;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.model.stats.Stats;
import l2r.gameserver.network.serverpackets.ExBasicActionList;

/**
 * @author UnAfraid
 */
public final class Transform implements IIdentifiable
{
	private final int _id;
	private final int _displayId;
	private final TransformType _type;
	private final boolean _canSwim;
	private final int _spawnHeight;
	private final boolean _canAttack;
	private final String _name;
	private final String _title;
	
	private TransformTemplate _maleTemplate;
	private TransformTemplate _femaleTemplate;
	
	public Transform(StatsSet set)
	{
		_id = set.getInteger("id");
		_displayId = set.getInteger("displayId", _id);
		_type = set.getEnum("type", TransformType.class, TransformType.COMBAT);
		_canSwim = set.getInteger("can_swim", 0) == 1;
		_canAttack = set.getInteger("normal_attackable", 1) == 1;
		_spawnHeight = set.getInteger("spawn_height", 0);
		_name = set.getString("setName", null);
		_title = set.getString("setTitle", null);
	}
	
	/**
	 * Gets the transformation ID.
	 * @return the transformation ID
	 */
	@Override
	public int getId()
	{
		return _id;
	}
	
	public int getDisplayId()
	{
		return _displayId;
	}
	
	public TransformType getType()
	{
		return _type;
	}
	
	public boolean canSwim()
	{
		return _canSwim;
	}
	
	public boolean canAttack()
	{
		return _canAttack;
	}
	
	public int getSpawnHeight()
	{
		return _spawnHeight;
	}
	
	/**
	 * @return name that's going to be set to the player while is transformed with current transformation
	 */
	public String getName()
	{
		return _name;
	}
	
	/**
	 * @return title that's going to be set to the player while is transformed with current transformation
	 */
	public String getTitle()
	{
		return _title;
	}
	
	public TransformTemplate getTemplate(L2PcInstance player)
	{
		return player != null ? (player.getAppearance().getSex() ? _femaleTemplate : _maleTemplate) : null;
	}
	
	public void setTemplate(boolean male, TransformTemplate template)
	{
		if (male)
		{
			_maleTemplate = template;
		}
		else
		{
			_femaleTemplate = template;
		}
	}
	
	/**
	 * @return {@code true} if transform type is mode change, {@code false} otherwise
	 */
	public boolean isStance()
	{
		return _type == TransformType.MODE_CHANGE;
	}
	
	/**
	 * @return {@code true} if transform type is combat, {@code false} otherwise
	 */
	public boolean isCombat()
	{
		return _type == TransformType.COMBAT;
	}
	
	/**
	 * @return {@code true} if transform type is non combat, {@code false} otherwise
	 */
	public boolean isNonCombat()
	{
		return _type == TransformType.NON_COMBAT;
	}
	
	/**
	 * @return {@code true} if transform type is flying, {@code false} otherwise
	 */
	public boolean isFlying()
	{
		return _type == TransformType.FLYING;
	}
	
	/**
	 * @return {@code true} if transform type is cursed, {@code false} otherwise
	 */
	public boolean isCursed()
	{
		return _type == TransformType.CURSED;
	}
	
	/**
	 * @return {@code true} if transform type is raiding, {@code false} otherwise
	 */
	public boolean isRiding()
	{
		return _type == TransformType.RIDING_MODE;
	}
	
	/**
	 * @return {@code true} if transform type is pure stat, {@code false} otherwise
	 */
	public boolean isPureStats()
	{
		return _type == TransformType.PURE_STAT;
	}
	
	public double getCollisionHeight(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		return template != null ? template.getCollisionHeight() : player.getCollisionHeight();
	}
	
	public double getCollisionRadius(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		return template != null ? template.getCollisionRadius() : player.getCollisionRadius();
	}
	
	public int getBaseAttackRange(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		return template != null ? template.getBaseAttackRange() : player.getTemplate().getBaseAttackRange();
	}
	
	public void onTransform(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			// Start flying.
			if (isFlying())
			{
				player.setIsFlying(true);
			}
			
			if (getName() != null)
			{
				player.getAppearance().setVisibleName(getName());
			}
			if (getTitle() != null)
			{
				player.getAppearance().setVisibleTitle(getTitle());
			}
			
			// Add common skills.
			for (SkillHolder holder : template.getSkills())
			{
				if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
				{
					player.addSkill(holder.getSkill(), false);
				}
				player.addTransformSkill(holder.getSkillId());
			}
			
			// Add skills depending on level.
			for (AdditionalSkillHolder holder : template.getAdditionalSkills())
			{
				if (player.getLevel() >= holder.getMinLevel())
				{
					if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
					{
						player.addSkill(holder.getSkill(), false);
					}
					player.addTransformSkill(holder.getSkillId());
				}
			}
			
			// Add collection skills.
			for (L2SkillLearn skill : SkillTreesData.getInstance().getCollectSkillTree().values())
			{
				if (player.getKnownSkill(skill.getSkillId()) != null)
				{
					player.addTransformSkill(skill.getSkillId());
				}
			}
			
			// Set inventory blocks if needed.
			if (!template.getAdditionalItems().isEmpty())
			{
				final List<Integer> allowed = new ArrayList<>();
				final List<Integer> notAllowed = new ArrayList<>();
				for (AdditionalItemHolder holder : template.getAdditionalItems())
				{
					if (holder.isAllowedToUse())
					{
						allowed.add(holder.getId());
					}
					else
					{
						notAllowed.add(holder.getId());
					}
				}
				
				if (!allowed.isEmpty())
				{
					final int[] items = new int[allowed.size()];
					for (int i = 0; i < items.length; i++)
					{
						items[i] = allowed.get(i);
					}
					player.getInventory().setInventoryBlock(items, 1);
				}
				
				if (!notAllowed.isEmpty())
				{
					final int[] items = new int[notAllowed.size()];
					for (int i = 0; i < items.length; i++)
					{
						items[i] = notAllowed.get(i);
					}
					player.getInventory().setInventoryBlock(items, 2);
				}
			}
			
			// Send basic action list.
			if (template.hasBasicActionList())
			{
				player.sendPacket(template.getBasicActionList());
			}
		}
	}
	
	public void onUntransform(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			// Stop flying.
			if (isFlying())
			{
				player.setIsFlying(false);
			}
			
			if (getName() != null)
			{
				player.getAppearance().setVisibleName(null);
			}
			if (getTitle() != null)
			{
				player.getAppearance().setVisibleTitle(null);
			}
			
			// Remove common skills.
			if (!template.getSkills().isEmpty())
			{
				for (SkillHolder holder : template.getSkills())
				{
					final L2Skill skill = holder.getSkill();
					if (!SkillTreesData.getInstance().isSkillAllowed(player, skill))
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}
			}
			
			// Remove skills depending on level.
			if (!template.getAdditionalSkills().isEmpty())
			{
				for (AdditionalSkillHolder holder : template.getAdditionalSkills())
				{
					final L2Skill skill = holder.getSkill();
					if ((player.getLevel() >= holder.getMinLevel()) && !SkillTreesData.getInstance().isSkillAllowed(player, skill))
					{
						player.removeSkill(skill, false, skill.isPassive());
					}
				}
			}
			
			// Remove transformation skills.
			player.removeAllTransformSkills();
			
			// Remove inventory blocks if needed.
			if (!template.getAdditionalItems().isEmpty())
			{
				player.getInventory().unblock();
			}
			
			player.sendPacket(ExBasicActionList.STATIC_PACKET);
		}
	}
	
	public void onLevelUp(L2PcInstance player)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			// Add skills depending on level.
			if (!template.getAdditionalSkills().isEmpty())
			{
				for (AdditionalSkillHolder holder : template.getAdditionalSkills())
				{
					if (player.getLevel() >= holder.getMinLevel())
					{
						if (player.getSkillLevel(holder.getSkillId()) < holder.getSkillLvl())
						{
							player.addSkill(holder.getSkill(), false);
							player.addTransformSkill(holder.getSkillId());
						}
					}
				}
			}
		}
	}
	
	public double getStat(L2PcInstance player, Stats stats)
	{
		double val = 0;
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			val = template.getStats(stats);
			final TransformLevelData data = template.getData(player.getLevel());
			if (data != null)
			{
				val = data.getStats(stats);
			}
		}
		return val;
	}
	
	/**
	 * @param player
	 * @param slot
	 * @return
	 */
	public int getBaseDefBySlot(L2PcInstance player, int slot)
	{
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			return template.getDefense(slot);
		}
		return player.getTemplate().getBaseDefBySlot(slot);
	}
	
	/**
	 * @param player
	 * @return
	 */
	public double getLevelMod(L2PcInstance player)
	{
		double val = -1;
		final TransformTemplate template = getTemplate(player);
		if (template != null)
		{
			final TransformLevelData data = template.getData(player.getLevel());
			if (data != null)
			{
				val = data.getLevelMod();
			}
		}
		return val;
	}
}
