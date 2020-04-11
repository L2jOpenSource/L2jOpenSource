package com.l2jfrozen.gameserver.scripting;

import java.io.File;

import javax.script.ScriptException;

import com.l2jfrozen.Config;

/**
 * Abstract class for classes that are meant to be implemented by scripts.
 * @author KenM
 */
public abstract class ManagedScript
{
	private final File scriptFile;
	private long lastLoadTime;
	private boolean isActive;
	
	public ManagedScript()
	{
		scriptFile = L2ScriptEngineManager.getInstance().getCurrentLoadScript();
		setLastLoadTime(System.currentTimeMillis());
	}
	
	/**
	 * Attempts to reload this script and to refresh the necessary bindings with it ScriptControler.<BR>
	 * Subclasses of this class should override this method to properly refresh their bindings when necessary.
	 * @return true if and only if the script was reloaded, false otherwise.
	 */
	public boolean reload()
	{
		try
		{
			L2ScriptEngineManager.getInstance().executeScript(getScriptFile());
			return true;
		}
		catch (final ScriptException e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			return false;
		}
	}
	
	public abstract boolean unload();
	
	public void setActive(final boolean status)
	{
		isActive = status;
	}
	
	public boolean isActive()
	{
		return isActive;
	}
	
	/**
	 * @return Returns the scriptFile.
	 */
	public File getScriptFile()
	{
		return scriptFile;
	}
	
	/**
	 * @param lastLoadTime The lastLoadTime to set.
	 */
	protected void setLastLoadTime(final long lastLoadTime)
	{
		this.lastLoadTime = lastLoadTime;
	}
	
	/**
	 * @return Returns the lastLoadTime.
	 */
	protected long getLastLoadTime()
	{
		return lastLoadTime;
	}
	
	public abstract String getScriptName();
	
	public abstract ScriptManager<?> getScriptManager();
}
