/*
 * Copyright (C) 2004-2014 L2J Server
 * 
 * This file is part of L2J Server.
 * 
 * L2J Server is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J Server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.script.faenor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.script.ScriptContext;

import l2r.Config;
import l2r.gameserver.script.Parser;
import l2r.gameserver.script.ParserNotCreatedException;
import l2r.gameserver.script.ScriptDocument;
import l2r.gameserver.script.ScriptEngine;
import l2r.util.file.filter.XMLFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

/**
 * @author Luis Arias
 */
public class FaenorScriptEngine extends ScriptEngine
{
	private static final Logger _log = LoggerFactory.getLogger(FaenorScriptEngine.class);
	public static final String PACKAGE_DIRECTORY = "data/xml/faenor/";
	
	protected FaenorScriptEngine()
	{
		final File packDirectory = new File(Config.DATAPACK_ROOT, PACKAGE_DIRECTORY);
		final File[] files = packDirectory.listFiles(new XMLFilter());
		for (File file : files)
		{
			try (InputStream in = new FileInputStream(file))
			{
				parseScript(new ScriptDocument(file.getName(), in), null);
			}
			catch (IOException e)
			{
				_log.warn(e.getMessage(), e);
			}
		}
	}
	
	public void parseScript(ScriptDocument script, ScriptContext context)
	{
		Node node = script.getDocument().getFirstChild();
		String parserClass = "faenor.Faenor" + node.getNodeName() + "Parser";
		
		Parser parser = null;
		try
		{
			parser = createParser(parserClass);
		}
		catch (ParserNotCreatedException e)
		{
			_log.warn("ERROR: No parser registered for Script: " + parserClass + ": " + e.getMessage(), e);
		}
		
		if (parser == null)
		{
			_log.warn("Unknown Script Type: " + script.getName());
			return;
		}
		
		try
		{
			parser.parseScript(node, context);
			_log.info(getClass().getSimpleName() + ": Loaded  " + script.getName() + " successfully.");
		}
		catch (Exception e)
		{
			_log.warn("Script Parsing Failed: " + e.getMessage(), e);
		}
	}
	
	public static FaenorScriptEngine getInstance()
	{
		return SingletonHolder._instance;
	}
	
	private static class SingletonHolder
	{
		protected static final FaenorScriptEngine _instance = new FaenorScriptEngine();
	}
}
