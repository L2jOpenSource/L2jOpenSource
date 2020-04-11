
package com.l2jfrozen.gameserver.util;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import com.l2jfrozen.Config;

/**
 * This is a class loader for the dynamic extensions used by DynamicExtension class.
 * @version $Revision: $ $Date: $
 * @author  galun
 */
public class JarClassLoader extends ClassLoader
{
	private final HashSet<String> jars = new HashSet<>();
	
	public void addJarFile(final String filename)
	{
		jars.add(filename);
	}
	
	@Override
	public Class<?> findClass(final String name) throws ClassNotFoundException
	{
		try
		{
			final byte[] b = loadClassData(name);
			return defineClass(name, b, 0, b.length);
		}
		catch (final Exception e)
		{
			if (Config.ENABLE_ALL_EXCEPTIONS)
			{
				e.printStackTrace();
			}
			
			throw new ClassNotFoundException(name);
		}
	}
	
	private byte[] loadClassData(final String name) throws IOException
	{
		byte[] classData = null;
		
		for (final String jarFile : jars)
		{
			boolean breakable = false;
			final File file = new File(jarFile);
			ZipFile zipFile = null;
			InputStream is = null;
			DataInputStream zipStream = null;
			
			try
			{
				zipFile = new ZipFile(file);
				
				final String fileName = name.replace('.', '/') + ".class";
				final ZipEntry entry = zipFile.getEntry(fileName);
				
				if (entry == null)
				{
					continue;
				}
				
				classData = new byte[(int) entry.getSize()];
				
				is = zipFile.getInputStream(entry);
				zipStream = new DataInputStream(is);
				zipStream.readFully(classData, 0, (int) entry.getSize());
				breakable = true;
				
			}
			catch (final ZipException e2)
			{
				e2.printStackTrace();
			}
			catch (final IOException e2)
			{
				e2.printStackTrace();
			}
			finally
			{
				
				if (zipStream != null)
				{
					try
					{
						zipStream.close();
					}
					catch (final IOException e1)
					{
						e1.printStackTrace();
					}
				}
				
				if (is != null)
				{
					try
					{
						is.close();
					}
					catch (final IOException e)
					{
						e.printStackTrace();
					}
				}
				
			}
			
			if (breakable)
			{
				break;
			}
			
		}
		
		if (classData == null)
		{
			throw new IOException("class not found in " + jars);
		}
		
		return classData;
	}
}
