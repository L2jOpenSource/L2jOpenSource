package com.l2jfrozen.gameserver.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.l2jfrozen.logs.Log;

public class GMAudit
{
	private static final Logger LOGGER = Logger.getLogger(Log.class);
	
	public static void auditGMAction(final String gmName, final String action, final String target, final String params)
	{
		String path = "log/gm_audit/";
		new File(path).mkdirs();
		final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy H:mm:ss");
		final String today = formatter.format(new Date());
		
		FileWriter save = null;
		try
		{
			final File file = new File(path + gmName + ".txt");
			save = new FileWriter(file, true);
			
			final String out = "[" + today + "] --> GM: " + gmName + ", Target: [" + target + "], Action: [" + action + "], Params: [" + params + "] \r\n";
			
			save.write(out);
		}
		catch (final IOException e)
		{
			LOGGER.error("GMAudit for GM " + gmName + " could not be saved: ", e);
		}
		finally
		{
			if (save != null)
			{
				try
				{
					save.close();
				}
				catch (final Exception e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void auditGMAction(final String gmName, final String action, final String target)
	{
		auditGMAction(gmName, action, target, "");
	}
}