package com.l2jfrozen.gameserver.handler.admincommandhandlers;

import java.io.File;
import java.util.StringTokenizer;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.cache.CrestCache;
import com.l2jfrozen.gameserver.cache.HtmCache;
import com.l2jfrozen.gameserver.handler.IAdminCommandHandler;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;

/**
 * @version $Revision: 1.1 $
 * @author  ProGramMoS
 */
public class AdminCache implements IAdminCommandHandler
{
	private static final String[] ADMIN_COMMANDS =
	{
		"admin_cache_htm_rebuild",
		"admin_cache_htm_reload",
		"admin_cache_reload_path",
		"admin_cache_reload_file",
		"admin_cache_crest_rebuild",
		"admin_cache_crest_reload",
		"admin_cache_crest_fix"
	};
	
	private enum CommandEnum
	{
		admin_cache_htm_rebuild,
		admin_cache_htm_reload,
		admin_cache_reload_path,
		admin_cache_reload_file,
		admin_cache_crest_rebuild,
		admin_cache_crest_reload,
		admin_cache_crest_fix
	}
	
	@Override
	public boolean useAdminCommand(final String command, final L2PcInstance activeChar)
	{
		final StringTokenizer st = new StringTokenizer(command, " ");
		
		final CommandEnum comm = CommandEnum.valueOf(st.nextToken());
		
		if (comm == null)
		{
			return false;
		}
		
		switch (comm)
		{
			case admin_cache_htm_reload:
			case admin_cache_htm_rebuild:
				HtmCache.getInstance().reload(Config.DATAPACK_ROOT);
				activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB on " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
				return true;
			
			case admin_cache_reload_path:
				if (st.hasMoreTokens())
				{
					final String path = st.nextToken();
					HtmCache.getInstance().reloadPath(new File(Config.DATAPACK_ROOT, path));
					activeChar.sendMessage("Cache[HTML]: " + HtmCache.getInstance().getMemoryUsage() + " MB in " + HtmCache.getInstance().getLoadedFiles() + " file(s) loaded.");
					return true;
				}
				activeChar.sendMessage("Usage: //cache_reload_path <path>");
				return false;
			case admin_cache_reload_file:
				
				if (st.hasMoreTokens())
				{
					
					String path = st.nextToken();
					if (HtmCache.getInstance().loadFile(new File(Config.DATAPACK_ROOT, path)) != null)
					{
						activeChar.sendMessage("Cache[HTML]: file was loaded");
						path = null;
					}
					else
					{
						activeChar.sendMessage("Cache[HTML]: file can't be loaded");
						path = null;
					}
					return true;
				}
				activeChar.sendMessage("Usage: //cache_reload_file <relative_path/file>");
				return false;
			
			case admin_cache_crest_rebuild:
			case admin_cache_crest_reload:
				CrestCache.getInstance().reload();
				activeChar.sendMessage("Cache[Crest]: " + String.format("%.3f", CrestCache.getInstance().getMemoryUsage()) + " megabytes on " + CrestCache.getInstance().getLoadedFiles() + " files loaded");
				return true;
			
			case admin_cache_crest_fix:
				CrestCache.getInstance().convertOldPedgeFiles();
				activeChar.sendMessage("Cache[Crest]: crests fixed");
				return true;
			default:
			{
				return false;
			}
		}
		
	}
	
	@Override
	public String[] getAdminCommandList()
	{
		return ADMIN_COMMANDS;
	}
}
