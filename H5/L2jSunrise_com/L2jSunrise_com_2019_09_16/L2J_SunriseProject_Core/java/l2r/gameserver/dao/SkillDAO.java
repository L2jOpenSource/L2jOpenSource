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
package l2r.gameserver.dao;

import java.util.List;

import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.skills.L2Skill;

/**
 * Skill DAO interface.
 * @author vGodFather
 */
public interface SkillDAO
{
	void insert(L2PcInstance player, int classIndex, L2Skill skill);
	
	void update(L2PcInstance player, int classIndex, L2Skill newSkill, L2Skill oldSkill);
	
	void delete(L2PcInstance player, L2Skill skill);
	
	/**
	 * Adds or updates player's skills in the database.
	 * @param player the player
	 * @param newClassIndex if newClassIndex > -1, the skills will be stored for that class index, not the current one
	 * @param newSkills the list of skills to store
	 */
	void insert(L2PcInstance player, int newClassIndex, List<L2Skill> newSkills);
	
	/**
	 * Retrieves all skills from the database.
	 * @param player the player
	 */
	void load(L2PcInstance player);
	
	void deleteAll(L2PcInstance player, int classIndex);
}
