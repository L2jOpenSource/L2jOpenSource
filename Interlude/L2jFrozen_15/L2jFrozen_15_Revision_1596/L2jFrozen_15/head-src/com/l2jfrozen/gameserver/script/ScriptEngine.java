package com.l2jfrozen.gameserver.script;

import java.util.Hashtable;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.script.faenor.FaenorInterface;

/**
 * @author Luis Arias
 */
public class ScriptEngine
{
	protected EngineInterface utils = FaenorInterface.getInstance();
	public static final Hashtable<String, ParserFactory> parserFactories = new Hashtable<>();
	
	protected static Parser createParser(final String name) throws ParserNotCreatedException
	{
		ParserFactory s = parserFactories.get(name);
		if (s == null) // shape not found
		{
			try
			{
				Class.forName("com.l2jfrozen.gameserver.script." + name);
				// By now the static block with no function would
				// have been executed if the shape was found.
				// the shape is expected to have put its factory
				// in the hashtable.
				
				s = parserFactories.get(name);
				if (s == null)
				{
					throw new ParserNotCreatedException();
				}
			}
			catch (final ClassNotFoundException e)
			{
				if (Config.ENABLE_ALL_EXCEPTIONS)
				{
					e.printStackTrace();
				}
				
				// We'll throw an exception to indicate that
				// the shape could not be created
				throw new ParserNotCreatedException();
			}
		}
		return s.create();
	}
}
