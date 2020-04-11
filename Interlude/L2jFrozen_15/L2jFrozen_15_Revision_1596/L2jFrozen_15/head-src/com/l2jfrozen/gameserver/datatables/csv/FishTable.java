package com.l2jfrozen.gameserver.datatables.csv;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.FishData;

/**
 * @author -Nemesiss-
 */
public class FishTable
{
	private static final Logger LOGGER = Logger.getLogger(FishTable.class);
	private static FishTable instance;
	
	private List<FishData> fishsNormal = new ArrayList<>();
	private List<FishData> fishsEasy = new ArrayList<>();
	private List<FishData> fishsHard = new ArrayList<>();
	
	public static FishTable getInstance()
	{
		if (instance == null)
		{
			return new FishTable();
		}
		return instance;
	}
	
	private FishTable()
	{
	}
	
	public void loadData()
	{
		int count = 0;
		
		File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/fish.csv");
		
		try (FileReader reader = new FileReader(fileData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff);)
		{
			String line = null;
			
			// format:
			// id;level;name;hp;hpregen;fish_type;fish_group;fish_guts;guts_check_time;wait_time;combat_time
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				StringTokenizer st = new StringTokenizer(line, ";");
				
				int id = Integer.parseInt(st.nextToken());
				int lvl = Integer.parseInt(st.nextToken());
				String name = st.nextToken();
				int hp = Integer.parseInt(st.nextToken());
				int hpreg = Integer.parseInt(st.nextToken());
				int type = Integer.parseInt(st.nextToken());
				int group = Integer.parseInt(st.nextToken());
				int fish_guts = Integer.parseInt(st.nextToken());
				int guts_check_time = Integer.parseInt(st.nextToken());
				int wait_time = Integer.parseInt(st.nextToken());
				int combat_time = Integer.parseInt(st.nextToken());
				
				FishData fish = new FishData(id, lvl, name, hp, hpreg, type, group, fish_guts, guts_check_time, wait_time, combat_time);
				
				switch (fish.getGroup())
				{
					case 0:
						fishsEasy.add(fish);
						break;
					case 1:
						fishsNormal.add(fish);
						break;
					case 2:
						fishsHard.add(fish);
				}
			}
			
			count = fishsEasy.size() + fishsNormal.size() + fishsHard.size();
		}
		catch (FileNotFoundException e)
		{
			LOGGER.error("FishTable.loadData: fish.csv is missing in data folder", e);
			return;
		}
		catch (IOException e)
		{
			LOGGER.error("FishTable.loadData: Error while creating table.", e);
			return;
		}
		
		LOGGER.info("FishTable: Loaded " + count + " Fishes.");
	}
	
	/**
	 * @param  lvl
	 * @param  type
	 * @param  group
	 * @return       List of Fish that can be fished
	 */
	public List<FishData> getfish(int lvl, int type, int group)
	{
		List<FishData> result = new ArrayList<>();
		List<FishData> fishList = null;
		
		switch (group)
		{
			case 0:
				fishList = fishsEasy;
				break;
			case 1:
				fishList = fishsNormal;
				break;
			case 2:
				fishList = fishsHard;
				break;
		}
		
		if (fishList == null)
		{
			// the fish list is empty
			LOGGER.warn("Fish are not defined !");
			return null;
		}
		
		for (FishData f : fishList)
		{
			if (f.getLevel() != lvl)
			{
				continue;
			}
			
			if (f.getType() != type)
			{
				continue;
			}
			
			result.add(f);
		}
		
		if (result.size() == 0)
		{
			LOGGER.warn("Cant Find Any Fish!? - Lvl: " + lvl + " Type: " + type);
		}
		
		fishList = null;
		
		return result;
	}
	
}
