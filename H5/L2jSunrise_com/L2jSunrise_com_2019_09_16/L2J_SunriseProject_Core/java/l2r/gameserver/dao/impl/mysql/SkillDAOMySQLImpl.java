/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.impl.mysql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.dao.SkillDAO;
import l2r.gameserver.data.xml.impl.ClassListData;
import l2r.gameserver.data.xml.impl.SkillData;
import l2r.gameserver.data.xml.impl.SkillTreesData;
import l2r.gameserver.enums.IllegalActionPunishmentType;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;
import l2r.gameserver.util.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Skill DAO MySQL implementation.
 * @author vGodFather
 */
public class SkillDAOMySQLImpl implements SkillDAO
{
	private static Logger Log = LoggerFactory.getLogger("DAO");
	
	private static final String SELECT = "SELECT skill_id,skill_level FROM character_skills WHERE charId=? AND class_index=?";
	private static final String INSERT = "INSERT INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	private static final String UPDATE = "UPDATE character_skills SET skill_level=? WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String REPLACE = "REPLACE INTO character_skills (charId,skill_id,skill_level,class_index) VALUES (?,?,?,?)";
	private static final String DELETE_ONE = "DELETE FROM character_skills WHERE skill_id=? AND charId=? AND class_index=?";
	private static final String DELETE_ALL = "DELETE FROM character_skills WHERE charId=? AND class_index=?";
	
	@Override
	public void load(L2PcInstance player)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SELECT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, player.getClassIndex());
			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					final int id = rs.getInt("skill_id");
					final int level = rs.getInt("skill_level");
					
					// Create a L2Skill object for each record
					final L2Skill skill = SkillData.getInstance().getSkill(id, level);
					
					if (skill == null)
					{
						Log.warn("Skipped null skill Id: " + id + " Level: " + level + " while restoring player skills for playerObjId: " + player.getObjectId());
						continue;
					}
					
					// Add the L2Skill object to the L2Character _skills and its Func objects to the calculator set of the L2Character
					player.addSkill(skill);
					
					if (Config.SKILL_CHECK_ENABLE)
					{
						boolean mustCheck = false;
						if (!player.isGM())
						{
							mustCheck = true;
						}
						else if (player.isGM() && Config.SKILL_CHECK_GM)
						{
							mustCheck = true;
						}
						
						if (mustCheck)
						{
							if (!SkillTreesData.getInstance().isSkillAllowed(player, skill))
							{
								Util.handleIllegalPlayerAction(player, "Player " + player.getName() + " has invalid skill " + skill.getName() + " (" + skill.getId() + "/" + skill.getLevel() + "), class:" + ClassListData.getInstance().getClass(player.getClassId()).getClassName(), IllegalActionPunishmentType.BROADCAST);
								if (Config.SKILL_CHECK_REMOVE)
								{
									player.removeSkill(skill);
								}
							}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			Log.error("Could not restore " + player + " skills: {}", e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, int classIndex, L2Skill skill)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(INSERT))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, skill.getId());
			ps.setInt(3, skill.getLevel());
			ps.setInt(4, classIndex);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Error could not store char skills: {}", e);
		}
	}
	
	@Override
	public void insert(L2PcInstance player, int newClassIndex, List<L2Skill> newSkills)
	{
		if (newSkills.isEmpty())
		{
			return;
		}
		
		final int classIndex = (newClassIndex > -1) ? newClassIndex : player.getClassIndex();
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(REPLACE))
		{
			con.setAutoCommit(false);
			for (final L2Skill addSkill : newSkills)
			{
				ps.setInt(1, player.getObjectId());
				ps.setInt(2, addSkill.getId());
				ps.setInt(3, addSkill.getLevel());
				ps.setInt(4, classIndex);
				ps.addBatch();
			}
			ps.executeBatch();
			con.commit();
		}
		catch (SQLException e)
		{
			Log.error("Error could not store " + player + " skills:", e);
		}
	}
	
	@Override
	public void update(L2PcInstance player, int classIndex, L2Skill newSkill, L2Skill oldSkill)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(UPDATE))
		{
			ps.setInt(1, newSkill.getLevel());
			ps.setInt(2, oldSkill.getId());
			ps.setInt(3, player.getObjectId());
			ps.setInt(4, classIndex);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Error could not store char skills: {}", e);
		}
	}
	
	@Override
	public void delete(L2PcInstance player, L2Skill skill)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_ONE))
		{
			ps.setInt(1, skill.getId());
			ps.setInt(2, player.getObjectId());
			ps.setInt(3, player.getClassIndex());
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Error could not delete skill: {}", e);
		}
	}
	
	@Override
	public void deleteAll(L2PcInstance player, int classIndex)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(DELETE_ALL))
		{
			ps.setInt(1, player.getObjectId());
			ps.setInt(2, classIndex);
			ps.execute();
		}
		catch (Exception e)
		{
			Log.warn("Error could not delete skill: {}", e);
		}
	}
}
