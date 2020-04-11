package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.ScheduledFuture;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.managers.CursedWeaponsManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.event.CTF;
import com.l2jfrozen.gameserver.model.entity.event.DM;
import com.l2jfrozen.gameserver.model.entity.event.TvT;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.Earthquake;
import com.l2jfrozen.gameserver.network.serverpackets.ExRedSky;
import com.l2jfrozen.gameserver.network.serverpackets.ItemList;
import com.l2jfrozen.gameserver.network.serverpackets.Ride;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.L2Item;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;
import com.l2jfrozen.util.Point3D;
import com.l2jfrozen.util.database.L2DatabaseFactory;
import com.l2jfrozen.util.random.Rnd;

public class CursedWeapon
{
	private static final Logger LOGGER = Logger.getLogger(CursedWeaponsManager.class);
	
	private static final String DELETE_CURSED_WEAPON = "DELETE FROM items WHERE owner_id=? AND item_id=?";
	private static final String UPDATE_CHARACTER_KARMA_AND_PK_KILLS_BY_OBJ_ID = "UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?";
	private static final String DELETE_CURSED_WEAPONS = "DELETE FROM cursed_weapons WHERE itemId=?";
	private static final String INSERT_CURSED_WEAPONS = "INSERT INTO cursed_weapons (itemId, playerId, playerKarma, playerPkKills, nbKills, endTime) VALUES (?, ?, ?, ?, ?, ?)";
	
	private final String name;
	private final int itemId;
	private final int skillId;
	private final int skillMaxLevel;
	private int dropRate;
	private int duration;
	private int durationLost;
	private int disapearChance;
	private int stageKills;
	
	private boolean isDropped = false;
	private boolean isActivated = false;
	private ScheduledFuture<?> removeTask;
	
	private int nbKills = 0;
	private long endTime = 0;
	
	private int playerId = 0;
	private L2PcInstance cursedWeaponPlayer = null;
	private L2ItemInstance cursedWeaponItem = null;
	private int playerKarma = 0;
	private int playerPkKills = 0;
	
	// =========================================================
	// Constructor
	public CursedWeapon(final int itemId, final int skillId, final String name)
	{
		this.name = name;
		this.itemId = itemId;
		this.skillId = skillId;
		skillMaxLevel = SkillTable.getInstance().getMaxLevel(this.skillId, 0);
	}
	
	// =========================================================
	// Private
	public void endOfLife()
	{
		if (isActivated)
		{
			if (cursedWeaponPlayer != null && cursedWeaponPlayer.isOnline())
			{
				// Remove from player
				LOGGER.info(name + " being removed online.");
				
				cursedWeaponPlayer.abortAttack();
				
				cursedWeaponPlayer.setKarma(playerKarma);
				cursedWeaponPlayer.setPkKills(playerPkKills);
				cursedWeaponPlayer.setCursedWeaponEquipedId(0);
				removeSkill();
				
				// Remove and destroy
				cursedWeaponPlayer.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
				cursedWeaponPlayer.getInventory().destroyItemByItemId("", itemId, 1, cursedWeaponPlayer, null);
				cursedWeaponPlayer.store();
				
				// update inventory and userInfo
				cursedWeaponPlayer.sendPacket(new ItemList(cursedWeaponPlayer, true));
				cursedWeaponPlayer.broadcastUserInfo();
			}
			else
			{
				// Remove from DB
				LOGGER.info(name + " being removed offline.");
				
				try (Connection con = L2DatabaseFactory.getInstance().getConnection();)
				{
					// Delete the item
					try (PreparedStatement statement = con.prepareStatement(DELETE_CURSED_WEAPON))
					{
						statement.setInt(1, playerId);
						statement.setInt(2, itemId);
						
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warn("Error while deleting itemId " + itemId + " from userId " + playerId);
						}
					}
					
					// Restore the karma
					try (PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_KARMA_AND_PK_KILLS_BY_OBJ_ID))
					{
						statement.setInt(1, playerKarma);
						statement.setInt(2, playerPkKills);
						statement.setInt(3, playerId);
						
						if (statement.executeUpdate() != 1)
						{
							LOGGER.warn("Error while updating karma & pkkills for userId " + playerId);
						}
					}
				}
				catch (Exception e)
				{
					LOGGER.error("CursedWeapon.endOfLife : Something went wrong", e);
				}
			}
		}
		else
		{
			// either this cursed weapon is in the inventory of someone who has another cursed weapon equipped,
			// OR this cursed weapon is on the ground.
			if (cursedWeaponPlayer != null && cursedWeaponPlayer.getInventory().getItemByItemId(itemId) != null)
			{
				final L2ItemInstance rhand = cursedWeaponPlayer.getInventory().getPaperdollItem(Inventory.PAPERDOLL_RHAND);
				if (rhand != null)
				{
					cursedWeaponPlayer.getInventory().unEquipItemInSlotAndRecord(rhand.getEquipSlot());
				}
				
				// Destroy
				cursedWeaponPlayer.getInventory().destroyItemByItemId("", itemId, 1, cursedWeaponPlayer, null);
				cursedWeaponPlayer.store();
				
				// update inventory and userInfo
				cursedWeaponPlayer.sendPacket(new ItemList(cursedWeaponPlayer, true));
				cursedWeaponPlayer.broadcastUserInfo();
			}
			// is dropped on the ground
			else if (cursedWeaponItem != null)
			{
				cursedWeaponItem.decayMe();
				L2World.getInstance().removeObject(cursedWeaponItem);
				LOGGER.info(name + " item has been removed from World.");
			}
		}
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_DISAPPEARED);
		sm.addItemName(itemId);
		CursedWeaponsManager.announce(sm);
		sm = null;
		
		// Reset state
		cancelTask();
		isActivated = false;
		isDropped = false;
		endTime = 0;
		cursedWeaponPlayer = null;
		playerId = 0;
		playerKarma = 0;
		playerPkKills = 0;
		cursedWeaponItem = null;
		nbKills = 0;
	}
	
	private void cancelTask()
	{
		if (removeTask != null)
		{
			removeTask.cancel(true);
			removeTask = null;
		}
	}
	
	private class RemoveTask implements Runnable
	{
		protected RemoveTask()
		{
		}
		
		@Override
		public void run()
		{
			if (System.currentTimeMillis() >= getEndTime())
			{
				endOfLife();
			}
		}
	}
	
	private void dropIt(final L2Attackable attackable, final L2PcInstance player)
	{
		dropIt(attackable, player, null, true);
	}
	
	public void dropIt(final L2Attackable attackable, final L2PcInstance player, final L2Character killer, final boolean fromMonster)
	{
		isActivated = false;
		
		final SystemMessage sm = new SystemMessage(SystemMessageId.S2_WAS_DROPPED_IN_THE_S1_REGION);
		
		if (fromMonster)
		{
			
			cursedWeaponItem = attackable.DropItem(player, itemId, 1);
			cursedWeaponItem.setDropTime(0); // Prevent item from being removed by ItemsAutoDestroy
			
			// RedSky and Earthquake
			ExRedSky packet = new ExRedSky(10);
			Earthquake eq = new Earthquake(player.getX(), player.getY(), player.getZ(), 14, 3);
			
			for (final L2PcInstance aPlayer : L2World.getInstance().getAllPlayers())
			{
				aPlayer.sendPacket(packet);
				aPlayer.sendPacket(eq);
			}
			
			sm.addZoneName(attackable.getX(), attackable.getY(), attackable.getZ()); // Region Name
			
			packet = null;
			eq = null;
			
			// EndTime: if dropped from monster, the endTime is a new endTime
			cancelTask();
			endTime = 0;
			
		}
		else
		{
			// Remove from player
			cursedWeaponPlayer.abortAttack();
			
			cursedWeaponPlayer.setKarma(playerKarma);
			cursedWeaponPlayer.setPkKills(playerPkKills);
			cursedWeaponPlayer.setCursedWeaponEquipedId(0);
			removeSkill();
			
			// Remove
			cursedWeaponPlayer.getInventory().unEquipItemInBodySlotAndRecord(L2Item.SLOT_LR_HAND);
			
			// drop
			cursedWeaponPlayer.dropItem("DieDrop", cursedWeaponItem, killer, true, true);
			cursedWeaponPlayer.store();
			
			// update Inventory and UserInfo
			cursedWeaponPlayer.sendPacket(new ItemList(cursedWeaponPlayer, false));
			cursedWeaponPlayer.broadcastUserInfo();
			
			sm.addZoneName(cursedWeaponPlayer.getX(), cursedWeaponPlayer.getY(), cursedWeaponPlayer.getZ()); // Region Name
			
			// EndTime: if dropped from player, the endTime is the same then before
			// cancelTask();
			// endTime = 0;
		}
		
		sm.addItemName(itemId);
		
		// reset
		cursedWeaponPlayer = null;
		playerId = 0;
		playerKarma = 0;
		playerPkKills = 0;
		nbKills = 0;
		isDropped = true;
		
		CursedWeaponsManager.announce(sm);
	}
	
	/**
	 * Yesod:<br>
	 * Rebind the passive skill belonging to the CursedWeapon. Invoke this method if the weapon owner switches to a subclass.
	 */
	public void giveSkill()
	{
		int level = 1 + nbKills / stageKills;
		
		if (level > skillMaxLevel)
		{
			level = skillMaxLevel;
		}
		
		L2Skill skill = SkillTable.getInstance().getInfo(skillId, level);
		// Yesod:
		// To properly support subclasses this skill can not be stored.
		cursedWeaponPlayer.addSkill(skill, false);
		
		// Void Burst, Void Flow
		skill = SkillTable.getInstance().getInfo(3630, 1);
		cursedWeaponPlayer.addSkill(skill, false);
		skill = SkillTable.getInstance().getInfo(3631, 1);
		cursedWeaponPlayer.addSkill(skill, false);
		
		if (Config.DEBUG)
		{
			LOGGER.info("Player " + cursedWeaponPlayer.getName() + " has been awarded with skill " + skill);
		}
		
		cursedWeaponPlayer.sendSkillList();
	}
	
	public void removeSkill()
	{
		cursedWeaponPlayer.removeSkill(SkillTable.getInstance().getInfo(skillId, cursedWeaponPlayer.getSkillLevel(skillId)), false);
		cursedWeaponPlayer.removeSkill(SkillTable.getInstance().getInfo(3630, 1), false);
		cursedWeaponPlayer.removeSkill(SkillTable.getInstance().getInfo(3631, 1), false);
		cursedWeaponPlayer.sendSkillList();
	}
	
	// =========================================================
	// Public
	public void reActivate()
	{
		isActivated = true;
		
		if (endTime - System.currentTimeMillis() <= 0)
		{
			endOfLife();
		}
		else
		{
			removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), durationLost * 12000L, durationLost * 12000L);
		}
	}
	
	public boolean checkDrop(final L2Attackable attackable, final L2PcInstance player)
	{
		
		if (Rnd.get(100000) < dropRate)
		{
			// Drop the item
			dropIt(attackable, player);
			
			// Start the Life Task
			endTime = System.currentTimeMillis() + duration * 60000L;
			
			removeTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new RemoveTask(), durationLost * 12000L, durationLost * 12000L);
			
			return true;
		}
		
		return false;
	}
	
	public void activate(final L2PcInstance player, final L2ItemInstance item)
	{
		cursedWeaponPlayer = player;
		// if the player is mounted, attempt to unmount first. Only allow picking up
		// the zariche if unmounting is successful.
		if (player.isMounted())
		{
			if (cursedWeaponPlayer.setMountType(0))
			{
				Ride dismount = new Ride(cursedWeaponPlayer.getObjectId(), Ride.ACTION_DISMOUNT, 0);
				cursedWeaponPlayer.broadcastPacket(dismount);
				cursedWeaponPlayer.setMountObjectID(0);
				dismount = null;
			}
			else
			{
				player.sendMessage("You may not pick up this item while riding in this territory");
				return;
			}
		}
		
		if ((player.inEventTvT && !Config.TVT_JOIN_CURSED))
		{
			if (player.inEventTvT)
			{
				TvT.removePlayer(player);
			}
		}
		
		if ((player.inEventCTF && !Config.CTF_JOIN_CURSED))
		{
			if (player.inEventCTF)
			{
				CTF.removePlayer(player);
			}
		}
		
		if ((player.inEventDM && !Config.DM_JOIN_CURSED))
		{
			if (player.inEventDM)
			{
				DM.removePlayer(player);
			}
		}
		
		isActivated = true;
		
		// Player holding it data
		playerId = cursedWeaponPlayer.getObjectId();
		playerKarma = cursedWeaponPlayer.getKarma();
		playerPkKills = cursedWeaponPlayer.getPkKills();
		saveData();
		
		// Change player stats
		cursedWeaponPlayer.setCursedWeaponEquipedId(itemId);
		cursedWeaponPlayer.setKarma(9999999);
		cursedWeaponPlayer.setPkKills(0);
		
		if (cursedWeaponPlayer.isInParty())
		{
			cursedWeaponPlayer.getParty().removePartyMember(cursedWeaponPlayer);
		}
		
		if (cursedWeaponPlayer.isWearingFormalWear())
		{
			cursedWeaponPlayer.getInventory().unEquipItemInSlot(10);
		}
		// Add skill
		giveSkill();
		
		// Equip with the weapon
		cursedWeaponItem = item;
		// L2ItemInstance[] items =
		cursedWeaponPlayer.getInventory().equipItemAndRecord(cursedWeaponItem);
		
		SystemMessage sm = new SystemMessage(SystemMessageId.S1_EQUIPPED);
		sm.addItemName(cursedWeaponItem.getItemId());
		cursedWeaponPlayer.sendPacket(sm);
		sm = null;
		
		// Fully heal player
		cursedWeaponPlayer.setCurrentHpMp(cursedWeaponPlayer.getMaxHp(), cursedWeaponPlayer.getMaxMp());
		cursedWeaponPlayer.setCurrentCp(cursedWeaponPlayer.getMaxCp());
		
		// Refresh inventory
		cursedWeaponPlayer.sendPacket(new ItemList(cursedWeaponPlayer, false));
		
		// Refresh player stats
		cursedWeaponPlayer.broadcastUserInfo();
		
		SocialAction atk = new SocialAction(cursedWeaponPlayer.getObjectId(), 17);
		
		cursedWeaponPlayer.broadcastPacket(atk);
		
		sm = new SystemMessage(SystemMessageId.THE_OWNER_OF_S2_HAS_APPEARED_IN_THE_S1_REGION);
		sm.addZoneName(cursedWeaponPlayer.getX(), cursedWeaponPlayer.getY(), cursedWeaponPlayer.getZ()); // Region Name
		sm.addItemName(cursedWeaponItem.getItemId());
		CursedWeaponsManager.announce(sm);
		sm = null;
		atk = null;
	}
	
	public void saveData()
	{
		if (Config.DEBUG)
		{
			LOGGER.info("CursedWeapon: Saving data to disk.");
		}
		
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(DELETE_CURSED_WEAPONS);
			PreparedStatement statement2 = con.prepareStatement(INSERT_CURSED_WEAPONS))
		{
			// Delete previous datas
			statement.setInt(1, itemId);
			statement.executeUpdate();
			
			if (isActivated)
			{
				statement2.setInt(1, itemId);
				statement2.setInt(2, playerId);
				statement2.setInt(3, playerKarma);
				statement2.setInt(4, playerPkKills);
				statement2.setInt(5, nbKills);
				statement2.setLong(6, endTime);
				statement2.executeUpdate();
			}
		}
		catch (SQLException e)
		{
			LOGGER.error("CursedWeapon.saveData: Failed to save data", e);
		}
	}
	
	public void dropIt(final L2Character killer)
	{
		if (Rnd.get(100) <= disapearChance)
		{
			// Remove it
			endOfLife();
		}
		else
		{
			// Unequip & Drop
			dropIt(null, null, killer, false);
			
		}
	}
	
	public void increaseKills()
	{
		nbKills++;
		
		cursedWeaponPlayer.setPkKills(nbKills);
		cursedWeaponPlayer.broadcastUserInfo();
		
		if (nbKills % stageKills == 0 && nbKills <= stageKills * (skillMaxLevel - 1))
		{
			giveSkill();
		}
		
		// Reduce time-to-live
		endTime -= durationLost * 60000L;
		saveData();
	}
	
	// =========================================================
	// Setter
	public void setDisapearChance(final int disapearChance)
	{
		this.disapearChance = disapearChance;
	}
	
	public void setDropRate(final int dropRate)
	{
		this.dropRate = dropRate;
	}
	
	public void setDuration(final int duration)
	{
		this.duration = duration;
	}
	
	public void setDurationLost(final int durationLost)
	{
		this.durationLost = durationLost;
	}
	
	public void setStageKills(final int stageKills)
	{
		this.stageKills = stageKills;
	}
	
	public void setNbKills(final int nbKills)
	{
		this.nbKills = nbKills;
	}
	
	public void setPlayerId(final int playerId)
	{
		this.playerId = playerId;
	}
	
	public void setPlayerKarma(final int playerKarma)
	{
		this.playerKarma = playerKarma;
	}
	
	public void setPlayerPkKills(final int playerPkKills)
	{
		this.playerPkKills = playerPkKills;
	}
	
	public void setActivated(final boolean isActivated)
	{
		this.isActivated = isActivated;
	}
	
	public void setDropped(final boolean isDropped)
	{
		this.isDropped = isDropped;
	}
	
	public void setEndTime(final long endTime)
	{
		this.endTime = endTime;
		
	}
	
	public void setPlayer(final L2PcInstance player)
	{
		cursedWeaponPlayer = player;
	}
	
	public void setItem(final L2ItemInstance item)
	{
		cursedWeaponItem = item;
	}
	
	// =========================================================
	// Getter
	public boolean isActivated()
	{
		return isActivated;
	}
	
	public boolean isDropped()
	{
		return isDropped;
	}
	
	public long getEndTime()
	{
		return endTime;
	}
	
	public String getName()
	{
		return name;
	}
	
	public int getItemId()
	{
		return itemId;
	}
	
	public int getSkillId()
	{
		return skillId;
	}
	
	public int getPlayerId()
	{
		return playerId;
	}
	
	public L2PcInstance getPlayer()
	{
		return cursedWeaponPlayer;
	}
	
	public int getPlayerKarma()
	{
		return playerKarma;
	}
	
	public int getPlayerPkKills()
	{
		return playerPkKills;
	}
	
	public int getNbKills()
	{
		return nbKills;
	}
	
	public int getStageKills()
	{
		return stageKills;
	}
	
	public boolean isActive()
	{
		return isActivated || isDropped;
	}
	
	public int getLevel()
	{
		if (nbKills > stageKills * skillMaxLevel)
		{
			return skillMaxLevel;
		}
		return nbKills / stageKills;
	}
	
	public long getTimeLeft()
	{
		return endTime - System.currentTimeMillis();
	}
	
	public int getDuration()
	{
		return duration;
	}
	
	public void goTo(final L2PcInstance player)
	{
		if (player == null)
		{
			return;
		}
		
		if (isActivated)
		{
			// Go to player holding the weapon
			player.teleToLocation(cursedWeaponPlayer.getX(), cursedWeaponPlayer.getY(), cursedWeaponPlayer.getZ() + 20, true);
		}
		else if (isDropped)
		{
			// Go to item on the ground
			player.teleToLocation(cursedWeaponItem.getX(), cursedWeaponItem.getY(), cursedWeaponItem.getZ() + 20, true);
		}
		else
		{
			player.sendMessage(name + " isn't in the World.");
		}
	}
	
	public Point3D getWorldPosition()
	{
		if (isActivated && cursedWeaponPlayer != null)
		{
			return cursedWeaponPlayer.getPosition().getWorldPosition();
		}
		
		if (isDropped && cursedWeaponItem != null)
		{
			return cursedWeaponItem.getPosition().getWorldPosition();
		}
		
		return null;
	}
}
