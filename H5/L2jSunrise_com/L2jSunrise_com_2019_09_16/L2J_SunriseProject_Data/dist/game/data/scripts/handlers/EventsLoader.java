package handlers;

import gr.sr.handler.ABLoader;

import events.CharacterBirthday.CharacterBirthday;
import events.SquashEvent.SquashEvent;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class EventsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		CharacterBirthday.class,
		
		// Disabled by default events
		// FreyaCelebration.class,
		// GiftOfVitality.class,
		// HeavyMedal.class,
		// LoveYourGatekeeper.class,
		// MasterOfEnchanting.class,
		// SavingSanta.class,
		SquashEvent.class,
		// TheValentineEvent.class,
	};
	
	public EventsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
