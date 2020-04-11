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
package l2r.gameserver.model.actor.status;

import java.util.Set;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javolution.util.FastSet;
import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Character;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.actor.stat.CharStat;
import l2r.gameserver.model.stats.Formulas;
import l2r.util.Rnd;

public class CharStatus
{
	protected static final Logger _log = Logger.getLogger(CharStatus.class.getName());
	
	private final L2Character _activeChar;
	
	private double _currentHp = 0; // Current HP of the L2Character
	private double _currentMp = 0; // Current MP of the L2Character
	
	/** Array containing all clients that need to be notified about hp/mp updates of the L2Character */
	private Set<L2Character> _StatusListener;
	
	private Future<?> _regTask;
	
	protected byte _flagsRegenActive = 0;
	
	protected static final byte REGEN_FLAG_CP = 4;
	private static final byte REGEN_FLAG_HP = 1;
	private static final byte REGEN_FLAG_MP = 2;
	
	public CharStatus(L2Character activeChar)
	{
		_activeChar = activeChar;
	}
	
	/**
	 * Add the object to the list of L2Character that must be informed of HP/MP updates of this L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.<br>
	 * Players who must be informed are players that target this L2Character.<br>
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<br>
	 * <B><U>Example of use</U>:</B>
	 * <ul>
	 * <li>Target a PC or NPC</li>
	 * <ul>
	 * @param object L2Character to add to the listener
	 */
	public final void addStatusListener(L2Character object)
	{
		if (object == getActiveChar())
		{
			return;
		}
		
		getStatusListener().add(object);
	}
	
	/**
	 * Remove the object from the list of L2Character that must be informed of HP/MP updates of this L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.<br>
	 * Players who must be informed are players that target this L2Character.<br>
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.<br>
	 * <B><U>Example of use </U>:</B>
	 * <ul>
	 * <li>Untarget a PC or NPC</li>
	 * </ul>
	 * @param object L2Character to add to the listener
	 */
	public final void removeStatusListener(L2Character object)
	{
		getStatusListener().remove(object);
	}
	
	/**
	 * Return the list of L2Character that must be informed of HP/MP updates of this L2Character.<br>
	 * <B><U>Concept</U>:</B><br>
	 * Each L2Character owns a list called <B>_statusListener</B> that contains all L2PcInstance to inform of HP/MP updates.<br>
	 * Players who must be informed are players that target this L2Character.<br>
	 * When a RegenTask is in progress sever just need to go through this list to send Server->Client packet StatusUpdate.
	 * @return The list of L2Character to inform or null if empty
	 */
	public final Set<L2Character> getStatusListener()
	{
		if (_StatusListener == null)
		{
			_StatusListener = new FastSet<L2Character>().shared();
		}
		return _StatusListener;
	}
	
	// place holder, only PcStatus has CP
	public void reduceCp(int value)
	{
	}
	
	/**
	 * Reduce the current HP of the L2Character and launch the doDie Task if necessary.
	 * @param value
	 * @param attacker
	 */
	public void reduceHp(double value, L2Character attacker)
	{
		reduceHp(value, attacker, true, false, false);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean isHpConsumption)
	{
		reduceHp(value, attacker, true, false, isHpConsumption);
	}
	
	public void reduceHp(double value, L2Character attacker, boolean awake, boolean isDOT, boolean isHPConsumption)
	{
		if (getActiveChar().isDead())
		{
			return;
		}
		
		// invul handling
		if (getActiveChar().isInvul() && !(isDOT || isHPConsumption))
		{
			return;
		}
		
		if (attacker != null)
		{
			final L2PcInstance attackerPlayer = attacker.getActingPlayer();
			if ((attackerPlayer != null) && attackerPlayer.isGM() && !attackerPlayer.getAccessLevel().canGiveDamage())
			{
				return;
			}
		}
		
		if (!isDOT && !isHPConsumption)
		{
			getActiveChar().stopEffectsOnDamage(awake);
			if (getActiveChar().isStunned() && (Rnd.get(10) == 0))
			{
				getActiveChar().stopStunning(true);
			}
		}
		
		if (value > 0)
		{
			setCurrentHp(Math.max(getCurrentHp() - value, 0));
		}
		
		if ((getActiveChar().getCurrentHp() < 0.5) && getActiveChar().isMortal()) // Die
		{
			getActiveChar().abortAttack();
			getActiveChar().abortCast();
			
			if (Config.DEBUG)
			{
				_log.fine("char is dead.");
			}
			
			getActiveChar().doDie(attacker);
		}
	}
	
	public void reduceMp(double value)
	{
		setCurrentMp(Math.max(getCurrentMp() - value, 0));
	}
	
	/**
	 * Start the HP/MP/CP Regeneration task.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Calculate the regen task period</li>
	 * <li>Launch the HP/MP/CP Regeneration task with Medium priority</li>
	 * </ul>
	 */
	public final synchronized void startHpMpRegeneration()
	{
		if ((_regTask == null) && !getActiveChar().isDead())
		{
			if (Config.DEBUG)
			{
				_log.fine("HP/MP regen started");
			}
			
			// Get the Regeneration period
			int period = Formulas.getRegeneratePeriod(getActiveChar());
			
			// Create the HP/MP/CP Regeneration task
			_regTask = ThreadPoolManager.getInstance().scheduleEffectAtFixedRate(new RegenTask(), period, period);
		}
	}
	
	/**
	 * Stop the HP/MP/CP Regeneration task.<br>
	 * <B><U>Actions</U>:</B>
	 * <ul>
	 * <li>Set the RegenActive flag to False</li>
	 * <li>Stop the HP/MP/CP Regeneration task</li>
	 * </ul>
	 */
	public final synchronized void stopHpMpRegeneration()
	{
		if (_regTask != null)
		{
			if (Config.DEBUG)
			{
				_log.fine("HP/MP regen stop");
			}
			
			// Stop the HP/MP/CP Regeneration task
			_regTask.cancel(false);
			_regTask = null;
			
			// Set the RegenActive flag to false
			_flagsRegenActive = 0;
		}
	}
	
	// place holder, only PcStatus has CP
	public double getCurrentCp()
	{
		return 0;
	}
	
	// place holder, only PcStatus has CP
	public void setCurrentCp(double newCp)
	{
	}
	
	public final double getCurrentHp()
	{
		return _currentHp;
	}
	
	public final void setCurrentHp(double newHp)
	{
		setCurrentHp(newHp, true);
	}
	
	/**
	 * Sets the current hp of this character.
	 * @param newHp the new hp
	 * @param broadcastPacket if true StatusUpdate packet will be broadcasted.
	 * @return @{code true} if hp was changed, @{code false} otherwise.
	 */
	public boolean setCurrentHp(double newHp, boolean broadcastPacket)
	{
		// Get the Max HP of the L2Character
		int currentHp = (int) getCurrentHp();
		final double maxHp = getActiveChar().getStat().getMaxHp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return false;
			}
			
			if (newHp >= maxHp)
			{
				// Set the RegenActive flag to false
				_currentHp = maxHp;
				_flagsRegenActive &= ~REGEN_FLAG_HP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentHp = newHp;
				_flagsRegenActive |= REGEN_FLAG_HP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		boolean hpWasChanged = currentHp != _currentHp;
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (hpWasChanged && broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
		
		return hpWasChanged;
	}
	
	public final void setCurrentHpMp(double newHp, double newMp)
	{
		boolean hpOrMpWasChanged = setCurrentHp(newHp, false);
		hpOrMpWasChanged |= setCurrentMp(newMp, false);
		if (hpOrMpWasChanged)
		{
			getActiveChar().broadcastStatusUpdate();
		}
	}
	
	public final double getCurrentMp()
	{
		return _currentMp;
	}
	
	public final void setCurrentMp(double newMp)
	{
		setCurrentMp(newMp, true);
	}
	
	/**
	 * Sets the current mp of this character.
	 * @param newMp the new mp
	 * @param broadcastPacket if true StatusUpdate packet will be broadcasted.
	 * @return @{code true} if mp was changed, @{code false} otherwise.
	 */
	public final boolean setCurrentMp(double newMp, boolean broadcastPacket)
	{
		// Get the Max MP of the L2Character
		int currentMp = (int) getCurrentMp();
		final int maxMp = getActiveChar().getStat().getMaxMp();
		
		synchronized (this)
		{
			if (getActiveChar().isDead())
			{
				return false;
			}
			
			if (newMp >= maxMp)
			{
				// Set the RegenActive flag to false
				_currentMp = maxMp;
				_flagsRegenActive &= ~REGEN_FLAG_MP;
				
				// Stop the HP/MP/CP Regeneration task
				if (_flagsRegenActive == 0)
				{
					stopHpMpRegeneration();
				}
			}
			else
			{
				// Set the RegenActive flag to true
				_currentMp = newMp;
				_flagsRegenActive |= REGEN_FLAG_MP;
				
				// Start the HP/MP/CP Regeneration task with Medium priority
				startHpMpRegeneration();
			}
		}
		
		boolean mpWasChanged = currentMp != _currentMp;
		
		// Send the Server->Client packet StatusUpdate with current HP and MP to all other L2PcInstance to inform
		if (mpWasChanged && broadcastPacket)
		{
			getActiveChar().broadcastStatusUpdate();
		}
		
		return mpWasChanged;
	}
	
	protected void doRegeneration()
	{
		final CharStat charstat = getActiveChar().getStat();
		
		// Modify the current HP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentHp() < charstat.getMaxRecoverableHp())
		{
			setCurrentHp(getCurrentHp() + Formulas.calcHpRegen(getActiveChar()), false);
		}
		
		// Modify the current MP of the L2Character and broadcast Server->Client packet StatusUpdate
		if (getCurrentMp() < charstat.getMaxRecoverableMp())
		{
			setCurrentMp(getCurrentMp() + Formulas.calcMpRegen(getActiveChar()), false);
		}
		
		if (!getActiveChar().isInActiveRegion())
		{
			// no broadcast necessary for characters that are in inactive regions.
			// stop regeneration for characters who are filled up and in an inactive region.
			if ((getCurrentHp() == charstat.getMaxRecoverableHp()) && (getCurrentMp() == charstat.getMaxMp()))
			{
				stopHpMpRegeneration();
			}
		}
		else
		{
			getActiveChar().broadcastStatusUpdate(); // send the StatusUpdate packet
		}
	}
	
	/** Task of HP/MP regeneration */
	class RegenTask implements Runnable
	{
		@Override
		public void run()
		{
			try
			{
				doRegeneration();
			}
			catch (Exception e)
			{
				_log.log(Level.SEVERE, "", e);
			}
		}
	}
	
	public L2Character getActiveChar()
	{
		return _activeChar;
	}
}
