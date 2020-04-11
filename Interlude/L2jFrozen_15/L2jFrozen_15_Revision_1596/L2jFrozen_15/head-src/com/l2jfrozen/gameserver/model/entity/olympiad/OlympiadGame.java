package com.l2jfrozen.gameserver.model.entity.olympiad;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.datatables.HeroSkillTable;
import com.l2jfrozen.gameserver.datatables.SkillTable;
import com.l2jfrozen.gameserver.model.L2Party;
import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.model.L2Summon;
import com.l2jfrozen.gameserver.model.actor.instance.L2CubicInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2ItemInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PetInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2SummonInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2TamedBeastInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad.COMP_TYPE;
import com.l2jfrozen.gameserver.model.spawn.L2Spawn;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.CreatureSay;
import com.l2jfrozen.gameserver.network.serverpackets.ExAutoSoulShot;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadMode;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadSpelledInfo;
import com.l2jfrozen.gameserver.network.serverpackets.ExOlympiadUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.InventoryUpdate;
import com.l2jfrozen.gameserver.network.serverpackets.MagicSkillUser;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.templates.StatsSet;
import com.l2jfrozen.logs.Log;
import com.l2jfrozen.util.L2FastList;

/**
 * @author GodKratos
 */
class OlympiadGame
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadGame.class);
	protected COMP_TYPE type;
	protected boolean aborted;
	protected boolean gamestarted;
	protected boolean playerOneDisconnected;
	protected boolean playerTwoDisconnected;
	protected boolean playerOneDefaulted;
	protected boolean playerTwoDefaulted;
	protected String playerOneName;
	protected String playerTwoName;
	protected List<L2Skill> playerOneSkills = new ArrayList<>();
	protected List<L2Skill> playerTwoSkills = new ArrayList<>();
	
	private static final String POINTS = "olympiad_points";
	private static final String COMP_DONE = "competitions_done";
	private static final String COMP_WON = "competitions_won";
	private static final String COMP_LOST = "competitions_lost";
	private static final String COMP_DRAWN = "competitions_drawn";
	protected static boolean battleStarted;
	
	public int damageP1 = 0;
	public int damageP2 = 0;
	
	public L2PcInstance playerOne;
	public L2PcInstance playerTwo;
	protected L2FastList<L2PcInstance> players;
	private int[] stadiumPort;
	private int x1, y1, z1, x2, y2, z2;
	public int stadiumID;
	private SystemMessage sm;
	private SystemMessage sm2;
	private SystemMessage sm3;
	
	protected OlympiadGame(final int id, final COMP_TYPE type, final L2FastList<L2PcInstance> list)
	{
		aborted = false;
		gamestarted = false;
		stadiumID = id;
		playerOneDisconnected = false;
		playerTwoDisconnected = false;
		this.type = type;
		stadiumPort = OlympiadManager.STADIUMS[id].getCoordinates();
		
		if (list != null)
		{
			players = list;
			playerOne = list.get(0);
			playerTwo = list.get(1);
			
			try
			{
				playerOneName = playerOne.getName();
				playerTwoName = playerTwo.getName();
				playerOne.setOlympiadGameId(id);
				playerTwo.setOlympiadGameId(id);
				playerOneSkills = new ArrayList<>();
				playerTwoSkills = new ArrayList<>();
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_OLYMPIAD_DEBUG)
				{
					LOGGER.warn("Olympiad System: Game - " + id + " aborted due to ", e);
				}
				
				aborted = true;
				clearPlayers();
			}
			
			if (Config.ENABLE_OLYMPIAD_DEBUG)
			{
				final String text = "Olympiad System: Game - " + id + ": " + playerOne.getName() + " Vs " + playerTwo.getName();
				Log.add(text, "Olympiad_game_logs");
			}
		}
		else
		{
			if (Config.ENABLE_OLYMPIAD_DEBUG)
			{
				final String text = "Olympiad System: Game - " + id + " aborted beacause player list is null";
				Log.add(text, "Olympiad_game_logs");
			}
			
			aborted = true;
			clearPlayers();
			return;
		}
	}
	
	public boolean isAborted()
	{
		return aborted;
	}
	
	protected final void clearPlayers()
	{
		playerOne = null;
		playerTwo = null;
		players = null;
		playerOneName = "";
		playerTwoName = "";
		playerOneSkills.clear();
		playerTwoSkills.clear();
	}
	
	protected void handleDisconnect(final L2PcInstance player)
	{
		if (gamestarted)
		{
			if (Config.ENABLE_OLYMPIAD_DEBUG)
			{
				final String text = "Olympiad System: Game - " + stadiumID + " player " + player.getName() + " of account " + player.getAccountName() + " has been disconnected";
				Log.add(text, "Olympiad_game_logs");
			}
			
			if (player == playerOne)
			{
				playerOneDisconnected = true;
			}
			else if (player == playerTwo)
			{
				playerTwoDisconnected = true;
			}
		}
	}
	
	protected void removals()
	{
		if (aborted)
		{
			return;
		}
		
		if (playerOne == null || playerTwo == null)
		{
			return;
		}
		if (playerOneDisconnected || playerTwoDisconnected)
		{
			return;
		}
		
		for (final L2PcInstance player : players)
		{
			try
			{
				// Remove Clan Skills
				if (player.getClan() != null)
				{
					for (final L2Skill skill : player.getClan().getAllSkills())
					{
						player.removeSkill(skill, false);
					}
				}
				// Abort casting if player casting
				if (player.isCastingNow())
				{
					player.abortCast();
				}
				
				// Force the character to be visible
				player.getAppearance().setVisible();
				
				// Remove Hero Skills
				if (player.isHero())
				{
					for (final L2Skill skill : HeroSkillTable.getHeroSkills())
					{
						player.removeSkill(skill, false);
					}
				}
				
				// Remove Restricted skills
				for (final L2Skill skill : player.getAllSkills())
				{
					if (Config.LIST_OLY_RESTRICTED_SKILLS.contains(skill.getId()))
					{
						if (player.getObjectId() == playerOne.getObjectId())
						{
							playerOneSkills.add(skill);
						}
						else
						{
							playerTwoSkills.add(skill);
						}
						player.removeSkill(skill, false);
					}
				}
				
				// Heal Player fully
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				
				// Remove Buffs
				player.stopAllEffects();
				
				// Remove Summon's Buffs
				if (player.getPet() != null)
				{
					final L2Summon summon = player.getPet();
					summon.stopAllEffects();
					
					if (summon instanceof L2PetInstance)
					{
						summon.unSummon(player);
					}
				}
				
				// Remove Tamed Beast
				if (player.getTrainedBeast() != null)
				{
					final L2TamedBeastInstance traindebeast = player.getTrainedBeast();
					traindebeast.stopAllEffects();
					
					traindebeast.doDespawn();
				}
				
				if (Config.REMOVE_CUBIC_OLYMPIAD)
				{
					if (player.getCubics() != null)
					{
						for (final L2CubicInstance cubic : player.getCubics().values())
						{
							cubic.stopAction();
							player.delCubic(cubic.getId());
						}
						player.getCubics().clear();
					}
				}
				else if (player.getCubics() != null)
				{
					boolean removed = false;
					for (final L2CubicInstance cubic : player.getCubics().values())
					{
						if (cubic.givenByOther())
						{
							cubic.stopAction();
							player.delCubic(cubic.getId());
							removed = true;
						}
					}
					if (removed)
					{
						player.broadcastUserInfo();
					}
				}
				
				// Remove player from his party
				if (player.getParty() != null)
				{
					final L2Party party = player.getParty();
					party.removePartyMember(player);
				}
				
				player.checkItemRestriction();
				
				// Remove shot automation
				final Map<Integer, Integer> activeSoulShots = player.getAutoSoulShot();
				for (final int itemId : activeSoulShots.values())
				{
					player.removeAutoSoulShot(itemId);
					final ExAutoSoulShot atk = new ExAutoSoulShot(itemId, 0);
					player.sendPacket(atk);
				}
				
				// Discharge any active shots
				if (player.getActiveWeaponInstance() != null)
				{
					player.getActiveWeaponInstance().setChargedSoulshot(L2ItemInstance.CHARGED_NONE);
					player.getActiveWeaponInstance().setChargedSpiritshot(L2ItemInstance.CHARGED_NONE);
				}
				
				// Skill recharge is a Gracia Final feature, but we have it configurable ;)
				if (Config.ALT_OLY_RECHARGE_SKILLS)
				{
					for (final L2Skill skill : player.getAllSkills())
					{
						if (skill.getId() != 1324)
						{
							player.enableSkill(skill);
						}
					}
					
					player.updateEffectIcons();
				}
				
				player.sendSkillList();
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + player.getName() + " removals, an error has been occurred:", e);
			}
		}
	}
	
	protected boolean portPlayersToArena()
	{
		final boolean playerOneCrash = (playerOne == null || playerOneDisconnected);
		final boolean playerTwoCrash = (playerTwo == null || playerTwoDisconnected);
		
		if (playerOneCrash || playerTwoCrash || aborted)
		{
			playerOne = null;
			playerTwo = null;
			aborted = true;
			return false;
		}
		
		if (playerOne.inObserverMode() || playerTwo.inObserverMode())
		{
			if (playerOne.inObserverMode())
			{
				LOGGER.warn("[OLYMPIAD DEBUG] Player one " + playerOne.getName() + " was on observer mode! Match aborted!");
			}
			
			if (playerTwo.inObserverMode())
			{
				LOGGER.warn("[OLYMPIAD DEBUG] Player two " + playerTwo.getName() + " was on observer mode! Match aborted!");
			}
			
			playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			
			aborted = true;
			return false;
		}
		
		try
		{
			x1 = playerOne.getX();
			y1 = playerOne.getY();
			z1 = playerOne.getZ();
			
			x2 = playerTwo.getX();
			y2 = playerTwo.getY();
			z2 = playerTwo.getZ();
			
			if (playerOne.isSitting())
			{
				playerOne.standUp();
			}
			
			if (playerTwo.isSitting())
			{
				playerTwo.standUp();
			}
			
			playerOne.setTarget(null);
			playerTwo.setTarget(null);
			
			playerOne.teleToLocation(stadiumPort[0] + 900, stadiumPort[1], stadiumPort[2], false);
			// teleport summon to
			if (playerOne.getPet() != null)
			{
				final L2Summon summon = playerOne.getPet();
				if (summon instanceof L2SummonInstance)
				{
					summon.teleToLocation(stadiumPort[0] + 900, stadiumPort[1], stadiumPort[2], false);
				}
			}
			playerTwo.teleToLocation(stadiumPort[0] - 900, stadiumPort[1], stadiumPort[2], false);
			// teleport summon to
			if (playerTwo.getPet() != null)
			{
				final L2Summon summon = playerTwo.getPet();
				if (summon instanceof L2SummonInstance)
				{
					summon.teleToLocation(stadiumPort[0] - 900, stadiumPort[1], stadiumPort[2], false);
				}
			}
			
			playerOne.sendPacket(new ExOlympiadMode(2, playerOne));
			playerTwo.sendPacket(new ExOlympiadMode(2, playerTwo));
			
			playerOne.setIsInOlympiadFight(false);
			playerOne.setOlympiadSide(1);
			
			playerTwo.setIsInOlympiadFight(false);
			playerTwo.setOlympiadSide(2);
		}
		catch (final NullPointerException e)
		{
			LOGGER.warn("Olympiad System: Game - " + stadiumID + " on players portPlayersToArena, an error has been occurred:", e);
			return false;
		}
		return true;
	}
	
	protected void additions()
	{
		for (final L2PcInstance player : players)
		{
			try
			{
				// Set HP/CP/MP to Max
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				// Wind Walk Buff for Both
				L2Skill skill;
				SystemMessage sm;
				skill = SkillTable.getInstance().getInfo(1204, 2);
				skill.getEffects(player, player);
				player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 2, skill.getHitTime(), 0));
				sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
				sm.addSkillName(1204);
				player.sendPacket(sm);
				if (!player.isMageClass())
				{
					// Haste Buff to Fighters
					skill = SkillTable.getInstance().getInfo(1086, 1);
					skill.getEffects(player, player);
					player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 1, skill.getHitTime(), 0));
					sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(1086);
					player.sendPacket(sm);
				}
				else
				{
					// Acumen Buff to Mages
					skill = SkillTable.getInstance().getInfo(1085, 1);
					skill.getEffects(player, player);
					player.broadcastPacket(new MagicSkillUser(player, player, skill.getId(), 1, skill.getHitTime(), 0));
					sm = new SystemMessage(SystemMessageId.YOU_FEEL_S1_EFFECT);
					sm.addSkillName(1085);
					player.sendPacket(sm);
				}
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + player.getName() + " additions, an error has been occurred:", e);
			}
		}
	}
	
	protected void sendMessageToPlayers(final boolean toBattleBegin, final int nsecond)
	{
		if (!toBattleBegin)
		{
			sm = new SystemMessage(SystemMessageId.YOU_WILL_ENTER_THE_OLYMPIAD_STADIUM_IN_S1_SECOND_S);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.THE_GAME_WILL_START_IN_S1_SECOND_S);
		}
		
		sm.addNumber(nsecond);
		
		for (final L2PcInstance player : players)
		{
			try
			{
				player.sendPacket(sm);
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + player.getName() + " sendMessage, an error has been occurred:", e);
				
			}
		}
		
	}
	
	protected void portPlayersBack()
	{
		playerOne.setIsInOlympiadMode(false);
		playerTwo.setIsInOlympiadMode(false);
		
		playerOne.setIsInOlympiadFight(false);
		playerTwo.setIsInOlympiadFight(false);
		
		if (playerOne != null && x1 != 0)
		{
			playerOne.teleToLocation(x1, y1, z1, true);
		}
		
		if (playerTwo != null && x2 != 0)
		{
			playerTwo.teleToLocation(x2, y2, z2, true);
		}
	}
	
	protected void playersStatusBack()
	{
		for (final L2PcInstance player : players)
		{
			try
			{
				player.getStatus().startHpMpRegeneration();
				player.setCurrentCp(player.getMaxCp());
				player.setCurrentHp(player.getMaxHp());
				player.setCurrentMp(player.getMaxMp());
				player.setIsInOlympiadMode(false);
				player.setIsInOlympiadFight(false);
				player.setOlympiadSide(-1);
				player.setOlympiadGameId(-1);
				player.sendPacket(new ExOlympiadMode(0, player));
				
				// Add Clan Skills
				if (player.getClan() != null)
				{
					for (final L2Skill skill : player.getClan().getAllSkills())
					{
						if (skill.getMinPledgeClass() <= player.getPledgeClass())
						{
							player.addSkill(skill, false);
						}
					}
				}
				
				// Add Hero Skills
				if (player.isHero())
				{
					for (final L2Skill skill : HeroSkillTable.getHeroSkills())
					{
						player.addSkill(skill, false);
					}
				}
				
				// Return Restricted Skills
				List<L2Skill> rskills;
				if (player.getObjectId() == playerOne.getObjectId())
				{
					rskills = playerOneSkills;
				}
				else
				{
					rskills = playerTwoSkills;
				}
				for (final L2Skill skill : rskills)
				{
					player.addSkill(skill, false);
				}
				rskills.clear();
				
				player.sendSkillList();
			}
			catch (final Exception e)
			{
				
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + player.getName() + " PlayersStatusBack, an error has been occurred:", e);
				
			}
		}
	}
	
	protected boolean haveWinner()
	{
		if (aborted || playerOne == null || playerTwo == null || playerOneDisconnected || playerTwoDisconnected)
		{
			return true;
		}
		
		double playerOneHp = 0;
		
		try
		{
			if (playerOne != null && playerOne.getOlympiadGameId() != -1)
			{
				playerOneHp = playerOne.getCurrentHp();
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + playerOne.getName() + " haveWinner, an error has been occurred:", e);
			playerOneHp = 0;
		}
		
		double playerTwoHp = 0;
		try
		{
			if (playerTwo != null && playerTwo.getOlympiadGameId() != -1)
			{
				playerTwoHp = playerTwo.getCurrentHp();
			}
		}
		catch (final Exception e)
		{
			LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + playerTwo.getName() + " haveWinner, an error has been occurred:", e);
			playerTwoHp = 0;
		}
		
		if (playerTwoHp <= 0 || playerOneHp <= 0)
		{
			return true;
		}
		
		return false;
	}
	
	protected void validateWinner()
	{
		if (aborted)
		{
			return;
		}
		
		final boolean pOneCrash = (playerOne == null || playerOneDisconnected);
		final boolean pTwoCrash = (playerTwo == null || playerTwoDisconnected);
		
		int div;
		int gpreward;
		
		String classed;
		switch (type)
		{
			case NON_CLASSED:
				div = 5;
				gpreward = Config.ALT_OLY_NONCLASSED_RITEM_C;
				classed = "no";
				break;
			default:
				div = 3;
				gpreward = Config.ALT_OLY_CLASSED_RITEM_C;
				classed = "yes";
				break;
		}
		
		final StatsSet playerOneStat = Olympiad.getNobleStats(playerOne.getObjectId());
		final StatsSet playerTwoStat = Olympiad.getNobleStats(playerTwo.getObjectId());
		
		final int playerOnePlayed = playerOneStat.getInteger(COMP_DONE);
		final int playerTwoPlayed = playerTwoStat.getInteger(COMP_DONE);
		final int playerOneWon = playerOneStat.getInteger(COMP_WON);
		final int playerTwoWon = playerTwoStat.getInteger(COMP_WON);
		final int playerOneLost = playerOneStat.getInteger(COMP_LOST);
		final int playerTwoLost = playerTwoStat.getInteger(COMP_LOST);
		final int playerOneDrawn = playerOneStat.getInteger(COMP_DRAWN);
		final int playerTwoDrawn = playerTwoStat.getInteger(COMP_DRAWN);
		
		final int playerOnePoints = playerOneStat.getInteger(POINTS);
		final int playerTwoPoints = playerTwoStat.getInteger(POINTS);
		final int pointDiff = Math.min(playerOnePoints, playerTwoPoints) / div;
		
		// Check for if a player defaulted before battle started
		if (playerOneDefaulted || playerTwoDefaulted)
		{
			if (playerOneDefaulted)
			{
				final int lostPoints = playerOnePoints / 3;
				playerOneStat.set(POINTS, playerOnePoints - lostPoints);
				Olympiad.updateNobleStats(playerOne.getObjectId(), playerOneStat);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
				sm.addString(playerOneName);
				sm.addNumber(lostPoints);
				broadcastMessage(sm, false);
				
				String text = "Olympiad result: " + playerOneName + " lost " + lostPoints + " points for leave before combat";
				Log.add(text, "olympiad_game");
				
				Olympiad.logResult(playerOneName, playerTwoName, 0D, 0D, 0, 0, playerOneName + " default", lostPoints, classed);
			}
			if (playerTwoDefaulted)
			{
				final int lostPoints = playerTwoPoints / 3;
				playerTwoStat.set(POINTS, playerTwoPoints - lostPoints);
				Olympiad.updateNobleStats(playerTwo.getObjectId(), playerTwoStat);
				final SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
				sm.addString(playerTwoName);
				sm.addNumber(lostPoints);
				broadcastMessage(sm, false);
				
				String text = "Olympiad result: " + playerTwoName + " lost " + lostPoints + " points for leave before combat";
				Log.add(text, "olympiad_game");
				
				Olympiad.logResult(playerOneName, playerTwoName, 0D, 0D, 0, 0, playerTwoName + " default", lostPoints, classed);
			}
			return;
		}
		
		// Create results for players if a player crashed
		if (pOneCrash || pTwoCrash)
		{
			if (pOneCrash && !pTwoCrash)
			{
				try
				{
					playerOneStat.set(POINTS, playerOnePoints - pointDiff);
					playerOneStat.set(COMP_LOST, playerOneLost + 1);
					
					String text = "Olympiad result: " + playerOneName + " vs " + playerTwoName + " ... " + playerOneName + " lost " + pointDiff + " points for game crash";
					Log.add(text, "olympiad_game");
					
					Olympiad.logResult(playerOneName, playerTwoName, 0D, 0D, 0, 0, playerOneName + " crash", pointDiff, classed);
					
					playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
					playerTwoStat.set(COMP_WON, playerTwoWon + 1);
					
					text = "Olympiad result: " + playerOneName + " vs " + playerTwoName + " ... " + playerTwoName + " Win " + pointDiff + " points";
					Log.add(text, "olympiad_game");
					
					sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
					sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
					sm.addString(playerTwoName);
					broadcastMessage(sm, true);
					sm2.addString(playerTwoName);
					sm2.addNumber(pointDiff);
					broadcastMessage(sm2, false);
				}
				catch (final Exception e)
				{
					LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player crashed evaluation, an error has been occurred:", e);
				}
				
			}
			else if (pTwoCrash && !pOneCrash)
			{
				try
				{
					playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
					playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
					
					String text = "Olympiad result: " + playerTwoName + " vs " + playerOneName + " ... " + playerTwoName + " lost " + pointDiff + " points for game crash";
					Log.add(text, "olympiad_game");
					
					Olympiad.logResult(playerOneName, playerTwoName, 0D, 0D, 0, 0, playerTwoName + " crash", pointDiff, classed);
					
					playerOneStat.set(POINTS, playerOnePoints + pointDiff);
					playerOneStat.set(COMP_WON, playerOneWon + 1);
					
					text = "Olympiad result: " + playerTwoName + " vs " + playerOneName + " ... " + playerOneName + " Win " + pointDiff + " points";
					Log.add(text, "olympiad_game");
					
					sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
					sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
					sm.addString(playerOneName);
					broadcastMessage(sm, true);
					sm2.addString(playerOneName);
					sm2.addNumber(pointDiff);
					broadcastMessage(sm2, false);
				}
				catch (final Exception e)
				{
					LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player crashed evaluation, an error has been occurred:", e);
				}
			}
			else if (pOneCrash && pTwoCrash)
			{
				try
				{
					playerOneStat.set(POINTS, playerOnePoints - pointDiff);
					playerOneStat.set(COMP_LOST, playerOneLost + 1);
					
					playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
					playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
					
					String text = "Olympiad result: " + playerOneName + " vs " + playerTwoName + " ... " + " BOTH LOST " + pointDiff + " points for game crash";
					Log.add(text, "olympiad_game");
					
					Olympiad.logResult(playerOneName, playerTwoName, 0D, 0D, 0, 0, "both crash", pointDiff, classed);
				}
				catch (final Exception e)
				{
					LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player crashed evaluation, an error has been occurred:", e);
					
				}
			}
			playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
			playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
			
			Olympiad.updateNobleStats(playerOne.getObjectId(), playerOneStat);
			Olympiad.updateNobleStats(playerTwo.getObjectId(), playerTwoStat);
			
			return;
		}
		
		double playerOneHp = 0;
		if (!playerOne.isDead())
		{
			playerOneHp = playerOne.getCurrentHp() + playerOne.getCurrentCp();
		}
		
		double playerTwoHp = 0;
		if (!playerTwo.isDead())
		{
			playerTwoHp = playerTwo.getCurrentHp() + playerTwo.getCurrentCp();
		}
		
		sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_GAME);
		sm2 = new SystemMessage(SystemMessageId.S1_HAS_GAINED_S2_OLYMPIAD_POINTS);
		sm3 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
		
		String result = "";
		
		String winner = "draw";
		
		if (playerOne == null && playerTwo == null)
		{
			playerOneStat.set(COMP_DRAWN, playerOneDrawn + 1);
			playerTwoStat.set(COMP_DRAWN, playerTwoDrawn + 1);
			result = " tie";
			sm = new SystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
			broadcastMessage(sm, true);
		}
		else if (playerTwo == null || !playerTwo.isOnline() || (playerTwoHp == 0 && playerOneHp != 0) || (damageP1 > damageP2 && playerTwoHp != 0 && playerOneHp != 0))
		{
			playerOneStat.set(POINTS, playerOnePoints + pointDiff);
			playerTwoStat.set(POINTS, playerTwoPoints - pointDiff);
			playerOneStat.set(COMP_WON, playerOneWon + 1);
			playerTwoStat.set(COMP_LOST, playerTwoLost + 1);
			
			sm.addString(playerOneName);
			broadcastMessage(sm, true);
			sm2.addString(playerOneName);
			sm2.addNumber(pointDiff);
			broadcastMessage(sm2, false);
			sm3.addString(playerTwoName);
			sm3.addNumber(pointDiff);
			broadcastMessage(sm3, false);
			winner = playerOneName + " won";
			
			try
			{
				result = playerOneName + " win " + pointDiff + " points";
				final L2ItemInstance item = playerOne.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, gpreward, playerOne, null);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				playerOne.sendPacket(iu);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(item.getItemId());
				sm.addNumber(gpreward);
				playerOne.sendPacket(sm);
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player validateWinner, an error has been occurred:", e);
				
			}
		}
		else if (playerOne == null || !playerOne.isOnline() || (playerOneHp == 0 && playerTwoHp != 0) || (damageP2 > damageP1 && playerOneHp != 0 && playerTwoHp != 0))
		{
			playerTwoStat.set(POINTS, playerTwoPoints + pointDiff);
			playerOneStat.set(POINTS, playerOnePoints - pointDiff);
			playerTwoStat.set(COMP_WON, playerTwoWon + 1);
			playerOneStat.set(COMP_LOST, playerOneLost + 1);
			
			sm.addString(playerTwoName);
			broadcastMessage(sm, true);
			sm2.addString(playerTwoName);
			sm2.addNumber(pointDiff);
			broadcastMessage(sm2, false);
			sm3.addString(playerOneName);
			sm3.addNumber(pointDiff);
			broadcastMessage(sm3, false);
			winner = playerTwoName + " won";
			
			try
			{
				result = playerTwoName + " win " + pointDiff + " points";
				final L2ItemInstance item = playerTwo.getInventory().addItem("Olympiad", Config.ALT_OLY_BATTLE_REWARD_ITEM, gpreward, playerTwo, null);
				final InventoryUpdate iu = new InventoryUpdate();
				iu.addModifiedItem(item);
				playerTwo.sendPacket(iu);
				
				final SystemMessage sm = new SystemMessage(SystemMessageId.EARNED_S2_S1_S);
				sm.addItemName(item.getItemId());
				sm.addNumber(gpreward);
				playerTwo.sendPacket(sm);
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player validateWinner, an error has been occurred:", e);
			}
		}
		else
		{
			result = " tie";
			sm = new SystemMessage(SystemMessageId.THE_GAME_ENDED_IN_A_TIE);
			broadcastMessage(sm, true);
			final int pointOneDiff = playerOnePoints / 5;
			final int pointTwoDiff = playerTwoPoints / 5;
			playerOneStat.set(POINTS, playerOnePoints - pointOneDiff);
			playerTwoStat.set(POINTS, playerTwoPoints - pointTwoDiff);
			playerOneStat.set(COMP_DRAWN, playerOneDrawn + 1);
			playerTwoStat.set(COMP_DRAWN, playerTwoDrawn + 1);
			sm2 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
			sm2.addString(playerOneName);
			sm2.addNumber(pointOneDiff);
			broadcastMessage(sm2, false);
			sm3 = new SystemMessage(SystemMessageId.S1_HAS_LOST_S2_OLYMPIAD_POINTS);
			sm3.addString(playerTwoName);
			sm3.addNumber(pointTwoDiff);
			broadcastMessage(sm3, false);
		}
		
		String text = "Olympiad fight: " + playerOneName + " vs " + playerTwoName + " ... " + result;
		Log.add(text, "olympiad_game");
		
		playerOneStat.set(COMP_DONE, playerOnePlayed + 1);
		playerTwoStat.set(COMP_DONE, playerTwoPlayed + 1);
		
		Olympiad.updateNobleStats(playerOne.getObjectId(), playerOneStat);
		Olympiad.updateNobleStats(playerTwo.getObjectId(), playerTwoStat);
		
		Olympiad.logResult(playerOneName, playerTwoName, playerOneHp, playerTwoHp, damageP1, damageP2, winner, pointDiff, classed);
		
		for (int i = 15; i > 5; i -= 5)
		{
			sm = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
			sm.addNumber(i);
			broadcastMessage(sm, false);
			try
			{
				Thread.sleep(5000);
			}
			catch (final InterruptedException e)
			{
			}
		}
		for (int i = 5; i > 0; i--)
		{
			sm = new SystemMessage(SystemMessageId.YOU_WILL_BE_MOVED_TO_TOWN_IN_S1_SECONDS);
			sm.addNumber(i);
			broadcastMessage(sm, false);
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
			}
		}
	}
	
	protected boolean makeCompetitionStart()
	{
		if (aborted)
		{
			return false;
		}
		
		sm = new SystemMessage(SystemMessageId.STARTS_THE_GAME);
		broadcastMessage(sm, true);
		
		for (final L2PcInstance player : players)
		{
			try
			{
				player.setIsInOlympiadFight(true);
			}
			catch (final Exception e)
			{
				LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + player.getName() + " makeCompetitionStart, an error has been occurred:", e);
				aborted = true;
			}
		}
		
		return !aborted;
	}
	
	protected void addDamage(final L2PcInstance player, final int damage)
	{
		if (playerOne == null || playerTwo == null)
		{
			return;
		}
		if (player == playerOne)
		{
			damageP1 += damage;
		}
		else if (player == playerTwo)
		{
			damageP2 += damage;
		}
	}
	
	protected String getTitle()
	{
		String msg = "";
		msg += playerOneName + " / " + playerTwoName;
		return msg;
	}
	
	protected L2PcInstance[] getPlayers()
	{
		final L2PcInstance[] players = new L2PcInstance[2];
		
		if (playerOne == null || playerTwo == null)
		{
			return null;
		}
		
		players[0] = playerOne;
		players[1] = playerTwo;
		
		return players;
	}
	
	private void broadcastMessage(final SystemMessage sm, final boolean toAll)
	{
		try
		{
			playerOne.sendPacket(sm);
			playerTwo.sendPacket(sm);
		}
		catch (final Exception e)
		{
			LOGGER.warn("Olympiad System: Game - " + stadiumID + " on players broadcastMessage, an error has been occurred:", e);
			
		}
		
		if (toAll && OlympiadManager.STADIUMS[stadiumID].getSpectators() != null)
		{
			for (final L2PcInstance spec : OlympiadManager.STADIUMS[stadiumID].getSpectators())
			{
				if (spec != null)
				{
					try
					{
						spec.sendPacket(sm);
					}
					catch (final Exception e)
					{
						LOGGER.warn("Olympiad System: Game - " + stadiumID + " on player " + spec.getName() + " broadcastMessage, an error has been occurred:", e);
						
					}
					
				}
			}
		}
	}
	
	protected void announceGame()
	{
		for (final L2Spawn manager : Olympiad.olymanagers)
		{
			if (manager != null && manager.getLastSpawn() != null)
			{
				final int objId = manager.getLastSpawn().getObjectId();
				final String npcName = manager.getLastSpawn().getName();
				
				final CreatureSay cs = new CreatureSay(objId, 1, npcName, "Olympiad is going to begin in Arena " + (stadiumID + 1) + " in a moment.");
				manager.getLastSpawn().broadcastPacket(cs);
			}
		}
	}
	
	public void sendPlayersStatus(final L2PcInstance spec)
	{
		spec.sendPacket(new ExOlympiadUserInfo(playerOne, 1));
		spec.sendPacket(new ExOlympiadUserInfo(playerTwo, 2));
		spec.sendPacket(new ExOlympiadSpelledInfo(playerOne));
		spec.sendPacket(new ExOlympiadSpelledInfo(playerTwo));
	}
}

/**
 * @author ascharot
 */
class OlympiadGameTask implements Runnable
{
	protected static final Logger LOGGER = Logger.getLogger(OlympiadGameTask.class);
	public OlympiadGame game = null;
	protected static final long BATTLE_PERIOD = Config.ALT_OLY_BATTLE; // 3 mins
	
	private boolean terminated = false;
	private boolean started = false;
	
	public boolean isTerminated()
	{
		return terminated || game.aborted;
	}
	
	public boolean isStarted()
	{
		return started;
	}
	
	public OlympiadGameTask(final OlympiadGame game)
	{
		this.game = game;
	}
	
	protected boolean checkObserverStatusBug(final L2PcInstance player)
	{
		if (player != null && player.inObserverMode())
		{
			LOGGER.info("[OLYMPIAD DEBUG] Player " + player.getName() + "is in Observer mode!");
			return true;
		}
		
		return false;
	}
	
	protected boolean checkBattleStatus()
	{
		final boolean pOneCrash = (game.playerOne == null || game.playerOneDisconnected);
		final boolean pTwoCrash = (game.playerTwo == null || game.playerTwoDisconnected);
		if (pOneCrash || pTwoCrash || game.aborted)
		{
			return false;
		}
		
		return true;
	}
	
	protected boolean checkDefaulted()
	{
		for (int i = 0; i < 2; i++)
		{
			boolean defaulted = false;
			final L2PcInstance player = game.players.get(i);
			if (player != null)
			{
				player.setOlympiadGameId(game.stadiumID);
			}
			final L2PcInstance otherPlayer = game.players.get(i ^ 1);
			SystemMessage sm = null;
			
			if (player == null || !player.isOnline())
			{
				defaulted = true;
			}
			else if (player.isDead())
			{
				sm = new SystemMessage(SystemMessageId.CANNOT_PARTICIPATE_OLYMPIAD_WHILE_DEAD);
				defaulted = true;
			}
			else if (player.isSubClassActive())
			{
				sm = new SystemMessage(SystemMessageId.SINCE_YOU_HAVE_CHANGED_YOUR_CLASS_INTO_A_SUB_JOB_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
				defaulted = true;
			}
			else if (player.isCursedWeaponEquiped())
			{
				sm = new SystemMessage(SystemMessageId.CANNOT_JOIN_OLYMPIAD_POSSESSING_S1);
				sm.addItemName(player.getCursedWeaponEquipedId());
				defaulted = true;
			}
			else if (player.getInventoryLimit() * 0.8 <= player.getInventory().getSize())
			{
				sm = new SystemMessage(SystemMessageId.SINCE_80_PERCENT_OR_MORE_OF_YOUR_INVENTORY_SLOTS_ARE_FULL_YOU_CANNOT_PARTICIPATE_IN_THE_OLYMPIAD);
				defaulted = true;
			}
			
			if (defaulted)
			{
				if (player != null)
				{
					player.sendPacket(sm);
				}
				if (otherPlayer != null)
				{
					otherPlayer.sendPacket(new SystemMessage(SystemMessageId.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_DOES_NOT_MEET_THE_REQUIREMENTS_FOR_JOINING_THE_GAME));
				}
				if (i == 0)
				{
					game.playerOneDefaulted = true;
				}
				else
				{
					game.playerTwoDefaulted = true;
				}
			}
		}
		return game.playerOneDefaulted || game.playerTwoDefaulted;
	}
	
	@Override
	public void run()
	{
		started = true;
		if (game != null)
		{
			if (game.playerOne == null || game.playerTwo == null)
			{
				return;
			}
			
			if (teleportCountdown())
			{
				runGame();
			}
			
			terminated = true;
			game.validateWinner();
			game.playersStatusBack();
			
			if (game.gamestarted)
			{
				game.gamestarted = false;
				try
				{
					game.portPlayersBack();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
			
			game.clearPlayers();
			game = null;
		}
	}
	
	private boolean runGame()
	{
		game.gamestarted = true;
		// Checking for opponents and teleporting to arena
		if (checkDefaulted())
		{
			return false;
		}
		game.portPlayersToArena();
		game.removals();
		if (Config.ALT_OLY_ANNOUNCE_GAMES)
		{
			game.announceGame();
		}
		try
		{
			Thread.sleep(5000);
		}
		catch (final InterruptedException e)
		{
		}
		
		synchronized (this)
		{
			if (!OlympiadGame.battleStarted)
			{
				OlympiadGame.battleStarted = true;
			}
		}
		
		for (int i = 45; i >= 15; i -= 15)
		{
			game.sendMessageToPlayers(true, i);
			try
			{
				Thread.sleep(15000);
			}
			catch (final InterruptedException e)
			{
			}
			if (i == 15)
			{
				game.additions();
				game.damageP1 = 0;
				game.damageP2 = 0;
				game.sendMessageToPlayers(true, 10);
				try
				{
					Thread.sleep(10000);
				}
				catch (final InterruptedException e)
				{
				}
			}
		}
		for (int i = 5; i > 0; i--)
		{
			game.sendMessageToPlayers(true, i);
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
			}
		}
		
		if (checkObserverStatusBug(game.playerOne))
		{
			game.playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			game.playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			game.aborted = true;
			return false;
		}
		
		if (checkObserverStatusBug(game.playerTwo))
		{
			game.playerOne.sendMessage("One player on this match is on Observer mode! Match aborted!");
			game.playerTwo.sendMessage("One player on this match is on Observer mode! Match aborted!");
			game.aborted = true;
			return false;
		}
		
		if (!checkBattleStatus())
		{
			return false;
		}
		game.playerOne.sendPacket(new ExOlympiadUserInfo(game.playerTwo, 1));
		game.playerTwo.sendPacket(new ExOlympiadUserInfo(game.playerOne, 1));
		if (OlympiadManager.STADIUMS[game.stadiumID].getSpectators() != null)
		{
			for (final L2PcInstance spec : OlympiadManager.STADIUMS[game.stadiumID].getSpectators())
			{
				game.sendPlayersStatus(spec);
			}
		}
		
		if (!game.makeCompetitionStart())
		{
			return false;
		}
		
		// Wait 3 mins (Battle)
		for (int i = 0; i < BATTLE_PERIOD; i += 2000)
		{
			try
			{
				Thread.sleep(2000);
				
			}
			catch (final InterruptedException e)
			{
			}
			
			// If game haveWinner then stop waiting battle_period
			// and validate winner
			if (game.haveWinner())
			{
				break;
			}
		}
		
		return checkBattleStatus();
	}
	
	private boolean teleportCountdown()
	{
		// Waiting for teleport to arena
		int k = 1;
		if (Config.ALT_OLY_TELEPORT_COUNTDOWN % 5 == 0)
		{
			k = 5;
		}
		else if (Config.ALT_OLY_TELEPORT_COUNTDOWN % 3 == 0)
		{
			k = 3;
		}
		else if (Config.ALT_OLY_TELEPORT_COUNTDOWN % 2 == 0)
		{
			k = 2;
		}
		
		for (int i = Config.ALT_OLY_TELEPORT_COUNTDOWN; i > k; i -= k)
		{
			switch (i)
			{
				case 120:
				case 60:
				case 30:
				case 15:
					game.sendMessageToPlayers(false, i);
					break;
			}
			try
			{
				Thread.sleep(k * 1000);
			}
			catch (final InterruptedException e)
			{
				return false;
			}
		}
		for (int i = k; i > 0; i--)
		{
			game.sendMessageToPlayers(false, i);
			try
			{
				Thread.sleep(1000);
			}
			catch (final InterruptedException e)
			{
				return false;
			}
		}
		return true;
	}
}
