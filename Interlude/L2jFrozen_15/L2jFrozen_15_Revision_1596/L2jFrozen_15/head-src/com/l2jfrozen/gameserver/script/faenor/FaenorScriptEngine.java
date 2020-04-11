package com.l2jfrozen.gameserver.script.faenor;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.Parser;
import com.l2jfrozen.gameserver.script.ParserNotCreatedException;
import com.l2jfrozen.gameserver.script.ScriptDocument;
import com.l2jfrozen.gameserver.script.ScriptEngine;
import com.l2jfrozen.gameserver.script.ScriptPackage;
import com.l2jfrozen.gameserver.scripting.L2ScriptEngineManager;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	static Logger LOGGER = Logger.getLogger(FaenorScriptEngine.class);
	public final static String PACKAGE_DIRECTORY = "data/faenor/";
	public final static boolean DEBUG = true;
	
	private LinkedList<ScriptDocument> scripts;
	
	public static FaenorScriptEngine getInstance()
	{
		return SingletonHolder.instance;
	}
	
	public FaenorScriptEngine()
	{
		scripts = new LinkedList<>();
		loadPackages();
		parsePackages();
	}
	
	public void reloadPackages()
	{
		scripts = new LinkedList<>();
		parsePackages();
	}
	
	private void loadPackages()
	{
		final File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);// LOGGER.sss(packDirectory.getAbsolutePath());
		
		final FileFilter fileFilter = file -> file.getName().endsWith(".zip");
		
		final File[] files = packDirectory.listFiles(fileFilter);
		if (files == null)
		{
			LOGGER.info("[FeanorScriptEngine] No Packages Loaded ...");
			return;
		}
		ZipFile zipPack;
		
		LOGGER.info("[FeanorScriptEngine] Loading files ...");
		
		for (final File file : files)
		{
			try
			{
				zipPack = new ZipFile(file);
			}
			catch (final ZipException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				continue;
			}
			
			final ScriptPackage module = new ScriptPackage(zipPack);
			
			final List<ScriptDocument> scrpts = module.getScriptFiles();
			for (final ScriptDocument script : scrpts)
			{
				scripts.add(script);
			}
			try
			{
				zipPack.close();
			}
			catch (final IOException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
			}
		}
		
		if (!scripts.isEmpty())
		{
			LOGGER.info("[FeanorScriptEngine] Loaded " + scripts.size() + " scripts ...");
		}
	}
	
	public void orderScripts()
	{
		if (scripts.size() > 1)
		{
			// ScriptDocument npcInfo = null;
			
			for (int i = 0; i < scripts.size();)
			{
				if (scripts.get(i).getName().contains("NpcStatData"))
				{
					scripts.addFirst(scripts.remove(i));
					// scripts.set(i, scripts.get(0));
					// scripts.set(0, npcInfo);
				}
				else
				{
					i++;
				}
			}
		}
	}
	
	public void parsePackages()
	{
		final L2ScriptEngineManager sem = L2ScriptEngineManager.getInstance();
		final ScriptContext context = sem.getScriptContext("beanshell");
		try
		{
			sem.eval("beanshell", "double log1p(double d) { return Math.log1p(d); }");
			sem.eval("beanshell", "double pow(double d, double p) { return Math.pow(d,p); }");
			
			for (final ScriptDocument script : scripts)
			{
				parseScript(script, context);
			}
		}
		catch (final ScriptException e)
		{
			e.printStackTrace();
		}
	}
	
	public void parseScript(final ScriptDocument script, final ScriptContext context)
	{
		if (DEBUG)
		{
			LOGGER.debug("Parsing Script: " + script.getName());
		}
		
		final Node node = script.getDocument().getFirstChild();
		final String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		
		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (final ParserNotCreatedException e)
		{
			LOGGER.warn("ERROR: No parser registered for Script: " + parserClass);
			e.printStackTrace();
		}
		
		if (parser == null)
		{
			LOGGER.warn("Unknown Script Type: " + script.getName());
			return;
		}
		
		try
		{
			parser.parseScript(node, context);
			LOGGER.info(script.getName() + " script Sucessfullty Parsed.");
		}
		catch (final Exception e)
		{
			e.printStackTrace();
			LOGGER.warn("Script Parsing Failed.");
		}
	}
	
	@Override
	public String toString()
	{
		if (scripts.isEmpty())
		{
			return "No Packages Loaded.";
		}
		
		String out = "Script Packages currently loaded:\n";
		
		for (final ScriptDocument script : scripts)
		{
			out += script;
		}
		return out;
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorScriptEngine instance = new FaenorScriptEngine();
	}
}
