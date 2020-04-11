package main.engine.events.daily.randoms;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.util.random.Rnd;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.engine.events.daily.AbstractEvent;
import main.engine.events.daily.randoms.type.AllFlags;
import main.engine.events.daily.randoms.type.CityElpys;
import main.engine.events.daily.randoms.type.SearchChest;
import main.holders.objects.NpcHolder;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class EventRandomManager extends AbstractEvent
{
	/** Events */
	private static final List<AbstractMod> EVENTS = new ArrayList<>();
	{
		try
		{
			if (ConfigData.ALL_FLAGS_Enabled)
			{
				EVENTS.add(new AllFlags());
			}
			if (ConfigData.ELPY_Enabled)
			{
				EVENTS.add(new CityElpys());
			}
			if (ConfigData.CHEST_Enabled)
			{
				EVENTS.add(new SearchChest());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/** Actual event in execution */
	private static AbstractMod mod = null;
	
	public EventRandomManager()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				startTimer("randomEvent", ConfigData.RANDOM_TIME_BETWEEN_EVENTS * 60 * 1000, null, null, false);
				break;
			case END:
				
				break;
		}
	}
	
	@Override
	public void onTimer(String timerName, NpcHolder npc, PlayerHolder player)
	{
		switch (timerName)
		{
			case "randomEvent":
			{
				// Random event
				mod = EVENTS.get(Rnd.get(EVENTS.size()));
				// Init mod
				mod.startMod();
				// Start finish task
				startTimer("cancelEvent", ConfigData.TIME_PER_EVENT * 60 * 1000, null, null, false);
				break;
			}
			case "cancelEvent":
			{
				// End actual mod
				if (mod != null)
				{
					mod.endMod();
				}
				// Start timer for next event
				startTimer("randomEvent", ConfigData.RANDOM_TIME_BETWEEN_EVENTS * 60 * 1000, null, null, false);
				break;
			}
		}
	}
}
