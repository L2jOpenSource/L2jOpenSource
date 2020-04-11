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
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.L2NpcWalkerNode;

/**
 * Main Table to Load Npc Walkers Routes and Chat SQL Table.<br>
 * @author Rayan RPG for L2Emu Project
 * @author ProGramMoS
 * @since  927
 */
public class NpcWalkerRoutesTable
{
	protected static final Logger LOGGER = Logger.getLogger(NpcWalkerRoutesTable.class);
	
	private static NpcWalkerRoutesTable instance;
	
	private List<L2NpcWalkerNode> routes;
	
	public static NpcWalkerRoutesTable getInstance()
	{
		if (instance == null)
		{
			instance = new NpcWalkerRoutesTable();
		}
		
		return instance;
	}
	
	public void load()
	{
		routes = new ArrayList<>();
		File fileData = new File(Config.DATAPACK_ROOT + "/data/csv/walker_routes.csv");
		
		try (FileReader reader = new FileReader(fileData);
			BufferedReader buff = new BufferedReader(reader);
			LineNumberReader lnr = new LineNumberReader(buff))
		{
			L2NpcWalkerNode route;
			String line = null;
			
			// format:
			// route_id;npc_id;move_point;chatText;move_x;move_y;move_z;delay;running
			while ((line = lnr.readLine()) != null)
			{
				// ignore comments
				if (line.trim().length() == 0 || line.startsWith("#"))
				{
					continue;
				}
				
				route = new L2NpcWalkerNode();
				StringTokenizer st = new StringTokenizer(line, ";");
				
				int route_id = Integer.parseInt(st.nextToken());
				int npc_id = Integer.parseInt(st.nextToken());
				String move_point = st.nextToken();
				String chatText = st.nextToken();
				int move_x = Integer.parseInt(st.nextToken());
				int move_y = Integer.parseInt(st.nextToken());
				int move_z = Integer.parseInt(st.nextToken());
				int delay = Integer.parseInt(st.nextToken());
				boolean running = Boolean.parseBoolean(st.nextToken());
				
				route.setRouteId(route_id);
				route.setNpcId(npc_id);
				route.setMovePoint(move_point);
				route.setChatText(chatText);
				route.setMoveX(move_x);
				route.setMoveY(move_y);
				route.setMoveZ(move_z);
				route.setDelay(delay);
				route.setRunning(running);
				
				routes.add(route);
			}
			
			LOGGER.info("WalkerRoutesTable: Loaded " + routes.size() + " Npc Walker Routes.");
			
		}
		catch (FileNotFoundException e)
		{
			LOGGER.error("NpcWalkerRoutesTable.load : walker_routes.csv file is missing in gameserver/data/csv folder.");
		}
		catch (IOException e)
		{
			LOGGER.error("NpcWalkerRoutesTable.load : Error while creating table. ", e);
		}
		
	}
	
	public List<L2NpcWalkerNode> getRouteForNpc(int id)
	{
		return routes.stream().filter(node -> node.getNpcId() == id).collect(Collectors.toList());
	}
}
