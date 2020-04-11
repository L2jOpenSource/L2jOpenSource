package com.l2jfrozen.gameserver.scripting;

import java.io.File;
import java.io.Serializable;

import javax.script.CompiledScript;

/**
 * @author KenM
 */
public class CompiledScriptHolder implements Serializable
{
	/**
	 * Version 1
	 */
	private static final long serialVersionUID = 1L;
	
	private long lastModified;
	private long size;
	private CompiledScript compiledScript;
	
	public CompiledScriptHolder(final CompiledScript compiledScript, final long lastModified, final long size)
	{
		this.compiledScript = compiledScript;
		this.lastModified = lastModified;
		this.size = size;
	}
	
	public CompiledScriptHolder(final CompiledScript compiledScript, final File scriptFile)
	{
		this(compiledScript, scriptFile.lastModified(), scriptFile.length());
	}
	
	/**
	 * @return Returns the lastModified.
	 */
	public long getLastModified()
	{
		return lastModified;
	}
	
	public void setLastModified(final long lastModified)
	{
		this.lastModified = lastModified;
	}
	
	/**
	 * @return Returns the size.
	 */
	public long getSize()
	{
		return size;
	}
	
	/**
	 * @param size The size to set.
	 */
	public void setSize(final long size)
	{
		this.size = size;
	}
	
	/**
	 * @return Returns the compiledScript.
	 */
	public CompiledScript getCompiledScript()
	{
		return compiledScript;
	}
	
	/**
	 * @param compiledScript The compiledScript to set.
	 */
	public void setCompiledScript(final CompiledScript compiledScript)
	{
		this.compiledScript = compiledScript;
	}
	
	public boolean matches(final File f)
	{
		return f.lastModified() == getLastModified() && f.length() == getSize();
	}
}
