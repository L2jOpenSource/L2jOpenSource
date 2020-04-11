package com.l2jfrozen;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class L2Properties extends Properties
{
	private static final long serialVersionUID = -4599023842346938325L;
	protected static final Logger LOGGER = Logger.getLogger(L2Properties.class);
	
	private boolean warn = false;
	
	public L2Properties()
	{
	}
	
	public L2Properties setLog(final boolean warn)
	{
		this.warn = warn;
		
		return this;
	}
	
	public L2Properties(final String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public L2Properties(final File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	public L2Properties(final InputStream inStream)
	{
		load(inStream);
	}
	
	public L2Properties(final Reader reader)
	{
		load(reader);
	}
	
	public void load(final String name) throws IOException
	{
		load(new FileInputStream(name));
	}
	
	public void load(final File file) throws IOException
	{
		load(new FileInputStream(file));
	}
	
	@Override
	public synchronized void load(final InputStream inStream)
	{
		try
		{
			super.load(inStream);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (inStream != null)
			{
				try
				{
					inStream.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public synchronized void load(final Reader reader)
	{
		try
		{
			super.load(reader);
		}
		catch (final IOException e)
		{
			e.printStackTrace();
		}
		finally
		{
			if (reader != null)
			{
				try
				{
					reader.close();
				}
				catch (final IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	@Override
	public String getProperty(final String key)
	{
		final String property = super.getProperty(key);
		
		if (property == null)
		{
			if (warn)
			{
				LOGGER.warn("L2Properties: Missing property for key - " + key);
			}
			return null;
		}
		return property.trim();
	}
	
	@Override
	public String getProperty(final String key, final String defaultValue)
	{
		final String property = super.getProperty(key, defaultValue);
		
		if (property == null)
		{
			if (warn)
			{
				LOGGER.warn("L2Properties: Missing defaultValue for key - " + key);
			}
			return null;
		}
		return property.trim();
	}
}