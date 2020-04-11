/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.gameserver.script;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.l2jserver.Config;

/**
 * @author Luis Arias
 */
public class ScriptPackage
{
	private static final Logger _log = Logger.getLogger(ScriptPackage.class.getName());
	
	private final List<ScriptDocument> _scriptFiles = new ArrayList<>();
	private final List<String> _otherFiles = new ArrayList<>();
	private final String _name;
	
	public ScriptPackage(ZipFile pack)
	{
		_name = pack.getName();
		addFiles(pack);
	}
	
	/**
	 * @return Returns the otherFiles.
	 */
	public List<String> getOtherFiles()
	{
		return _otherFiles;
	}
	
	/**
	 * @return Returns the scriptFiles.
	 */
	public List<ScriptDocument> getScriptFiles()
	{
		return _scriptFiles;
	}
	
	/**
	 * @param pack
	 */
	private void addFiles(ZipFile pack)
	{
		for (Enumeration<? extends ZipEntry> e = pack.entries(); e.hasMoreElements();)
		{
			ZipEntry entry = e.nextElement();
			if (entry.getName().endsWith(".xml"))
			{
				try
				{
					_scriptFiles.add(new ScriptDocument(entry.getName(), pack.getInputStream(entry)));
				}
				catch (IOException io)
				{
					_log.warning(getClass().getSimpleName() + ": " + io.getMessage());
				}
			}
			else if (!entry.isDirectory())
			{
				_otherFiles.add(entry.getName());
			}
		}
	}
	
	/**
	 * @return Returns the name.
	 */
	public String getName()
	{
		return _name;
	}
	
	@Override
	public String toString()
	{
		if (getScriptFiles().isEmpty() && getOtherFiles().isEmpty())
		{
			return "Empty Package.";
		}
		
		StringBuilder out = new StringBuilder();
		out.append("Package Name: ");
		out.append(getName());
		out.append(Config.EOL);
		
		if (!getScriptFiles().isEmpty())
		{
			out.append("Xml Script Files..." + Config.EOL);
			for (ScriptDocument script : getScriptFiles())
			{
				out.append(script.getName());
				out.append(Config.EOL);
			}
		}
		
		if (!getOtherFiles().isEmpty())
		{
			out.append("Other Files..." + Config.EOL);
			for (String fileName : getOtherFiles())
			{
				out.append(fileName);
				out.append(Config.EOL);
			}
		}
		return out.toString();
	}
}
