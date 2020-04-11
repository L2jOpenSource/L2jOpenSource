package main.engine.admin;

import java.util.StringTokenizer;

import main.engine.AbstractMod;
import main.holders.objects.CharacterHolder;
import main.holders.objects.PlayerHolder;

/**
 * @author fissban
 */
public class ServerStatistics extends AbstractMod
{
	public ServerStatistics()
	{
		registerMod(true);
	}
	
	@Override
	public void onModState()
	{
		switch (getState())
		{
			case START:
				//
				break;
			case END:
				//
				break;
		}
	}
	
	@Override
	public void onEvent(PlayerHolder player, CharacterHolder character, String command)
	{
		//
	}
	
	@Override
	public boolean onAdminCommand(PlayerHolder ph, String chat)
	{
		StringTokenizer st = new StringTokenizer(chat, ";");
		
		if (!st.nextToken().equals("engine"))
		{
			return false;
		}
		
		if (!st.hasMoreTokens())
		{
			return false;
		}
		
		String event = st.nextToken();
		switch (event)
		{
			case "die":
			{
				return true;
			}
		}
		
		return false;
	}
}
