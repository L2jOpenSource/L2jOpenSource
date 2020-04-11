
package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.List;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.datatables.xml.AugmentationData;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.skills.Stats;
import com.l2jfrozen.gameserver.skills.funcs.FuncAdd;
import com.l2jfrozen.gameserver.skills.funcs.LambdaConst;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * Used to store an augmentation and its boni
 * @author durgus
 */
public final class L2Augmentation
{
	private static final Logger LOGGER = Logger.getLogger(L2Augmentation.class);
	private static final String INSERT_AUGMENTATION = "INSERT INTO augmentations (item_object_id,attributes,skill,level) VALUES (?,?,?,?)";
	private static final String DELETE_AUGMENTATION_BY_ITEM_OBJECT_ID = "DELETE FROM augmentations WHERE item_object_id=?";
	private final L2ItemInstance item;
	private int effectsId = 0;
	private AugmentationStatBoni boni = null;
	private L2Skill skill = null;
	
	public L2Augmentation(final L2ItemInstance item, final int effects, final L2Skill skill, final boolean save)
	{
		this.item = item;
		effectsId = effects;
		boni = new AugmentationStatBoni(effectsId);
		this.skill = skill;
		
		// write to DB if save is true
		if (save)
		{
			saveAugmentationData();
		}
	}
	
	public L2Augmentation(final L2ItemInstance item, final int effects, final int skill, final int skillLevel, final boolean save)
	{
		this(item, effects, SkillTable.getInstance().getInfo(skill, skillLevel), save);
	}
	
	// =========================================================
	// Nested Class
	
	public class AugmentationStatBoni
	{
		private final Stats stats[];
		private final float values[];
		private boolean active;
		
		public AugmentationStatBoni(final int augmentationId)
		{
			active = false;
			List<AugmentationData.AugStat> as = AugmentationData.getInstance().getAugStatsById(augmentationId);
			
			stats = new Stats[as.size()];
			values = new float[as.size()];
			
			int i = 0;
			for (final AugmentationData.AugStat aStat : as)
			{
				stats[i] = aStat.getStat();
				values[i] = aStat.getValue();
				i++;
			}
			
			as = null;
		}
		
		public void applyBoni(final L2PcInstance player)
		{
			// make sure the boni are not applyed twice..
			if (active)
			{
				return;
			}
			
			for (int i = 0; i < stats.length; i++)
			{
				player.addStatFunc(new FuncAdd(stats[i], 0x40, this, new LambdaConst(values[i])));
			}
			
			active = true;
		}
		
		public void removeBoni(final L2PcInstance player)
		{
			// make sure the boni is not removed twice
			if (!active)
			{
				return;
			}
			
			player.removeStatsOwner(this);
			
			active = false;
		}
	}
	
	private void saveAugmentationData()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(INSERT_AUGMENTATION))
		{
			statement.setInt(1, item.getObjectId());
			statement.setInt(2, effectsId);
			
			if (skill != null)
			{
				statement.setInt(3, skill.getId());
				statement.setInt(4, skill.getLevel());
			}
			else
			{
				statement.setInt(3, 0);
				statement.setInt(4, 0);
			}
			
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not save augmentation for item: " + item.getObjectId() + " from DB:", e);
		}
	}
	
	public void deleteAugmentationData()
	{
		if (!item.isAugmented())
		{
			return;
		}
		
		// delete the augmentation from the database
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_AUGMENTATION_BY_ITEM_OBJECT_ID);)
		{
			
			statement.setInt(1, item.getObjectId());
			statement.executeUpdate();
		}
		catch (final Exception e)
		{
			LOGGER.error("Could not delete augmentation for item: " + item.getObjectId() + " from DB:", e);
		}
	}
	
	/**
	 * Get the augmentation "id" used in serverpackets.
	 * @return augmentationId
	 */
	public int getAugmentationId()
	{
		return effectsId;
	}
	
	public L2Skill getSkill()
	{
		return skill;
	}
	
	/**
	 * Applys the boni to the player.
	 * @param player
	 */
	public void applyBoni(final L2PcInstance player)
	{
		boni.applyBoni(player);
		
		// add the skill if any
		if (skill != null)
		{
			
			player.addSkill(skill);
			
			if (skill.isActive() && Config.ACTIVE_AUGMENTS_START_REUSE_TIME > 0)
			{
				player.disableSkill(skill, Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
				player.addTimeStamp(skill, Config.ACTIVE_AUGMENTS_START_REUSE_TIME);
			}
			
			player.sendSkillList();
		}
	}
	
	/**
	 * Removes the augmentation boni from the player.
	 * @param player
	 */
	public void removeBoni(final L2PcInstance player)
	{
		boni.removeBoni(player);
		
		// remove the skill if any
		if (skill != null)
		{
			if (skill.isPassive())
			{
				player.removeSkill(skill);
			}
			else
			{
				player.removeSkill(skill, false);
			}
			
			if ((skill.isPassive() && Config.DELETE_AUGM_PASSIVE_ON_CHANGE) || (skill.isActive() && Config.DELETE_AUGM_ACTIVE_ON_CHANGE))
			{
				
				// Iterate through all effects currently on the character.
				final L2Effect[] effects = player.getAllEffects();
				
				for (final L2Effect currenteffect : effects)
				{
					final L2Skill effectSkill = currenteffect.getSkill();
					
					if (effectSkill.getId() == skill.getId())
					{
						player.sendMessage("You feel the power of " + effectSkill.getName() + " leaving yourself.");
						currenteffect.exit(false);
					}
				}
				
			}
			player.sendSkillList();
		}
	}
}
