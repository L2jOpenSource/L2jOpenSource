package main.engine.mods;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.util.database.L2DatabaseFactory;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class SubClassAcumulatives extends AbstractMod
{
	// SQL
	private static final String RESTORE_SKILLS_FOR_CHAR = "SELECT skill_id,skill_level,class_index FROM character_skills WHERE char_obj_id=?";
	
	/**
	 * Constructor
	 */
	public SubClassAcumulatives()
	{
		registerMod(ConfigData.ENABLE_SubClassAcumulatives);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public boolean onRestoreSkills(PlayerHolder ph)
	{
		if (ph.getInstance() == null)
		{
			return true;
		}
		
		Map<Integer, Integer> skills = new HashMap<Integer, Integer>();
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(RESTORE_SKILLS_FOR_CHAR))
		{
			// Retrieve all skills of this Player from the database
			ps.setInt(1, ph.getObjectId());
			
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					int id = rs.getInt("skill_id");
					int level = rs.getInt("skill_level");
					int classIndex = rs.getInt("class_index");
					
					if (ph.getInstance().getClassIndex() != classIndex)
					{
						L2Skill skill = SkillTable.getInstance().getInfo(id, level);
						
						if (skill == null)
						{
							LOG.log(Level.SEVERE, "Skipped null skill Id: " + id + ", Level: " + level + " while restoring player skills for " + ph.getName());
							continue;
						}
						
						if (!ConfigData.ACUMULATIVE_PASIVE_SKILLS)
						{
							if (skill.isPassive())
							{
								continue;
							}
						}
						
						if (ConfigData.DONT_ACUMULATIVE_SKILLS_ID.contains(id))
						{
							continue;
						}
					}
					
					// Save all the skills that we will teach our character.
					// This will avoid teaching a skill from lvl 1 to 15 for example
					// And directly we teach the lvl 15 =)
					if ((skills.get(id) != null) && (skills.get(id) > level))
					{
						continue;
					}
					
					skills.put(id, level);
				}
			}
		}
		catch (Exception e)
		{
			LOG.log(Level.SEVERE, "Could not restore " + ph.getName() + " skills:", e);
			e.printStackTrace();
		}
		
		for (Entry<Integer, Integer> entry : skills.entrySet())
		{
			int id = entry.getKey();
			int level = entry.getValue();
			
			// The level of skill that the character has is checked.
			if (ph.getInstance().getSkillLevel(id) < level)
			{
				// Create a Skill object for each record
				L2Skill skill = SkillTable.getInstance().getInfo(id, level);
				
				// Add the Skill object to the L2Character skills and its Func objects to the calculator set of the L2Character
				ph.getInstance().addSkill(skill, false);
			}
		}
		
		return true;
	}
}
