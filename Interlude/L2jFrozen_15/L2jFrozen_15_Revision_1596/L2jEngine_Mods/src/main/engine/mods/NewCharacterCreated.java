package main.engine.mods;

import java.util.ArrayList;
import java.util.List;

import com.l2jfrozen.gameserver.model.L2Skill;
import com.l2jfrozen.gameserver.network.clientpackets.Say2;

import main.data.properties.ConfigData;
import main.engine.AbstractMod;
import main.holders.IntIntHolder;
import main.holders.objects.PlayerHolder;
import main.util.UtilMessage;

/**
 * @author fissban
 */
public class NewCharacterCreated extends AbstractMod
{
	private static List<Integer> players = new ArrayList<>();
	
	public NewCharacterCreated()
	{
		registerMod(true);// TODO missing enable/disable config
	}
	
	@Override
	public void onModState()
	{
		//
	}
	
	@Override
	public void onCreateCharacter(PlayerHolder ph)
	{
		// a new title for the character is assigned.
		ph.getInstance().setTitle(ConfigData.NEW_CHARACTER_CREATED_TITLE);
		
		players.add(ph.getObjectId());
	}
	
	@Override
	public void onEnterWorld(PlayerHolder ph)
	{
		if (players.contains(ph.getObjectId()))
		{
			if (ConfigData.NEW_CHARACTER_CREATED_GIVE_BUFF)
			{
				// buffs list is delivered
				for (IntIntHolder bsh : ConfigData.NEW_CHARACTER_CREATED_BUFFS_WARRIORS)
				{
					L2Skill skill = bsh.getSkill();
					if (skill != null)
					{
						skill.getEffects(ph.getInstance(), ph.getInstance());
					}
				}
			}
			
			if (!ConfigData.NEW_CHARACTER_CREATED_SEND_SCREEN_MSG.equals(""))
			{
				UtilMessage.sendCreatureMsg(ph, Say2.TELL, "[System]", ConfigData.NEW_CHARACTER_CREATED_SEND_SCREEN_MSG);
			}
			
			players.remove(Integer.valueOf(ph.getObjectId()));
		}
	}
}
