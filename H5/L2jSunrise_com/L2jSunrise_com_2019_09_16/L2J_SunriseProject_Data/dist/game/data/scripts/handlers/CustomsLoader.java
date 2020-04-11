package handlers;

import gr.sr.handler.ABLoader;

import custom.EchoCrystals.EchoCrystals;
import custom.FifthAnniversary.FifthAnniversary;
import custom.NewbieCoupons.NewbieCoupons;
import custom.NpcLocationInfo.NpcLocationInfo;
import custom.PinsAndPouchUnseal.PinsAndPouchUnseal;
import custom.RaidbossInfo.RaidbossInfo;
import custom.ShadowWeapons.ShadowWeapons;
import custom.Validators.SubClassSkills;
import custom.events.Wedding.Wedding;
import handlers.custom.CustomAnnouncePkPvP;

/**
 * @author L2jSunrise Team
 * @Website www.l2jsunrise.com
 */
public final class CustomsLoader extends ABLoader
{
	private final Class<?>[] SCRIPTS =
	{
		CustomAnnouncePkPvP.class,
		// AutoAdenaToGoldBar.class,
		EchoCrystals.class,
		FifthAnniversary.class,
		NewbieCoupons.class,
		NpcLocationInfo.class,
		PinsAndPouchUnseal.class,
		RaidbossInfo.class,
		ShadowWeapons.class,
		SubClassSkills.class,
		Wedding.class,
	};
	
	public CustomsLoader()
	{
		loadScripts();
	}
	
	@Override
	public Class<?>[] getScripts()
	{
		return SCRIPTS;
	}
}
