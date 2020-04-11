package l2r.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import l2r.Config;
import l2r.gameserver.model.actor.instance.L2PcInstance;

public class Files
{
	private static final Map<String, String> cache = new ConcurrentHashMap<>();
	
	public static String read(String name)
	{
		if (name == null)
		{
			return null;
		}
		if (Config.LAZY_CACHE && cache.containsKey(name))
		{
			return cache.get(name);
		}
		File file = new File("./" + name);
		// _log.info("Get file "+file.getPath());
		if (!file.exists())
		{
			return null;
		}
		String content = null;
		BufferedReader br = null;
		try
		{
			br = new BufferedReader(new UnicodeReader(new FileInputStream(file), "UTF-8"));
			StringBuffer sb = new StringBuffer();
			String s;
			while ((s = br.readLine()) != null)
			{
				sb.append(s).append("\n");
			}
			content = sb.toString();
		}
		catch (Exception e)
		{ /* problem are ignored */
		}
		finally
		{
			try
			{
				if (br != null)
				{
					br.close();
				}
			}
			catch (Exception e1)
			{ /* problems ignored */
			}
		}
		if (Config.LAZY_CACHE)
		{
			cache.put(name, content);
		}
		return content;
	}
	
	public static void cacheClean()
	{
		cache.clear();
	}
	
	public static long lastModified(String name)
	{
		if (name == null)
		{
			return 0;
		}
		return new File(name).lastModified();
	}
	
	public static String read(String name, L2PcInstance player)
	{
		if (player == null)
		{
			return "";
		}
		return read(name);
	}
	
	public static void writeFile(String path, String string)
	{
		if ((string == null) || (string.length() == 0))
		{
			return;
		}
		File target = new File(path);
		if (!target.exists())
		{
			try
			{
				target.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace(System.err);
			}
		}
		try (FileOutputStream fos = new FileOutputStream(target))
		{
			fos.write(string.getBytes("UTF-8"));
		}
		catch (IOException e)
		{
			e.printStackTrace(System.err);
		}
	}
}