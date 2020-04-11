package com.l2jfrozen.gameserver.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Luis Arias
 */
public class ScriptPackage
{
	private final List<ScriptDocument> scriptFiles;
	private final List<String> otherFiles;
	private final String name;
	
	public ScriptPackage(final ZipFile pack)
	{
		scriptFiles = new ArrayList<>();
		otherFiles = new ArrayList<>();
		name = pack.getName();
		addFiles(pack);
	}
	
	public List<String> getOtherFiles()
	{
		return otherFiles;
	}
	
	public List<ScriptDocument> getScriptFiles()
	{
		return scriptFiles;
	}
	
	private void addFiles(final ZipFile pack)
	{
		for (final Enumeration<? extends ZipEntry> e = pack.entries(); e.hasMoreElements();)
		{
			final ZipEntry entry = e.nextElement();
			if (entry.getName().endsWith(".xml"))
			{
				try
				{
					final ScriptDocument newScript = new ScriptDocument(entry.getName(), pack.getInputStream(entry));
					scriptFiles.add(newScript);
				}
				catch (final IOException e1)
				{
					e1.printStackTrace();
				}
			}
			else if (!entry.isDirectory())
			{
				otherFiles.add(entry.getName());
			}
		}
	}
	
	public String getName()
	{
		return name;
	}
	
	@Override
	public String toString()
	{
		if (getScriptFiles().isEmpty() && getOtherFiles().isEmpty())
		{
			return "Empty Package.";
		}
		
		String out = "Package Name: " + getName() + "\n";
		
		if (!getScriptFiles().isEmpty())
		{
			out += "Xml Script Files...\n";
			for (final ScriptDocument script : getScriptFiles())
			{
				out += script.getName() + "\n";
			}
		}
		
		if (!getOtherFiles().isEmpty())
		{
			out += "Other Files...\n";
			for (final String fileName : getOtherFiles())
			{
				out += fileName + "\n";
			}
		}
		return out;
	}
}
