package com.l2jfrozen.gameserver.model.actor.instance;

import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.gameserver.managers.SiegeManager;
import com.l2jfrozen.gameserver.network.serverpackets.NpcHtmlMessage;
import com.l2jfrozen.gameserver.templates.L2NpcTemplate;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author Kerberos
 */
public final class L2CastleTeleporterInstance extends L2NpcInstance
{
	public static final Logger LOGGER = Logger.getLogger(L2CastleTeleporterInstance.class);
	
	private boolean currentTask = false;
	
	/**
	 * Instantiates a new l2 castle teleporter instance.
	 * @param objectId the object id
	 * @param template the template
	 */
	public L2CastleTeleporterInstance(final int objectId, final L2NpcTemplate template)
	{
		super(objectId, template);
	}
	
	@Override
	public void onBypassFeedback(final L2PcInstance player, final String command)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		final String actualCommand = st.nextToken(); // Get actual command
		
		if (actualCommand.equalsIgnoreCase("tele"))
		{
			int delay = SiegeManager.getInstance().getDefenderRespawnDelay();
			
			if (!getTask())
			{
				if (getCastle().getSiege().getIsInProgress())
				{
					// Like L2OFF? When all the Control Towers are destroyed, respawn time is 8 minutes (480,000 miliseconds)
					if (getCastle().getSiege().getControlTowerCount() == 0)
					{
						delay = 480000;
					}
					else
					{
						delay = getCastle().getSiege().getDefenderRespawnDelay();
					}
				}
				
				setTask(true);
				ThreadPoolManager.getInstance().scheduleGeneral(new OustAllPlayers(), delay);
			}
			
			final String filename = "data/html/castleteleporter/MassGK-1.htm";
			final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
			html.setFile(filename);
			player.sendPacket(html);
		}
		else
		{
			super.onBypassFeedback(player, command);
		}
	}
	
	@Override
	public void showChatWindow(final L2PcInstance player)
	{
		String filename;
		if (!getTask())
		{
			if (getCastle().getSiege().getIsInProgress() && getCastle().getSiege().getControlTowerCount() == 0)
			{
				filename = "data/html/castleteleporter/MassGK-2.htm";
			}
			else
			{
				filename = "data/html/castleteleporter/MassGK.htm";
			}
		}
		else
		{
			filename = "data/html/castleteleporter/MassGK-1.htm";
		}
		
		final NpcHtmlMessage html = new NpcHtmlMessage(getObjectId());
		html.setFile(filename);
		html.replace("%objectId%", String.valueOf(getObjectId()));
		player.sendPacket(html);
	}
	
	/**
	 * Teleport defenders of castle inside the castle but outside of the respawn room where a Guard stands
	 */
	public void oustAllPlayers()
	{
		getCastle().oustAllPlayers();
	}
	
	public class OustAllPlayers implements Runnable
	{
		
		@Override
		public void run()
		{
			try
			{
				oustAllPlayers();
				setTask(false);
			}
			catch (final NullPointerException e)
			{
				LOGGER.warn("" + e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Gets the task.
	 * @return the task
	 */
	public boolean getTask()
	{
		return currentTask;
	}
	
	/**
	 * Sets the task.
	 * @param state the new task
	 */
	public void setTask(final boolean state)
	{
		currentTask = state;
	}
	
}