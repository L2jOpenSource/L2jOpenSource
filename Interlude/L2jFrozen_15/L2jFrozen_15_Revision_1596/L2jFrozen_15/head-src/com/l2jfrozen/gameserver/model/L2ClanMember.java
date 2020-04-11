package com.l2jfrozen.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.util.database.L2DatabaseFactory;

/**
 * This class ...
 * @version $Revision: 1.5.4.2 $ $Date: 2005/03/27 15:29:33 $
 */
public class L2ClanMember
{
	private static final Logger LOGGER = Logger.getLogger(L2ClanMember.class);
	private static final String UPDATE_CHARACTER_SUBPLEDGE_BY_OBJ_ID = "UPDATE characters SET subpledge=? WHERE obj_id=?";
	private static final String UPDATE_CHARACTER_APPRENTICE_AND_SPONSOR_BY_OBJ_ID = "UPDATE characters SET apprentice=?,sponsor=? WHERE obj_Id=?";
	private static final String UPDATE_CHARACTER_POWER_GRADE = "UPDATE characters SET power_grade=? WHERE obj_id=?";
	
	private final L2Clan clan;
	private int objectId;
	private String name;
	private String title;
	private int powerGrade;
	private int level;
	private int classId;
	private L2PcInstance playerInstance;
	private int pledgeType;
	private int clanApprentice;
	private int clanSponsor;
	
	public L2ClanMember(final L2Clan clan, final String name, final int level, final int classId, final int objectId, final int pledgeType, final int powerGrade, final String title)
	{
		if (clan == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember with a null clan.");
		}
		this.clan = clan;
		this.name = name;
		this.level = level;
		this.classId = classId;
		this.objectId = objectId;
		this.powerGrade = powerGrade;
		this.title = title;
		this.pledgeType = pledgeType;
		clanApprentice = 0;
		clanSponsor = 0;
		
	}
	
	public L2ClanMember(final L2PcInstance player)
	{
		if (player.getClan() == null)
		{
			throw new IllegalArgumentException("Can not create a ClanMember if player has a null clan.");
		}
		
		clan = player.getClan();
		playerInstance = player;
		name = playerInstance.getName();
		level = playerInstance.getLevel();
		classId = playerInstance.getClassId().getId();
		objectId = playerInstance.getObjectId();
		powerGrade = playerInstance.getPowerGrade();
		pledgeType = playerInstance.getPledgeType();
		title = playerInstance.getTitle();
		clanApprentice = 0;
		clanSponsor = 0;
	}
	
	public void setPlayerInstance(final L2PcInstance player)
	{
		if (player == null && playerInstance != null)
		{
			final L2PcInstance local_player = playerInstance;
			
			// this is here to keep the data when the player logs off
			name = local_player.getName();
			level = local_player.getLevel();
			classId = local_player.getClassId().getId();
			objectId = local_player.getObjectId();
			powerGrade = local_player.getPowerGrade();
			pledgeType = local_player.getPledgeType();
			title = local_player.getTitle();
			clanApprentice = local_player.getApprentice();
			clanSponsor = local_player.getSponsor();
		}
		
		if (player != null)
		{
			if (clan.getLevel() > 3 && player.isClanLeader())
			{
				SiegeManager.getInstance().addSiegeSkills(player);
			}
			
			if (clan.getReputationScore() >= 0)
			{
				final L2Skill[] skills = clan.getAllSkills();
				for (final L2Skill sk : skills)
				{
					if (sk.getMinPledgeClass() <= player.getPledgeClass())
					{
						player.addSkill(sk, false);
					}
				}
			}
		}
		
		playerInstance = player;
	}
	
	public L2PcInstance getPlayerInstance()
	{
		return playerInstance;
	}
	
	public boolean isOnline()
	{
		return playerInstance != null;
	}
	
	/**
	 * @return Returns the classId.
	 */
	public int getClassId()
	{
		if (playerInstance != null)
		{
			return playerInstance.getClassId().getId();
		}
		return classId;
	}
	
	/**
	 * @return Returns the level.
	 */
	public int getLevel()
	{
		if (playerInstance != null)
		{
			return playerInstance.getLevel();
		}
		return level;
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		if (playerInstance != null)
		{
			return playerInstance.getName();
		}
		return name;
	}
	
	/**
	 * @return Returns the objectId.
	 */
	public int getObjectId()
	{
		if (playerInstance != null)
		{
			return playerInstance.getObjectId();
		}
		return objectId;
	}
	
	public String getTitle()
	{
		if (playerInstance != null)
		{
			return playerInstance.getTitle();
		}
		return title;
	}
	
	public int getPledgeType()
	{
		if (playerInstance != null)
		{
			return playerInstance.getPledgeType();
		}
		return pledgeType;
	}
	
	public void setPledgeType(final int pledgeType)
	{
		this.pledgeType = pledgeType;
		if (playerInstance != null)
		{
			playerInstance.setPledgeType(pledgeType);
		}
		else
		{
			// db save if char not logged in
			updatePledgeType();
		}
	}
	
	public void updatePledgeType()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_SUBPLEDGE_BY_OBJ_ID))
		{
			statement.setLong(1, pledgeType);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2ClanMember.updatePledgeType : Could not update character subpledge", e);
		}
	}
	
	public int getPowerGrade()
	{
		if (playerInstance != null)
		{
			return playerInstance.getPowerGrade();
		}
		return powerGrade;
	}
	
	/**
	 * @param powerGrade
	 */
	public void setPowerGrade(final int powerGrade)
	{
		this.powerGrade = powerGrade;
		if (playerInstance != null)
		{
			playerInstance.setPowerGrade(powerGrade);
		}
		else
		{
			// db save if char not logged in
			updatePowerGrade();
		}
	}
	
	/**
	 * Update the characters table of the database with power grade.<BR>
	 * <BR>
	 */
	public void updatePowerGrade()
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_POWER_GRADE))
		{
			statement.setLong(1, powerGrade);
			statement.setInt(2, getObjectId());
			statement.executeUpdate();
		}
		catch (Exception e)
		{
			LOGGER.error("L2ClanMember.updatePowerGrade : Coul not update character power_grade", e);
		}
	}
	
	public void initApprenticeAndSponsor(final int apprenticeID, final int sponsorID)
	{
		clanApprentice = apprenticeID;
		clanSponsor = sponsorID;
	}
	
	public int getSponsor()
	{
		if (playerInstance != null)
		{
			return playerInstance.getSponsor();
		}
		return clanSponsor;
	}
	
	public int getApprentice()
	{
		if (playerInstance != null)
		{
			return playerInstance.getApprentice();
		}
		return clanApprentice;
	}
	
	public String getApprenticeOrSponsorName()
	{
		if (playerInstance != null)
		{
			clanApprentice = playerInstance.getApprentice();
			clanSponsor = playerInstance.getSponsor();
		}
		
		if (clanApprentice != 0)
		{
			final L2ClanMember apprentice = clan.getClanMember(clanApprentice);
			if (apprentice != null)
			{
				return apprentice.getName();
			}
			return "Error";
		}
		if (clanSponsor != 0)
		{
			final L2ClanMember sponsor = clan.getClanMember(clanSponsor);
			if (sponsor != null)
			{
				return sponsor.getName();
			}
			return "Error";
		}
		return "";
	}
	
	public L2Clan getClan()
	{
		return clan;
	}
	
	public int calculatePledgeClass(final L2PcInstance player)
	{
		int pledgeClass = 0;
		
		if (player == null)
		{
			return pledgeClass;
		}
		
		L2Clan clan = player.getClan();
		
		if (clan != null)
		{
			switch (player.getClan().getLevel())
			{
				case 4:
					if (player.isClanLeader())
					{
						pledgeClass = 3;
					}
					break;
				case 5:
					if (player.isClanLeader())
					{
						pledgeClass = 4;
					}
					else
					{
						pledgeClass = 2;
					}
					break;
				case 6:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 5;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 4;
										break;
									case -1:
									default:
										pledgeClass = 3;
										break;
								}
							}
							break;
					}
					break;
				case 7:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 3;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 2;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 7;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 6;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 5;
										break;
									case -1:
									default:
										pledgeClass = 4;
										break;
								}
							}
							break;
					}
					break;
				case 8:
					switch (player.getPledgeType())
					{
						case -1:
							pledgeClass = 1;
							break;
						case 100:
						case 200:
							pledgeClass = 4;
							break;
						case 1001:
						case 1002:
						case 2001:
						case 2002:
							pledgeClass = 3;
							break;
						case 0:
							if (player.isClanLeader())
							{
								pledgeClass = 8;
							}
							else
							{
								switch (clan.getLeaderSubPledge(player.getName()))
								{
									case 100:
									case 200:
										pledgeClass = 7;
										break;
									case 1001:
									case 1002:
									case 2001:
									case 2002:
										pledgeClass = 6;
										break;
									case -1:
									default:
										pledgeClass = 5;
										break;
								}
							}
							break;
					}
					break;
				default:
					pledgeClass = 1;
					break;
			}
		}
		
		clan = null;
		
		return pledgeClass;
	}
	
	public void saveApprenticeAndSponsor(final int apprentice, final int sponsor)
	{
		try (Connection con = L2DatabaseFactory.getInstance().getConnection();
			PreparedStatement statement = con.prepareStatement(UPDATE_CHARACTER_APPRENTICE_AND_SPONSOR_BY_OBJ_ID))
		{
			statement.setInt(1, apprentice);
			statement.setInt(2, sponsor);
			statement.setInt(3, getObjectId());
			statement.executeUpdate();
		}
		catch (SQLException e)
		{
			LOGGER.error("L2ClanMember.saveApprenticeAndSponsor : Could not update character apprentice and sponsor", e);
		}
	}
}
