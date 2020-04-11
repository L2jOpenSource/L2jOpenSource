package com.l2jfrozen.gameserver.scripting;

/**
 * @author KenM
 * @param  <S>
 */
public abstract class ScriptManager<S extends ManagedScript>
{
	public abstract Iterable<S> getAllManagedScripts();
	
	public boolean reload(final S ms)
	{
		return ms.reload();
	}
	
	public boolean unload(final S ms)
	{
		return ms.unload();
	}
	
	public void setActive(final S ms, final boolean status)
	{
		ms.setActive(status);
	}
	
	public abstract String getScriptManagerName();
}
