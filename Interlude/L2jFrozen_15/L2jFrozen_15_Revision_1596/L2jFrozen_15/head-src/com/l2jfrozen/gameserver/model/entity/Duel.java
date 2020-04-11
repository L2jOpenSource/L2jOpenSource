package com.l2jfrozen.gameserver.model.entity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.ai.CtrlIntention;
import com.l2jfrozen.gameserver.managers.DuelManager;
import com.l2jfrozen.gameserver.managers.OlympiadStadiaManager;
import com.l2jfrozen.gameserver.model.L2Character;
import com.l2jfrozen.gameserver.model.L2Effect;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.model.entity.olympiad.Olympiad;
import com.l2jfrozen.gameserver.network.SystemMessageId;
import com.l2jfrozen.gameserver.network.serverpackets.ActionFailed;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelEnd;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelReady;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelStart;
import com.l2jfrozen.gameserver.network.serverpackets.ExDuelUpdateUserInfo;
import com.l2jfrozen.gameserver.network.serverpackets.L2GameServerPacket;
import com.l2jfrozen.gameserver.network.serverpackets.PlaySound;
import com.l2jfrozen.gameserver.network.serverpackets.SocialAction;
import com.l2jfrozen.gameserver.network.serverpackets.SystemMessage;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

public class Duel
{
	protected static final Logger LOGGER = Logger.getLogger(Duel.class);
	
	public static final int DUELSTATE_NODUEL = 0;
	public static final int DUELSTATE_DUELLING = 1;
	public static final int DUELSTATE_DEAD = 2;
	public static final int DUELSTATE_WINNER = 3;
	public static final int DUELSTATE_INTERRUPTED = 4;
	
	private final int duelId;
	private L2PcInstance playerA;
	private L2PcInstance playerB;
	protected boolean partyDuel;
	private final Calendar duelEndTime;
	private int surrenderRequest = 0;
	private int countdown = 4;
	private boolean finished = false;
	private Map<Integer, PlayerCondition> playerConditions;
	
	public static enum DuelResultEnum
	{
		Continue,
		Team1Win,
		Team2Win,
		Team1Surrender,
		Team2Surrender,
		Canceled,
		Timeout
	}
	
	/**
	 * Instantiates a new duel.
	 * @param playerA   the player a
	 * @param playerB   the player b
	 * @param partyDuel the party duel
	 * @param duelId    the duel id
	 */
	public Duel(final L2PcInstance playerA, final L2PcInstance playerB, final int partyDuel, final int duelId)
	{
		this.duelId = duelId;
		this.playerA = playerA;
		this.playerB = playerB;
		this.partyDuel = partyDuel == 1 ? true : false;
		
		duelEndTime = Calendar.getInstance();
		
		if (this.partyDuel)
		{
			duelEndTime.add(Calendar.SECOND, 300);
		}
		else
		{
			duelEndTime.add(Calendar.SECOND, 120);
		}
		
		playerConditions = new HashMap<>();
		
		setFinished(false);
		
		if (this.partyDuel)
		{
			// increase countdown so that start task can teleport players
			countdown++;
			// inform players that they will be portet shortly
			SystemMessage sm = new SystemMessage(SystemMessageId.IN_A_MOMENT_YOU_WILL_BE_TRANSPORTED_TO_THE_SITE_WHERE_THE_DUEL_WILL_TAKE_PLACE);
			broadcastToTeam1(sm);
			broadcastToTeam2(sm);
			sm = null;
		}
		// Schedule duel start
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleStartDuelTask(this), 3000);
	}
	
	public class PlayerCondition
	{
		private L2PcInstance pcPlayer;
		private double hp;
		private double mp;
		private double cp;
		private boolean paDuel;
		private int x, y, z;
		private List<L2Effect> debuffs;
		
		/**
		 * Instantiates a new player condition.
		 * @param player    the player
		 * @param partyDuel the party duel
		 */
		public PlayerCondition(final L2PcInstance player, final boolean partyDuel)
		{
			if (player == null)
			{
				return;
			}
			
			pcPlayer = player;
			hp = pcPlayer.getCurrentHp();
			mp = pcPlayer.getCurrentMp();
			cp = pcPlayer.getCurrentCp();
			paDuel = partyDuel;
			
			if (paDuel)
			{
				x = pcPlayer.getX();
				y = pcPlayer.getY();
				z = pcPlayer.getZ();
			}
		}
		
		public synchronized void restoreCondition()
		{
			if (pcPlayer == null)
			{
				return;
			}
			
			pcPlayer.setCurrentHp(hp);
			pcPlayer.setCurrentMp(mp);
			pcPlayer.setCurrentCp(cp);
			
			if (paDuel)
			{
				teleportBack();
			}
			
			if (debuffs != null) // Debuff removal
			{
				for (final L2Effect temp : debuffs)
				{
					if (temp != null)
					{
						temp.exit(false);
					}
				}
			}
		}
		
		public void registerDebuff(final L2Effect debuff)
		{
			if (debuffs == null)
			{
				debuffs = new ArrayList<>();
			}
			
			debuffs.add(debuff);
		}
		
		public void removeDebuff(final L2Effect debuff)
		{
			if (debuffs == null)
			{
				return;
			}
			
			debuffs.remove(debuff);
		}
		
		public void teleportBack()
		{
			if (paDuel)
			{
				pcPlayer.teleToLocation(x, y, z);
			}
		}
		
		public L2PcInstance getPlayer()
		{
			return pcPlayer;
		}
	}
	
	public class ScheduleDuelTask implements Runnable
	{
		private final Duel duel;
		
		/**
		 * Instantiates a new schedule duel task.
		 * @param duel the duel
		 */
		public ScheduleDuelTask(final Duel duel)
		{
			this.duel = duel;
		}
		
		@Override
		public void run()
		{
			try
			{
				DuelResultEnum status = duel.checkEndDuelCondition();
				
				if (status == DuelResultEnum.Canceled)
				{
					// do not schedule duel end if it was interrupted
					setFinished(true);
					duel.endDuel(status);
				}
				else if (status != DuelResultEnum.Continue)
				{
					setFinished(true);
					playKneelAnimation();
					ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleEndDuelTask(duel, status), 5000);
				}
				else
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				
				status = null;
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	public class ScheduleStartDuelTask implements Runnable
	{
		private final Duel duel;
		
		/**
		 * Instantiates a new schedule start duel task.
		 * @param duel the duel
		 */
		public ScheduleStartDuelTask(final Duel duel)
		{
			this.duel = duel;
		}
		
		@Override
		public void run()
		{
			try
			{
				// start/continue countdown
				final int count = duel.countdown();
				
				if (!partyDuel || count == 4)
				{
					savePlayerConditions();
				}
				
				if (count == 4)
				{
					// players need to be teleportet first
					// TODO: stadia manager needs a function to return an unused stadium for duels
					
					// currently if oly in competition period
					// and defined location is into a stadium
					// just use Gludin Arena as location
					if (Olympiad.getInstance().inCompPeriod() && OlympiadStadiaManager.getInstance().getStadiumByLoc(Config.DUEL_SPAWN_X, Config.DUEL_SPAWN_Y, Config.DUEL_SPAWN_Z) != null)
					{
						duel.teleportPlayers(-87912, 142221, -3645);
					}
					else
					{
						duel.teleportPlayers(Config.DUEL_SPAWN_X, Config.DUEL_SPAWN_Y, Config.DUEL_SPAWN_Z);
					}
					
					// give players 20 seconds to complete teleport and get ready (its ought to be 30 on offical..)
					ThreadPoolManager.getInstance().scheduleGeneral(this, 20000);
				}
				else if (count > 0) // duel not started yet - continue countdown
				{
					ThreadPoolManager.getInstance().scheduleGeneral(this, 1000);
				}
				else
				{
					duel.startDuel();
				}
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	public static class ScheduleEndDuelTask implements Runnable
	{
		private final Duel duel;
		private final DuelResultEnum result;
		
		/**
		 * Instantiates a new schedule end duel task.
		 * @param duel   the duel
		 * @param result the result
		 */
		public ScheduleEndDuelTask(final Duel duel, final DuelResultEnum result)
		{
			this.duel = duel;
			this.result = result;
		}
		
		@Override
		public void run()
		{
			try
			{
				duel.endDuel(result);
			}
			catch (final Throwable t)
			{
				t.printStackTrace();
			}
		}
	}
	
	/**
	 * Stops all players from attacking. Used for duel timeout / interrupt.
	 */
	private void stopFighting()
	{
		ActionFailed af = ActionFailed.STATIC_PACKET;
		if (partyDuel)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
			
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.abortCast();
				temp.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
				temp.setTarget(null);
				temp.sendPacket(af);
			}
		}
		else
		{
			playerA.abortCast();
			playerB.abortCast();
			playerA.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			playerA.setTarget(null);
			playerB.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
			playerB.setTarget(null);
			playerA.sendPacket(af);
			playerB.sendPacket(af);
		}
	}
	
	/**
	 * Check if a player engaged in pvp combat (only for 1on1 duels).
	 * @param  sendMessage the send message
	 * @return             returns true if a duelist is engaged in Pvp combat
	 */
	public boolean isDuelistInPvp(final boolean sendMessage)
	{
		if (partyDuel)
		{
			// Party duels take place in arenas - should be no other players there
			return false;
		}
		else if (playerA.getPvpFlag() != 0 || playerB.getPvpFlag() != 0)
		{
			if (sendMessage)
			{
				final String engagedInPvP = "The duel was canceled because a duelist engaged in PvP combat.";
				playerA.sendMessage(engagedInPvP);
				playerB.sendMessage(engagedInPvP);
			}
			
			return true;
		}
		
		return false;
	}
	
	public void startDuel()
	{
		// savePlayerConditions();
		
		if (playerA == null || playerB == null || playerA.isInDuel() || playerB.isInDuel() || playerA.isInOlympiadMode() || playerB.isInOfflineMode())
		{
			// clean up
			playerConditions.clear();
			playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}
		
		if (partyDuel)
		{
			// set isInDuel() state
			// cancel all active trades, just in case? xD
			for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(duelId);
				temp.setTeam(1);
				// temp.broadcastStatusUpdate();
				temp.broadcastUserInfo();
				broadcastToTeam2(new ExDuelUpdateUserInfo(temp));
			}
			for (final L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.cancelActiveTrade();
				temp.setIsInDuel(duelId);
				temp.setTeam(2);
				// temp.broadcastStatusUpdate();
				temp.broadcastUserInfo();
				broadcastToTeam1(new ExDuelUpdateUserInfo(temp));
			}
			
			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(1);
			ExDuelStart start = new ExDuelStart(1);
			
			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
			
			ready = null;
			start = null;
		}
		else
		{
			// set isInDuel() state
			playerA.setIsInDuel(duelId);
			playerA.setTeam(1);
			playerB.setIsInDuel(duelId);
			playerB.setTeam(2);
			
			// Send duel Start packets
			ExDuelReady ready = new ExDuelReady(0);
			ExDuelStart start = new ExDuelStart(0);
			
			broadcastToTeam1(ready);
			broadcastToTeam2(ready);
			broadcastToTeam1(start);
			broadcastToTeam2(start);
			
			broadcastToTeam1(new ExDuelUpdateUserInfo(playerB));
			broadcastToTeam2(new ExDuelUpdateUserInfo(playerA));
			// playerA.broadcastStatusUpdate();
			// playerB.broadcastStatusUpdate();
			playerA.broadcastUserInfo();
			playerB.broadcastUserInfo();
		}
		
		// play sound
		PlaySound ps = new PlaySound(1, "B04_S01", 0, 0, 0, 0, 0);
		broadcastToTeam1(ps);
		broadcastToTeam2(ps);
		
		ps = null;
		
		// start duelling task
		ThreadPoolManager.getInstance().scheduleGeneral(new ScheduleDuelTask(this), 1000);
	}
	
	/**
	 * Save the current player condition: hp, mp, cp, location.
	 */
	public void savePlayerConditions()
	{
		
		if (partyDuel)
		{
			for (L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				playerConditions.put(temp.getObjectId(), new PlayerCondition(temp, partyDuel));
			}
			
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				playerConditions.put(temp.getObjectId(), new PlayerCondition(temp, partyDuel));
			}
		}
		else
		{
			playerConditions.put(playerA.getObjectId(), new PlayerCondition(playerA, partyDuel));
			playerConditions.put(playerB.getObjectId(), new PlayerCondition(playerB, partyDuel));
		}
		
	}
	
	/**
	 * Restore player conditions.
	 * @param abnormalDuelEnd the abnormal duel end
	 */
	private synchronized void restorePlayerConditions(final boolean abnormalDuelEnd)
	{
		// update isInDuel() state for all players
		if (partyDuel)
		{
			for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
			
			for (final L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.setIsInDuel(0);
				temp.setTeam(0);
				temp.broadcastUserInfo();
			}
		}
		else
		{
			playerA.setIsInDuel(0);
			playerA.setTeam(0);
			playerA.broadcastUserInfo();
			playerB.setIsInDuel(0);
			playerB.setTeam(0);
			playerB.broadcastUserInfo();
		}
		
		// if it is an abnormal DuelEnd do not restore hp, mp, cp
		if (abnormalDuelEnd)
		{
			return;
		}
		
		// restore player conditions
		for (int playerObjId : playerConditions.keySet())
		{
			PlayerCondition e = playerConditions.get(playerObjId);
			e.restoreCondition();
		}
	}
	
	/**
	 * @return the duel id.
	 */
	public int getId()
	{
		return duelId;
	}
	
	public int getRemainingTime()
	{
		return (int) (duelEndTime.getTimeInMillis() - Calendar.getInstance().getTimeInMillis());
	}
	
	/**
	 * Get the player that requestet the duel.
	 * @return duel requester
	 */
	public L2PcInstance getPlayerA()
	{
		return playerA;
	}
	
	/**
	 * Get the player that was challenged.
	 * @return challenged player
	 */
	public L2PcInstance getPlayerB()
	{
		return playerB;
	}
	
	/**
	 * Returns whether this is a party duel or not.
	 * @return is party duel
	 */
	public boolean isPartyDuel()
	{
		return partyDuel;
	}
	
	/**
	 * Sets the finished.
	 * @param mode the new finished
	 */
	public void setFinished(final boolean mode)
	{
		finished = mode;
	}
	
	/**
	 * Gets the finished.
	 * @return the finished
	 */
	public boolean getFinished()
	{
		return finished;
	}
	
	/**
	 * teleport all players to the given coordinates.
	 * @param x the x
	 * @param y the y
	 * @param z the z
	 */
	public void teleportPlayers(final int x, final int y, final int z)
	{
		// TODO: adjust the values if needed... or implement something better (especially using more then 1 arena)
		if (!partyDuel)
		{
			return;
		}
		
		int offset = 0;
		
		for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y - 150, z);
			offset += 40;
		}
		
		offset = 0;
		
		for (final L2PcInstance temp : playerB.getParty().getPartyMembers())
		{
			temp.teleToLocation(x + offset - 180, y + 150, z);
			offset += 40;
		}
	}
	
	/**
	 * Broadcast a packet to the challanger team.
	 * @param packet the packet
	 */
	public void broadcastToTeam1(final L2GameServerPacket packet)
	{
		if (playerA == null)
		{
			return;
		}
		
		if (partyDuel && playerA.getParty() != null)
		{
			for (final L2PcInstance temp : playerA.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			playerA.sendPacket(packet);
		}
	}
	
	/**
	 * Broadcast a packet to the challenged team.
	 * @param packet the packet
	 */
	public void broadcastToTeam2(L2GameServerPacket packet)
	{
		if (playerB == null)
		{
			return;
		}
		
		if (partyDuel && playerB.getParty() != null)
		{
			for (L2PcInstance temp : playerB.getParty().getPartyMembers())
			{
				temp.sendPacket(packet);
			}
		}
		else
		{
			playerB.sendPacket(packet);
		}
	}
	
	/**
	 * Get the duel winner.
	 * @return winner
	 */
	public L2PcInstance getWinner()
	{
		if (!getFinished() || playerA == null || playerB == null)
		{
			return null;
		}
		
		if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			return playerA;
		}
		
		if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			return playerB;
		}
		
		return null;
	}
	
	/**
	 * Get the duel looser.
	 * @return looser
	 */
	public L2PcInstance getLooser()
	{
		if (!getFinished() || playerA == null || playerB == null)
		{
			return null;
		}
		
		if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			return playerB;
		}
		else if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			return playerA;
		}
		
		return null;
	}
	
	/**
	 * Playback the bow animation for all loosers.
	 */
	public void playKneelAnimation()
	{
		L2PcInstance looser = getLooser();
		
		if (looser == null)
		{
			return;
		}
		
		if (partyDuel && looser.getParty() != null)
		{
			for (L2PcInstance temp : looser.getParty().getPartyMembers())
			{
				temp.broadcastPacket(new SocialAction(temp.getObjectId(), 7));
			}
		}
		else
		{
			looser.broadcastPacket(new SocialAction(looser.getObjectId(), 7));
		}
	}
	
	/**
	 * Do the countdown and send message to players if necessary.
	 * @return current count
	 */
	public int countdown()
	{
		countdown--;
		
		if (countdown > 3)
		{
			return countdown;
		}
		
		// Broadcast countdown to duelists
		SystemMessage sm = null;
		if (countdown > 0)
		{
			sm = new SystemMessage(SystemMessageId.THE_DUEL_WILL_BEGIN_IN_S1_SECONDS);
			sm.addNumber(countdown);
		}
		else
		{
			sm = new SystemMessage(SystemMessageId.LET_THE_DUEL_BEGIN);
		}
		
		broadcastToTeam1(sm);
		broadcastToTeam2(sm);
		
		return countdown;
	}
	
	/**
	 * The duel has reached a state in which it can no longer continue.
	 * @param result the result
	 */
	public void endDuel(final DuelResultEnum result)
	{
		if (playerA == null || playerB == null)
		{
			// clean up
			playerConditions.clear();
			playerConditions = null;
			DuelManager.getInstance().removeDuel(this);
			return;
		}
		
		// inform players of the result
		SystemMessage sm = null;
		switch (result)
		{
			case Team2Surrender:
			case Team1Win:
				restorePlayerConditions(false);
				
				if (partyDuel)
				{
					sm = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL);
				}
				
				sm.addString(playerA.getName());
				
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Team1Surrender:
			case Team2Win:
				restorePlayerConditions(false);
				
				if (partyDuel)
				{
					sm = new SystemMessage(SystemMessageId.S1S_PARTY_HAS_WON_THE_DUEL);
				}
				else
				{
					sm = new SystemMessage(SystemMessageId.S1_HAS_WON_THE_DUEL);
				}
				
				sm.addString(playerB.getName());
				
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Canceled:
				stopFighting();
				// dont restore hp, mp, cp
				restorePlayerConditions(true);
				sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
			case Timeout:
				// restore hp, mp, cp
				restorePlayerConditions(true);
				sm = new SystemMessage(SystemMessageId.THE_DUEL_HAS_ENDED_IN_A_TIE);
				broadcastToTeam1(sm);
				broadcastToTeam2(sm);
				break;
		}
		
		// Send end duel packet
		ExDuelEnd duelEnd = null;
		if (partyDuel)
		{
			duelEnd = new ExDuelEnd(1);
		}
		else
		{
			duelEnd = new ExDuelEnd(0);
		}
		
		broadcastToTeam1(duelEnd);
		broadcastToTeam2(duelEnd);
		
		// clean up
		playerConditions.clear();
		playerConditions = null;
		DuelManager.getInstance().removeDuel(this);
	}
	
	/**
	 * Did a situation occur in which the duel has to be ended?.
	 * @return DuelResultEnum duel status
	 */
	public DuelResultEnum checkEndDuelCondition()
	{
		// one of the players might leave during duel
		if (playerA == null || playerB == null)
		{
			return DuelResultEnum.Canceled;
		}
		
		// got a duel surrender request?
		if (surrenderRequest != 0)
		{
			if (surrenderRequest == 1)
			{
				return DuelResultEnum.Team1Surrender;
			}
			
			return DuelResultEnum.Team2Surrender;
		}
		// duel timed out
		else if (getRemainingTime() <= 0)
		{
			return DuelResultEnum.Timeout;
		}
		else if (playerA.getDuelState() == DUELSTATE_WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResultEnum.Team1Win;
		}
		else if (playerB.getDuelState() == DUELSTATE_WINNER)
		{
			// If there is a Winner already there should be no more fighting going on
			stopFighting();
			return DuelResultEnum.Team2Win;
		}
		
		// More end duel conditions for 1on1 duels
		else if (!partyDuel)
		{
			// Duel was interrupted e.g.: player was attacked by mobs / other players
			if (playerA.getDuelState() == DUELSTATE_INTERRUPTED || playerB.getDuelState() == DUELSTATE_INTERRUPTED)
			{
				return DuelResultEnum.Canceled;
			}
			
			// Are the players too far apart?
			if (!playerA.isInsideRadius(playerB, 1600, false, false))
			{
				return DuelResultEnum.Canceled;
			}
			
			// Did one of the players engage in PvP combat?
			if (isDuelistInPvp(true))
			{
				return DuelResultEnum.Canceled;
			}
			
			// is one of the players in a Siege, Peace or PvP zone?
			if (playerA.isInsideZone(L2Character.ZONE_PEACE) || playerB.isInsideZone(L2Character.ZONE_PEACE) || playerA.isInsideZone(L2Character.ZONE_SIEGE) || playerB.isInsideZone(L2Character.ZONE_SIEGE) || playerA.isInsideZone(L2Character.ZONE_PVP) || playerB.isInsideZone(L2Character.ZONE_PVP))
			{
				return DuelResultEnum.Canceled;
			}
		}
		
		return DuelResultEnum.Continue;
	}
	
	/**
	 * Register a surrender request.
	 * @param player the player
	 */
	public void doSurrender(final L2PcInstance player)
	{
		// already recived a surrender request
		if (surrenderRequest != 0)
		{
			return;
		}
		
		// stop the fight
		stopFighting();
		
		// TODO: Can every party member cancel a party duel? or only the party leaders?
		if (partyDuel)
		{
			if (playerA.getParty().getPartyMembers().contains(player))
			{
				surrenderRequest = 1;
				
				for (L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
			else if (playerB.getParty().getPartyMembers().contains(player))
			{
				surrenderRequest = 2;
				
				for (L2PcInstance temp : playerB.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_DEAD);
				}
				
				for (L2PcInstance temp : playerA.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else
		{
			if (player == playerA)
			{
				surrenderRequest = 1;
				playerA.setDuelState(DUELSTATE_DEAD);
				playerB.setDuelState(DUELSTATE_WINNER);
			}
			else if (player == playerB)
			{
				surrenderRequest = 2;
				playerB.setDuelState(DUELSTATE_DEAD);
				playerA.setDuelState(DUELSTATE_WINNER);
			}
		}
	}
	
	/**
	 * This function is called whenever a player was defeated in a duel.
	 * @param player the player
	 */
	public void onPlayerDefeat(final L2PcInstance player)
	{
		// Set player as defeated
		player.setDuelState(DUELSTATE_DEAD);
		
		if (partyDuel)
		{
			boolean teamdefeated = true;
			
			for (final L2PcInstance temp : player.getParty().getPartyMembers())
			{
				if (temp.getDuelState() == DUELSTATE_DUELLING)
				{
					teamdefeated = false;
					break;
				}
			}
			
			if (teamdefeated)
			{
				L2PcInstance winner = playerA;
				
				if (playerA.getParty().getPartyMembers().contains(player))
				{
					winner = playerB;
				}
				
				for (L2PcInstance temp : winner.getParty().getPartyMembers())
				{
					temp.setDuelState(DUELSTATE_WINNER);
				}
			}
		}
		else
		{
			if (player != playerA && player != playerB)
			{
				LOGGER.warn("Error in onPlayerDefeat(): player is not part of this 1vs1 duel");
			}
			
			if (playerA == player)
			{
				playerB.setDuelState(DUELSTATE_WINNER);
			}
			else
			{
				playerA.setDuelState(DUELSTATE_WINNER);
			}
		}
	}
	
	/**
	 * This function is called whenever a player leaves a party.
	 * @param player the player
	 */
	public void onRemoveFromParty(L2PcInstance player)
	{
		// if it isnt a party duel ignore this
		if (!partyDuel)
		{
			return;
		}
		
		// this player is leaving his party during party duel
		// if hes either playerA or playerB cancel the duel and port the players back
		if (player == playerA || player == playerB)
		{
			
			PlayerCondition e = playerConditions.remove(player.getObjectId());
			
			if (e != null)
			{
				e.teleportBack();
				e.getPlayer().setIsInDuel(0);
			}
			
			if (player == playerA)
			{
				playerA = null;
			}
			else
			{
				playerB = null;
			}
		}
		else
		// teleport the player back & delete his PlayerCondition record
		{
			PlayerCondition e = playerConditions.remove(player.getObjectId());
			
			if (e != null)
			{
				e.teleportBack();
			}
			
			player.setIsInDuel(0);
		}
	}
	
	/**
	 * On buff.
	 * @param player the player
	 * @param debuff the debuff
	 */
	public void onBuff(L2PcInstance player, L2Effect debuff)
	{
		final PlayerCondition e = playerConditions.get(player.getObjectId());
		if (e != null)
		{
			e.registerDebuff(debuff);
		}
	}
	
	/**
	 * On buff stop.
	 * @param player the player
	 * @param debuff the debuff
	 */
	public void onBuffStop(L2PcInstance player, L2Effect debuff)
	{
		PlayerCondition e = playerConditions.get(player.getObjectId());
		
		if (e != null)
		{
			e.removeDebuff(debuff);
		}
	}
}
