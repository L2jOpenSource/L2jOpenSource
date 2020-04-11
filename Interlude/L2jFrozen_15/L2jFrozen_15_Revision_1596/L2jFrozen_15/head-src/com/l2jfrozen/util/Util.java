package com.l2jfrozen.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import javolution.text.TextBuilder;

/**
 * This class ...
 * @version $Revision: 1.2 $ $Date: 2004/06/27 08:12:59 $
 * @author  luisantonioa
 */
public class Util
{
	protected static final Logger LOGGER = Logger.getLogger(Util.class);
	
	public static boolean isInternalIP(final String ipAddress)
	{
		return ipAddress.startsWith("192.168.") || ipAddress.startsWith("10.") || ipAddress.startsWith("127.0.0.1");
	}
	
	public static String printData(final byte[] data, final int len)
	{
		final TextBuilder result = new TextBuilder();
		
		int counter = 0;
		
		for (int i = 0; i < len; i++)
		{
			if (counter % 16 == 0)
			{
				result.append(fillHex(i, 4) + ": ");
			}
			
			result.append(fillHex(data[i] & 0xff, 2) + " ");
			counter++;
			if (counter == 16)
			{
				result.append("   ");
				
				int charpoint = i - 15;
				for (int a = 0; a < 16; a++)
				{
					final int t1 = data[charpoint++];
					if (t1 > 0x1f && t1 < 0x80)
					{
						result.append((char) t1);
					}
					else
					{
						result.append('.');
					}
				}
				
				result.append('\n');
				counter = 0;
			}
		}
		
		final int rest = data.length % 16;
		if (rest > 0)
		{
			for (int i = 0; i < 17 - rest; i++)
			{
				result.append("   ");
			}
			
			int charpoint = data.length - rest;
			for (int a = 0; a < rest; a++)
			{
				final int t1 = data[charpoint++];
				if (t1 > 0x1f && t1 < 0x80)
				{
					result.append((char) t1);
				}
				else
				{
					result.append('.');
				}
			}
			
			result.append('\n');
		}
		
		return result.toString();
	}
	
	public static String fillHex(final int data, final int digits)
	{
		String number = Integer.toHexString(data);
		
		for (int i = number.length(); i < digits; i++)
		{
			number = "0" + number;
		}
		
		return number;
	}
	
	/**
	 * @param s
	 */
	
	public static void printSection(String s)
	{
		final int maxlength = 68;
		s = "-[ " + s + " ]";
		final int slen = s.length();
		if (slen > maxlength)
		{
			LOGGER.info(s);
			return;
		}
		int i;
		for (i = 0; i < maxlength - slen; i++)
		{
			s = "=" + s;
		}
		LOGGER.info(s);
	}
	
	/**
	 * @param  raw
	 * @return
	 */
	public static String printData(final byte[] raw)
	{
		return printData(raw, raw.length);
	}
	
	/**
	 * returns how many processors are installed on this system.
	 */
	private static void printCpuInfo()
	{
		LOGGER.info("Avaible CPU(s): " + Runtime.getRuntime().availableProcessors());
		LOGGER.info("Processor(s) Identifier: " + System.getenv("PROCESSOR_IDENTIFIER"));
		LOGGER.info("..................................................");
		LOGGER.info("..................................................");
	}
	
	/**
	 * returns the operational system server is running on it.
	 */
	private static void printOSInfo()
	{
		LOGGER.info("OS: " + System.getProperty("os.name") + " Build: " + System.getProperty("os.version"));
		LOGGER.info("OS Arch: " + System.getProperty("os.arch"));
		LOGGER.info("..................................................");
		LOGGER.info("..................................................");
	}
	
	/**
	 * returns JAVA Runtime Enviroment properties
	 */
	private static void printJreInfo()
	{
		LOGGER.info("Java Platform Information");
		LOGGER.info("Java Runtime  Name: " + System.getProperty("java.runtime.name"));
		LOGGER.info("Java Version: " + System.getProperty("java.version"));
		LOGGER.info("Java Class Version: " + System.getProperty("java.class.version"));
		LOGGER.info("..................................................");
		LOGGER.info("..................................................");
	}
	
	/**
	 * returns general infos related to machine
	 */
	private static void printRuntimeInfo()
	{
		LOGGER.info("Runtime Information");
		LOGGER.info("Current Free Heap Size: " + Runtime.getRuntime().freeMemory() / 1024 / 1024 + " mb");
		LOGGER.info("Current Heap Size: " + Runtime.getRuntime().totalMemory() / 1024 / 1024 + " mb");
		LOGGER.info("Maximum Heap Size: " + Runtime.getRuntime().maxMemory() / 1024 / 1024 + " mb");
		LOGGER.info("..................................................");
		LOGGER.info("..................................................");
		
	}
	
	/**
	 * calls time service to get system time.
	 */
	private static void printSystemTime()
	{
		// instanciates Date Objec
		final Date dateInfo = new Date();
		
		// generates a simple date format
		final SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss aa");
		
		// generates String that will get the formater info with values
		final String dayInfo = df.format(dateInfo);
		
		LOGGER.info("..................................................");
		LOGGER.info("System Time: " + dayInfo);
		LOGGER.info("..................................................");
	}
	
	/**
	 * gets system JVM properties.
	 */
	private static void printJvmInfo()
	{
		LOGGER.info("Virtual Machine Information (JVM)");
		LOGGER.info("JVM Name: " + System.getProperty("java.vm.name"));
		LOGGER.info("JVM installation directory: " + System.getProperty("java.home"));
		LOGGER.info("JVM version: " + System.getProperty("java.vm.version"));
		LOGGER.info("JVM Vendor: " + System.getProperty("java.vm.vendor"));
		LOGGER.info("JVM Info: " + System.getProperty("java.vm.info"));
		LOGGER.info("..................................................");
		LOGGER.info("..................................................");
	}
	
	/**
	 * prints all other methods.
	 */
	public static void printGeneralSystemInfo()
	{
		printSystemTime();
		printOSInfo();
		printCpuInfo();
		printRuntimeInfo();
		printJreInfo();
		printJvmInfo();
	}
	
	/**
	 * converts a given time from minutes -> miliseconds
	 * @param  minutesToConvert
	 * @return
	 */
	public static int convertMinutesToMiliseconds(final int minutesToConvert)
	{
		return minutesToConvert * 60000;
	}
	
	public static int getAvailableProcessors()
	{
		final Runtime rt = Runtime.getRuntime();
		return rt.availableProcessors();
	}
	
	public static String getOSName()
	{
		return System.getProperty("os.name");
	}
	
	public static String getOSVersion()
	{
		return System.getProperty("os.version");
	}
	
	public static String getOSArch()
	{
		return System.getProperty("os.arch");
	}
}
