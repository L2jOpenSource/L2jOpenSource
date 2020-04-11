package main.engine.events.daily.randoms.type;

import com.l2jfrozen.gameserver.model.L2World;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.engine.AbstractMod;
import main.holders.objects.PlayerHolder;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class AllFlags extends AbstractMod
{
	public AllFlags()
	{
		registerMod(false);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Event: All Flag has ben Started!");
				L2World.getInstance().getAllPlayers().forEach(p -> p.updatePvPFlag(2));// PURPLE
				break;
			case END:
				UtilMessage.toAllOnline(Say2.ANNOUNCEMENT, "Event: All Flag has ben Finished!");
				L2World.getInstance().getAllPlayers().forEach(p -> p.updatePvPFlag(0));// NON_PVP
				break;
		}
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		ph.getInstance().updatePvPFlag(2);// PURPLE
	}
}
