package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;

import javax.script.ScriptException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author KidZor
 */

public class AdminScript implements IAdminCommandHandler
{
	private static final File SCRIPT_FOLDER = new File(Config.DATAPACK_ROOT.getAbsolutePath(), "data/scripts");
	private static final Logger LOGGER = Logger.getLogger(AdminScript.class);
	
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_load_script"
	};
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		if (command.startsWith("admin_load_script"))
		{
			File file;
			StringTokenizer st = new StringTokenizer(command, " ");
			st.nextToken();
			final String line = st.nextToken();
			
			try
			{
				file = new File(SCRIPT_FOLDER, line);
				
				if (file.isFile())
				{
					try
					{
						L2ScriptEngineManager.getInstance().executeScript(file);
					}
					catch (final ScriptException e)
					{
						if (Config.ENABLE_ALL_EXCEPTIONS)
						{
							e.printStackTrace();
						}
						
						L2ScriptEngineManager.getInstance().reportScriptFileError(file, e);
					}
				}
				else
				{
					LOGGER.warn("Failed loading: (" + file.getCanonicalPath() + " - Reason: doesnt exists or is not a file.");
				}
			}
			catch (final Exception e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
			st = null;
		}
		
		return true;
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
