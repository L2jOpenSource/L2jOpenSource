package com.l2jfrozen.gameserver.scripting;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;

/**
 * Cache of Compiled Scripts
 * @author KenM
 */
public class CompiledScriptCache implements Serializable
{
	/**
	 * Version 1
	 */
	private static final long serialVersionUID = 1L;
	
	private static final Logger LOG = Logger.getLogger(CompiledScriptCache.class);
	
	private final Map<String, CompiledScriptHolder> compiledScriptCache = new HashMap<>();
	private transient boolean modified = false;
	
	public CompiledScript loadCompiledScript(final ScriptEngine engine, final File file) throws ScriptException
	{
		final int len = L2ScriptEngineManager.SCRIPT_FOLDER.getPath().length() + 1;
		final String relativeName = file.getPath().substring(len);
		
		final CompiledScriptHolder csh = compiledScriptCache.get(relativeName);
		if (csh != null && csh.matches(file))
		{
			if (Config.DEBUG)
			{
				LOG.debug("Reusing cached compiled script: " + file);
			}
			return csh.getCompiledScript();
		}
		
		if (Config.DEBUG)
		{
			LOG.info("Compiling script: " + file);
		}
		
		final Compilable eng = (Compilable) engine;
		FileInputStream fis = null;
		
		BufferedReader buff = null;
		InputStreamReader isr = null;
		CompiledScript cs = null;
		
		try
		{
			
			fis = new FileInputStream(file);
			isr = new InputStreamReader(fis);
			buff = new BufferedReader(isr);
			
			// TODO lock file
			cs = eng.compile(buff);
			if (cs instanceof Serializable)
			{
				synchronized (compiledScriptCache)
				{
					compiledScriptCache.put(relativeName, new CompiledScriptHolder(cs, file));
					modified = true;
				}
			}
			
		}
		catch (final IOException e)
		{
			
			e.printStackTrace();
			
		}
		finally
		{
			
			if (buff != null)
			{
				try
				{
					buff.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (isr != null)
			{
				try
				{
					isr.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
			if (fis != null)
			{
				try
				{
					fis.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		return cs;
	}
	
	public boolean isModified()
	{
		return modified;
	}
	
	public void purge()
	{
		synchronized (compiledScriptCache)
		{
			for (final String path : compiledScriptCache.keySet())
			{
				final File file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, path);
				if (!file.isFile())
				{
					compiledScriptCache.remove(path);
					modified = true;
				}
			}
		}
	}
	
	public void save()
	{
		synchronized (compiledScriptCache)
		{
			File file = null;
			FileOutputStream out = null;
			ObjectOutputStream oos = null;
			
			try
			{
				file = new File(L2ScriptEngineManager.SCRIPT_FOLDER, "CompiledScripts.cache");
				out = new FileOutputStream(file);
				oos = new ObjectOutputStream(out);
				oos.writeObject(this);
				modified = false;
			}
			catch (final FileNotFoundException e)
			{
				e.printStackTrace();
			}
			catch (final IOException e)
			{
				e.printStackTrace();
				
			}
			finally
			{
				
				if (oos != null)
				{
					try
					{
						oos.close();
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
				
				if (out != null)
				{
					try
					{
						out.close();
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
				
			}
			
		}
	}
}
