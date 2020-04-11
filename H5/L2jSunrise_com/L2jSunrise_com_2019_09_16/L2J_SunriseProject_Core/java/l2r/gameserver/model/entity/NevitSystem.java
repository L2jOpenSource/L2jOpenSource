package l2r.gameserver.model.entity;

import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import l2r.Config;
import l2r.gameserver.ThreadPoolManager;
import l2r.gameserver.model.actor.L2Attackable;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.model.effects.AbnormalEffect;
import l2r.gameserver.model.events.EventType;
import l2r.gameserver.model.events.annotations.RegisterEvent;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogin;
import l2r.gameserver.model.events.impl.character.player.OnPlayerLogout;
import l2r.gameserver.model.events.listeners.ConsumerEventListener;
import l2r.gameserver.model.interfaces.IUniqueId;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.ExNevitAdventEffect;
import l2r.gameserver.network.serverpackets.ExNevitAdventPointInfoPacket;
import l2r.gameserver.network.serverpackets.ExNevitAdventTimeChange;

/**
 * @author vGodFather
 */
public class NevitSystem implements IUniqueId
{
	// Settings
	private static final boolean HUNTING_BONUS_ENGINE = Config.HUNTING_BONUS_ENGINE;
	private static final int MAX_POINTS = Config.HUNTING_BONUS_MAX_POINTS;
	private static final int BONUS_EFFECT_TIME = Config.HUNTING_BONUS_EFFECT_TIME;
	protected static final int REFRESH_RATE = Config.HUNTING_BONUS_REFRESH_RATE;
	protected static final int REFRESH_POINTS = Config.HUNTING_BONUS_POINTS_ON_REFRESH;
	private static final boolean EXTRA_POINTS = Config.HUNTING_BONUS_EXTRA_POINTS;
	private static final boolean EXTRA_POINTS_ALL_TIME = Config.HUNTING_BONUS_EXTRA_POINTS_ALL_TIME;
	
	// Nevit Hour
	public static final int ADVENT_TIME = Config.HUNTING_BONUS_MAX_TIME;
	private final L2PcInstance _player;
	
	private volatile ScheduledFuture<?> _adventTask;
	private volatile ScheduledFuture<?> _nevitEffectTask;
	
	public NevitSystem(L2PcInstance player)
	{
		_player = player;
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGIN, (OnPlayerLogin event) -> onPlayerLogin(event), this));
		player.addListener(new ConsumerEventListener(player, EventType.ON_PLAYER_LOGOUT, (OnPlayerLogout event) -> OnPlayerLogout(event), this));
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGIN)
	private void onPlayerLogin(OnPlayerLogin event)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			final Calendar cal = Calendar.getInstance();
			cal.set(Calendar.HOUR_OF_DAY, 6);
			cal.set(Calendar.MINUTE, 30);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
			
			// Reset Nevit's Blessing
			if ((_player.getLastAccess() < (cal.getTimeInMillis() / 1000L)) && (System.currentTimeMillis() > cal.getTimeInMillis()))
			{
				_player.getNevitSystem().setAdventTime(0); // Refuel-reset hunting bonus time
			}
			
			// Send Packets
			_player.sendPacket(new ExNevitAdventEffect(0));
			_player.sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
			_player.sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
			
			startNevitEffect(_player.getVariables().getInt("nevit_b", 0));
			
			// Set percent
			int percent = calcPercent(getAdventPoints());
			
			if ((percent >= 45) && (percent < 50))
			{
				_player.sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
			}
			else if ((percent >= 50) && (percent < 75))
			{
				_player.sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
			}
			else if (percent >= 75)
			{
				_player.sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
			}
		}
	}
	
	@RegisterEvent(EventType.ON_PLAYER_LOGOUT)
	private void OnPlayerLogout(OnPlayerLogout event)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			stopNevitEffectTask(true);
			stopAdventTask(false);
		}
	}
	
	public void addPoints(int val)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			// vGodFather: we must calculate points even if advent blessing effect is active
			// setAdventPoints(getEffectTime() > 0 ? 0 : getAdventPoints() + val);
			setAdventPoints(getAdventPoints() + val);
			
			if (getAdventPoints() > MAX_POINTS)
			{
				setAdventPoints(0);
				startNevitEffect(BONUS_EFFECT_TIME);
			}
			
			switch (calcPercent(getAdventPoints()))
			{
				case 45:
				{
					getPlayer().sendPacket(SystemMessageId.YOU_ARE_STARTING_TO_FEEL_THE_EFFECTS_OF_NEVITS_ADVENT_BLESSING);
					break;
				}
				case 50:
				{
					getPlayer().sendPacket(SystemMessageId.YOU_ARE_FURTHER_INFUSED_WITH_THE_BLESSINGS_OF_NEVIT);
					break;
				}
				case 75:
				{
					getPlayer().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_SHINES_STRONGLY_FROM_ABOVE);
					break;
				}
			}
			
			getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
		}
	}
	
	public void startAdventTask()
	{
		if (HUNTING_BONUS_ENGINE)
		{
			if ((_adventTask == null) && (getAdventTime() < ADVENT_TIME))
			{
				_adventTask = ThreadPoolManager.getInstance().scheduleGeneralAtFixedRate(new AdventTask(), REFRESH_RATE * 1000, REFRESH_RATE * 1000);
				getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
			}
		}
	}
	
	protected class AdventTask implements Runnable
	{
		@Override
		public void run()
		{
			setAdventTime(getAdventTime() + REFRESH_RATE);
			if (getAdventTime() >= ADVENT_TIME)
			{
				setAdventTime(ADVENT_TIME);
				stopAdventTask(true);
				return;
			}
			
			addPoints(REFRESH_POINTS);
			if ((getAdventTime() % 60) == 0)
			{
				getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), false));
			}
		}
	}
	
	public void stopAdventTask(boolean sendPacket)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			if (_adventTask != null)
			{
				_adventTask.cancel(true);
				_adventTask = null;
			}
			
			if (sendPacket)
			{
				getPlayer().sendPacket(new ExNevitAdventTimeChange(getAdventTime(), true));
			}
		}
	}
	
	private void startNevitEffect(int time)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			if (getEffectTime() > 0)
			{
				stopNevitEffectTask(false);
				time += getEffectTime();
			}
			
			if ((getAdventTime() < ADVENT_TIME) && (time > 0))
			{
				getPlayer().getVariables().set("nevit_b", time);
				getPlayer().sendPacket(new ExNevitAdventEffect(time));
				getPlayer().sendPacket(SystemMessageId.THE_ANGEL_NEVIT_HAS_BLESSED_YOU_FROM_ABOVE);
				getPlayer().startSpecialEffect(AbnormalEffect.NAVIT_ADVENT.getMask());
				_nevitEffectTask = ThreadPoolManager.getInstance().scheduleGeneral(new NevitEffectEnd(), time * 1000L);
			}
		}
	}
	
	protected class NevitEffectEnd implements Runnable
	{
		@Override
		public void run()
		{
			getPlayer().getVariables().remove("nevit_b");
			getPlayer().sendPacket(new ExNevitAdventEffect(0));
			getPlayer().sendPacket(new ExNevitAdventPointInfoPacket(getAdventPoints()));
			getPlayer().sendPacket(SystemMessageId.NEVITS_ADVENT_BLESSING_HAS_ENDED);
			getPlayer().stopSpecialEffect(AbnormalEffect.NAVIT_ADVENT.getMask());
			stopNevitEffectTask(false);
		}
	}
	
	protected void stopNevitEffectTask(boolean saveTime)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			if (_nevitEffectTask != null)
			{
				if (saveTime)
				{
					int time = getEffectTime();
					if (time > 0)
					{
						getPlayer().getVariables().set("nevit_b", time);
					}
					else
					{
						getPlayer().getVariables().remove("nevit_b");
					}
				}
				_nevitEffectTask.cancel(true);
				_nevitEffectTask = null;
			}
		}
	}
	
	public void checkIfMustGivePoints(long baseExp, L2Attackable l2Attackable)
	{
		if (HUNTING_BONUS_ENGINE)
		{
			if (EXTRA_POINTS)
			{
				if (((_adventTask != null) && EXTRA_POINTS_ALL_TIME) || (_adventTask == null))
				{
					int nevitPoints = Math.round(((baseExp / (l2Attackable.getLevel() * l2Attackable.getLevel())) * 100) / 20);
					addPoints(nevitPoints);
				}
			}
		}
	}
	
	public L2PcInstance getPlayer()
	{
		return _player;
	}
	
	@Override
	public int getObjectId()
	{
		return _player.getObjectId();
	}
	
	private int getEffectTime()
	{
		return _nevitEffectTask == null ? 0 : (int) Math.max(0, _nevitEffectTask.getDelay(TimeUnit.SECONDS));
	}
	
	public boolean isAdventBlessingActive()
	{
		return ((_nevitEffectTask != null) && (_nevitEffectTask.getDelay(TimeUnit.MILLISECONDS) > 0));
	}
	
	public static int calcPercent(int points)
	{
		return (int) ((100.0D / MAX_POINTS) * points);
	}
	
	public void setAdventPoints(int points)
	{
		getPlayer().getVariables().set("hunting_points", points);
	}
	
	public void setAdventTime(int time)
	{
		getPlayer().getVariables().set("hunting_time", time);
	}
	
	public int getAdventPoints()
	{
		return getPlayer().getVariables().getInt("hunting_points", 0);
	}
	
	public int getAdventTime()
	{
		return getPlayer().getVariables().getInt("hunting_time", 0);
	}
}