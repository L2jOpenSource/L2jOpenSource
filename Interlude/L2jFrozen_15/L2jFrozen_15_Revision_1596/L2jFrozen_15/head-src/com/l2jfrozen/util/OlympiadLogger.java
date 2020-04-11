
/**
 @author ProGramMoS, scoria dev
 version 0.1.1, 2009-04-08
 */
package com.l2jfrozen.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

public class OlympiadLogger
{
	private static final Logger LOGGER = Logger.getLogger(OlympiadLogger.class);
	
	public static final void add(final String text, final String cat)
	{
		String date = new SimpleDateFormat("yy.MM.dd H:mm:ss").format(new Date());
		
		new File("log/game").mkdirs();
		final File file = new File("log/game/" + (cat != null ? cat : "_all") + ".txt");
		FileWriter save = null;
		try
		{
			save = new FileWriter(file, true);
			final String out = "[" + date + "] '---': " + text + "\n"; // "+char_name()+"
			save.write(out);
			save.flush();
		}
		catch (final IOException e)
		{
			LOGGER.warn("saving chat LOGGER failed: " + e);
			e.printStackTrace();
		}
		finally
		{
			
			if (save != null)
			{
				try
				{
					save.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (cat != null)
		{
			add(text, null);
		}
		
		date = null;
	}
	
	public static final void Assert(final boolean exp, final String cmt)
	{
		if (exp || !Config.ASSERT)
		{
			return;
		}
		
		LOGGER.info("Assertion error [" + cmt + "]");
		Thread.dumpStack();
	}
}