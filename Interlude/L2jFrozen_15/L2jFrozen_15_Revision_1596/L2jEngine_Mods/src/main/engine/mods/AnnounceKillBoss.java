package main.engine.mods;

import com.l2jfrozen.gameserver.model.actor.instance.L2GrandBossInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2PlayableInstance;
import com.l2jfrozen.gameserver.model.actor.instance.L2RaidBossInstance;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.objects.CharacterHolder;
import main.util.Util;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class AnnounceKillBoss extends AbstractMod
{
	public AnnounceKillBoss()
	{
		registerMod(ConfigData.ENABLE_AnnounceKillBoss);
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onKill(CharacterHolder killer, CharacterHolder victim, boolean isPet)
	{
		if (!Util.areObjectType(L2PlayableInstance.class, killer))
		{
			return;
		}
		
		if (Util.areObjectType(L2RaidBossInstance.class, victim))
		{
			UtilMessage.toAllOnline(Say2.TELL, ConfigData.ANNOUNCE_KILL_BOSS.replace("%s1", killer.getInstance().getActingPlayer().getName()).replace("%s2", victim.getInstance().getName()));
			return;
		}
		
		if (Util.areObjectType(L2GrandBossInstance.class, victim))
		{
			UtilMessage.toAllOnline(Say2.TELL, ConfigData.ANNOUNCE_KILL_GRANDBOSS.replace("%s1", killer.getInstance().getActingPlayer().getName()).replace("%s2", victim.getInstance().getName()));
			return;
		}
	}
}
