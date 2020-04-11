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
package l2r.gameserver.datatables.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.L2DatabaseFactory;
import l2r.gameserver.datatables.xml.ExperienceData;
import l2r.gameserver.datatables.xml.PetData;
import l2r.gameserver.datatables.xml.SkillData;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.L2PetData;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.instance.L2PetInstance;
import l2r.gameserver.model.actor.instance.L2ServitorInstance;
import l2r.gameserver.model.actor.instance.L2SiegeSummonInstance;
import l2r.gameserver.model.actor.templates.L2NpcTemplate;
import l2r.gameserver.model.items.instance.L2ItemInstance;
import l2r.gameserver.model.skills.l2skills.L2SkillSummon;
import l2r.gameserver.network.serverpackets.PetItemList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Nyaran
 */
public class CharSummonTable
{
	private static Logger _log = LoggerFactory.getLogger(CharSummonTable.class);
	private static final Map<Integer, Integer> _pets = new ConcurrentHashMap<>();
	private static final Map<Integer, Integer> _servitors = new ConcurrentHashMap<>();
	
	private static final String INIT_SUMMONS = "SELECT ownerId, summonSkillId FROM character_summons";
	private static final String INIT_PET = "SELECT ownerId, item_obj_id FROM pets WHERE restore = 'true'";
	
	private static final String SAVE_SUMMON = "REPLACE INTO character_summons (ownerId,summonSkillId,curHp,curMp,time) VALUES (?,?,?,?,?)";
	private static final String LOAD_SUMMON = "SELECT curHp, curMp, time FROM character_summons WHERE ownerId = ? AND summonSkillId = ?";
	private static final String REMOVE_SUMMON = "DELETE FROM character_summons WHERE ownerId = ?";
	
	public static CharSummonTable getInstance()
	{
		return SingletonHolder._instance;
	}
	
	public void init()
	{
		if (Config.RESTORE_SERVITOR_ON_RECONNECT)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_SUMMONS))
			{
				while (rs.next())
				{
					_servitors.put(rs.getInt("ownerId"), rs.getInt("summonSkillId"));
				}
			}
			catch (Exception e)
			{
				_log.error(getClass().getSimpleName() + ": Error while loading saved summons", e);
			}
		}
		
		if (Config.RESTORE_PET_ON_RECONNECT)
		{
			try (Connection con = L2DatabaseFactory.getInstance().getConnection();
				Statement s = con.createStatement();
				ResultSet rs = s.executeQuery(INIT_PET))
			{
				while (rs.next())
				{
					_pets.put(rs.getInt("ownerId"), rs.getInt("item_obj_id"));
				}
			}
			catch (Exception e)
			{
				_log.error(getClass().getSimpleName() + ": Error while loading saved summons", e);
			}
		}
	}
	
	public Map<Integer, Integer> getPets()
	{
		return _pets;
	}
	
	public Map<Integer, Integer> getServitors()
	{
		return _servitors;
	}
	
	public void saveSummon(L2ServitorInstance summon)
	{
		if ((summon == null) || (summon.getTimeRemaining() <= 0))
		{
			return;
		}
		_servitors.put(summon.getOwner().getObjectId(), summon.getReferenceSkill());
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(SAVE_SUMMON))
		{
			ps.setInt(1, summon.getOwner().getObjectId());
			ps.setInt(2, summon.getReferenceSkill());
			ps.setInt(3, (int) Math.round(summon.getCurrentHp()));
			ps.setInt(4, (int) Math.round(summon.getCurrentMp()));
			ps.setInt(5, summon.getTimeRemaining());
			ps.execute();
		}
		catch (Exception e)
		{
			_log.error(getClass().getSimpleName() + ": Failed to store summon [SummonId: " + summon.getId() + "] from Char [CharId: " + summon.getOwner().getObjectId() + "] data", e);
		}
	}
	
	public void restoreServitor(L2PcInstance activeChar)
	{
		int skillId = _servitors.get(activeChar.getObjectId());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(LOAD_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.setInt(2, skillId);
			try (ResultSet rs = ps.executeQuery())
			{
				
				L2NpcTemplate summonTemplate;
				L2ServitorInstance summon;
				L2SkillSummon skill;
				
				while (rs.next())
				{
					int curHp = rs.getInt("curHp");
					int curMp = rs.getInt("curMp");
					int time = rs.getInt("time");
					
					skill = (L2SkillSummon) SkillData.getInstance().getInfo(skillId, activeChar.getSkillLevel(skillId));
					if (skill == null)
					{
						removeServitor(activeChar);
						return;
					}
					
					summonTemplate = NpcTable.getInstance().getTemplate(skill.getNpcId());
					if (summonTemplate == null)
					{
						_log.warn(getClass().getSimpleName() + ": Summon attemp for nonexisting Skill ID:" + skillId);
						return;
					}
					
					final int id = IdFactory.getInstance().getNextId();
					if (summonTemplate.isType("L2SiegeSummon"))
					{
						summon = new L2SiegeSummonInstance(id, summonTemplate, activeChar, skill);
					}
					else if (summonTemplate.isType("L2MerchantSummon"))
					{
						// TODO: Confirm L2Merchant summon = new L2MerchantSummonInstance(id, summonTemplate, activeChar, skill);
						summon = new L2ServitorInstance(id, summonTemplate, activeChar, skill);
					}
					else
					{
						summon = new L2ServitorInstance(id, summonTemplate, activeChar, skill);
					}
					
					summon.setName(summonTemplate.getName());
					summon.setTitle(activeChar.getName());
					summon.setExpPenalty(skill.getExpPenalty());
					summon.setSharedElementals(skill.getInheritElementals());
					summon.setSharedElementalsValue(skill.getElementalSharePercent());
					
					if (summon.getLevel() >= ExperienceData.getInstance().getMaxPetLevel())
					{
						summon.getStat().setExp(ExperienceData.getInstance().getExpForLevel(ExperienceData.getInstance().getMaxPetLevel() - 1));
						_log.warn(getClass().getSimpleName() + ": Summon (" + summon.getName() + ") NpcID: " + summon.getId() + " has a level above " + ExperienceData.getInstance().getMaxPetLevel() + ". Please rectify.");
					}
					else
					{
						summon.getStat().setExp(ExperienceData.getInstance().getExpForLevel(summon.getLevel() % ExperienceData.getInstance().getMaxPetLevel()));
					}
					summon.setCurrentHp(curHp);
					summon.setCurrentMp(curMp);
					summon.setHeading(activeChar.getHeading());
					summon.setRunning();
					activeChar.setPet(summon);
					summon.setTimeRemaining(time);
					
					// L2World.getInstance().storeObject(summon);
					summon.spawnMe(activeChar.getX() + 20, activeChar.getY() + 20, activeChar.getZ());
				}
			}
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Summon cannot be restored: ", e);
		}
	}
	
	public void removeServitor(L2PcInstance activeChar)
	{
		_servitors.remove(activeChar.getObjectId());
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement ps = con.prepareStatement(REMOVE_SUMMON))
		{
			ps.setInt(1, activeChar.getObjectId());
			ps.execute();
		}
		catch (SQLException e)
		{
			_log.warn(getClass().getSimpleName() + ": Summon cannot be removed: ", e);
		}
	}
	
	public void restorePet(L2PcInstance activeChar)
	{
		L2ItemInstance item = activeChar.getInventory().getItemByObjectId(_pets.get(activeChar.getObjectId()));
		
		if (item == null)
		{
			return;
		}
		
		final L2PetData petData = PetData.getInstance().getPetDataByItemId(item.getId());
		L2NpcTemplate npcTemplate = NpcTable.getInstance().getTemplate(petData.getNpcId());
		
		if (npcTemplate == null)
		{
			return;
		}
		
		final L2PetInstance pet = L2PetInstance.spawnPet(npcTemplate, activeChar, item);
		if (pet == null)
		{
			return;
		}
		
		pet.setShowSummonAnimation(true);
		pet.setTitle(activeChar.getName());
		
		if (!pet.isRespawned())
		{
			pet.setCurrentHp(pet.getMaxHp());
			pet.setCurrentMp(pet.getMaxMp());
			pet.getStat().setExp(pet.getExpForThisLevel());
			pet.setCurrentFed(pet.getMaxFed());
		}
		
		pet.setRunning();
		
		if (!pet.isRespawned())
		{
			pet.store();
		}
		
		item.setEnchantLevel(pet.getLevel());
		activeChar.setPet(pet);
		pet.spawnMe(activeChar.getX() + 50, activeChar.getY() + 100, activeChar.getZ());
		pet.startFeed();
		pet.setFollowStatus(true);
		pet.getOwner().sendPacket(new PetItemList(pet.getInventory().getItems()));
		pet.broadcastStatusUpdate();
	}
	
	private static class SingletonHolder
	{
		protected static final CharSummonTable _instance = new CharSummonTable();
	}
}
