package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.templates.L2Henna;
import com.l2jfrozen.gameserver.templates.StatsSet;

/**
 * This class ...
 * @version $Revision$ $Date$
 */
public class HennaTable
{
	private static Logger LOGGER = Logger.getLogger(HennaTable.class);
	
	private static HennaTable instance;
	
	private final Map<Integer, L2Henna> henna;
	private final boolean initialized = true;
	
	public static HennaTable getInstance()
	{
		if (instance == null)
		{
			instance = new HennaTable();
		}
		
		return instance;
	}
	
	private HennaTable()
	{
		henna = new HashMap<>();
		restoreHennaData();
	}
	
	private void restoreHennaData()
	{
		File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/henna.csv");
		
		try (FileReader reader = new FileReader(fileData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff))
		{
			String line = null;
			
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				final StringTokenizer st = new StringTokenizer(line, ";");
				
				StatsSet hennaDat = new StatsSet();
				final int id = Integer.parseInt(st.nextToken());
				hennaDat.set("symbol_id", id);
				st.nextToken(); // next token...ignore name
				hennaDat.set("dye", Integer.parseInt(st.nextToken()));
				hennaDat.set("amount", Integer.parseInt(st.nextToken()));
				hennaDat.set("price", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_INT", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_STR", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_CON", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_MEM", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_DEX", Integer.parseInt(st.nextToken()));
				hennaDat.set("stat_WIT", Integer.parseInt(st.nextToken()));
				
				L2Henna template = new L2Henna(hennaDat);
				henna.put(id, template);
				hennaDat = null;
				template = null;
			}
			
			LOGGER.info("HennaTable: Loaded " + henna.size() + " Templates.");
		}
		catch (FileNotFoundException e)
		{
			LOGGER.error("HennaTable.restoreHennaData : " + Config.DATAPACK_ROOT + "/data/csv/henna.csv is missing in data folder. ");
		}
		catch (IOException e)
		{
			LOGGER.error("HennaTable.restoreHennaData : Error while creating table. ", e);
		}
	}
	
	public boolean isInitialized()
	{
		return initialized;
	}
	
	public L2Henna getTemplate(final int id)
	{
		return henna.get(id);
	}
	
}
