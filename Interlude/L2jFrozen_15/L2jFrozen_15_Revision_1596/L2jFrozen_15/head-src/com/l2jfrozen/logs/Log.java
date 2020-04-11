package com.l2jfrozen.logs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

/**
 * @author ProGramMoS
 * @author scoria dev
 * @author ReynalDev
 */
public class Log
{
	private static final Logger LOGGER = Logger.getLogger(Log.class);
	private static final SimpleDateFormat LOG_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss,SSS");
	private static final SimpleDateFormat FILE_NAME_DATE_FORMAT = new SimpleDateFormat("dd_MM_yyyy");
	
	/**
	 * By default path is <b>log/game/.....</b>
	 * @param text     the text you want to include in the file
	 * @param fileName name of file. Extension name is not need it, by default all the log files have <b>.txt</b> extension.
	 */
	public static final void add(String text, String fileName)
	{
		add(text, "log/game/", fileName);
	}
	
	/**
	 * @param text     The line to add to the log file
	 * @param path     for example <b>log/directory/subdirectory/</b>, make sure path ends with <b>/</b>
	 * @param fileName name of file. Extension name is not need it, by default all the log files have <b>.txt</b> extension.
	 */
	public static final void add(String text, String path, String fileName)
	{
		Date now = new Date();
		String date = LOG_DATE_FORMAT.format(now);
		String fileDate = FILE_NAME_DATE_FORMAT.format(now);
		
		new File(path).mkdirs();
		String pathName = path;
		
		if (fileName != null)
		{
			pathName += fileName;
		}
		else
		{
			pathName += "_all";
		}
		
		pathName += "_";
		pathName += fileDate;
		pathName += ".txt";
		
		File file = new File(pathName);
		
		try (FileWriter save = new FileWriter(file, true);)
		{
			String out = "[" + date + "] '---': " + text + "\n";
			save.write(out);
			save.flush();
		}
		catch (IOException e)
		{
			LOGGER.error("Log.add : Problem during creating the file" + e);
		}
	}
}
