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
package com.l2jserver.tools.util.io;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author mrTJO
 */
public class FileWriterStdout extends BufferedWriter
{
	public FileWriterStdout(FileWriter fileWriter)
	{
		super(fileWriter);
	}
	
	public void println() throws IOException
	{
		append(System.getProperty("line.separator"));
	}
	
	public void println(String line) throws IOException
	{
		append(line + System.getProperty("line.separator"));
	}
	
	public void print(String text) throws IOException
	{
		append(text);
	}
}
