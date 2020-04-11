package net.sf.l2j.gameserver.handler.admincommandhandlers;

import java.util.StringTokenizer;

import net.sf.l2j.gameserver.data.xml.PlayerLevelData;
import net.sf.l2j.gameserver.handler.IAdminCommandHandler;
import net.sf.l2j.gameserver.model.PlayerLevel;
import net.sf.l2j.gameserver.model.WorldObject;
import net.sf.l2j.gameserver.model.actor.Playable;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.network.SystemMessageId;

public class AdminLevel implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_addlevel",
		"admin_setlevel"
	};
	
	@Override
	public boolean useAdminCommand(String command, Player activeChar)
	{
		if (activeChar == null)
			return false;
		
		final WorldObject target = activeChar.getTarget();
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken();
		
		if (actualCommand.equalsIgnoreCase("admin_addlevel"))
		{
			if (!(target instanceof Playable))
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			try
			{
				((Playable) target).getStat().addLevel(Byte.parseByte(st.nextToken()));
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //addlevel <level>.");
				return false;
			}
		}
		else if (actualCommand.equalsIgnoreCase("admin_setlevel"))
		{
			if (!(target instanceof Player))
			{
				activeChar.sendPacket(SystemMessageId.TARGET_IS_INCORRECT);
				return false;
			}
			
			try
			{
				final PlayerLevel pl = PlayerLevelData.getInstance().getPlayerLevel(Integer.parseInt(st.nextToken()));
				if (pl == null)
				{
					activeChar.sendMessage("Invalid used level for //setlevel.");
					return false;
				}
				
				long pXp = ((Player) target).getExp();
				long tXp = pl.getRequiredExpToLevelUp();
				
				if (pXp > tXp)
					((Player) target).removeExpAndSp(pXp - tXp, 0);
				else if (pXp < tXp)
					((Player) target).addExpAndSp(tXp - pXp, 0);
			}
			catch (Exception e)
			{
				activeChar.sendMessage("Usage: //setlevel <level>.");
				return false;
			}
		}
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}