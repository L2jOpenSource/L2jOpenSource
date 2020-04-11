package net.sf.l2j.gameserver.model.zone.type;

import java.util.concurrent.Future;

import net.sf.l2j.commons.concurrent.ThreadPool;

import net.sf.l2j.gameserver.enums.ZoneId;
import net.sf.l2j.gameserver.enums.skills.Stats;
import net.sf.l2j.gameserver.model.actor.Creature;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.model.zone.type.subtype.CastleZoneType;
import net.sf.l2j.gameserver.network.SystemMessageId;
import net.sf.l2j.gameserver.network.serverpackets.EtcStatusUpdate;
import net.sf.l2j.gameserver.network.serverpackets.SystemMessage;

/**
 * A zone extending {@link CastleZoneType}, which fires a task on the first character entrance, notably used by castle damage traps.<br>
 * <br>
 * This task decreases HPs using a reuse delay and can affect specific class types. The zone is considered a danger zone.
 */
public class DamageZone extends CastleZoneType
{
	private volatile Future<?> _task;
	
	private int _hpDamage = 200;
	private int _initialDelay = 1000;
	private int _reuseDelay = 5000;
	
	public DamageZone(int id)
	{
		super(id);
	}
	
	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("hpDamage"))
			_hpDamage = Integer.parseInt(value);
		else if (name.equalsIgnoreCase("initialDelay"))
			_initialDelay = Integer.parseInt(value);
		else if (name.equalsIgnoreCase("reuseDelay"))
			_reuseDelay = Integer.parseInt(value);
		else
			super.setParameter(name, value);
	}
	
	@Override
	protected boolean isAffected(Creature character)
	{
		return character instanceof Playable;
	}
	
	@Override
	protected void onEnter(Creature character)
	{
		if (_hpDamage > 0)
		{
			// Castle traps are active only during siege, or if they're activated.
			if (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress()))
				return;
			
			Future<?> task = _task;
			if (task == null)
			{
				synchronized (this)
				{
					task = _task;
					if (task == null)
						_task = task = ThreadPool.scheduleAtFixedRate(() ->
						{
							if (_characters.isEmpty() || _hpDamage <= 0 || (getCastle() != null && (!isEnabled() || !getCastle().getSiege().isInProgress())))
							{
								stopTask();
								return;
							}
							
							// Effect all people inside the zone.
							for (Creature temp : _characters.values())
							{
								if (!temp.isDead())
									temp.reduceCurrentHp(_hpDamage * (1 + (temp.calcStat(Stats.DAMAGE_ZONE_VULN, 0, null, null) / 100)), null, null);
							}
						}, _initialDelay, _reuseDelay);
					
					// Message for castle traps.
					if (getCastle() != null)
						getCastle().getSiege().announceToPlayers(SystemMessage.getSystemMessage(SystemMessageId.A_TRAP_DEVICE_HAS_BEEN_TRIPPED), false);
				}
			}
		}
		
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, true);
			character.sendPacket(new EtcStatusUpdate((Player) character));
		}
	}
	
	@Override
	protected void onExit(Creature character)
	{
		if (character instanceof Player)
		{
			character.setInsideZone(ZoneId.DANGER_AREA, false);
			
			if (!character.isInsideZone(ZoneId.DANGER_AREA))
				character.sendPacket(new EtcStatusUpdate((Player) character));
		}
	}
	
	private void stopTask()
	{
		if (_task != null)
		{
			_task.cancel(false);
			_task = null;
		}
	}
}