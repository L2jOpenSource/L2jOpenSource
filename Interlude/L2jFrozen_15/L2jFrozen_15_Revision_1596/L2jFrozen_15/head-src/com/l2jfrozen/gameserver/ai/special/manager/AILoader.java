package com.l2jfrozen.gameserver.ai.special.manager;

import com.l2jfrozen.gameserver.ai.special.Antharas;
import com.l2jfrozen.gameserver.ai.special.Baium;
import com.l2jfrozen.gameserver.ai.special.Core;
import com.l2jfrozen.gameserver.ai.special.FairyTrees;
import com.l2jfrozen.gameserver.ai.special.Frintezza;
import com.l2jfrozen.gameserver.ai.special.Gordon;
import com.l2jfrozen.gameserver.ai.special.IceFairySirra;
import com.l2jfrozen.gameserver.ai.special.Monastery;
import com.l2jfrozen.gameserver.ai.special.Orfen;
import com.l2jfrozen.gameserver.ai.special.QueenAnt;
import com.l2jfrozen.gameserver.ai.special.SummonMinions;
import com.l2jfrozen.gameserver.ai.special.Transform;
import com.l2jfrozen.gameserver.ai.special.Valakas;
import com.l2jfrozen.gameserver.ai.special.VanHalter;
import com.l2jfrozen.gameserver.ai.special.VarkaKetraAlly;
import com.l2jfrozen.gameserver.ai.special.Zaken;
import com.l2jfrozen.gameserver.ai.special.ZombieGatekeepers;
import com.l2jfrozen.gameserver.thread.ThreadPoolManager;

/**
 * @author qwerty
 */

public class AILoader
{
	public static void init()
	{
		ThreadPoolManager.getInstance().scheduleAi(new Antharas(-1, "antharas", "ai"), 100);
		
		ThreadPoolManager.getInstance().scheduleAi(new Baium(-1, "baium", "ai"), 200);
		
		ThreadPoolManager.getInstance().scheduleAi(new Core(-1, "core", "ai"), 300);
		
		ThreadPoolManager.getInstance().scheduleAi(new QueenAnt(-1, "queen_ant", "ai"), 400);
		
		ThreadPoolManager.getInstance().scheduleAi(new VanHalter(-1, "vanhalter", "ai"), 500);
		
		ThreadPoolManager.getInstance().scheduleAi(new Gordon(-1, "Gordon", "ai"), 600);
		
		ThreadPoolManager.getInstance().scheduleAi(new Monastery(-1, "monastery", "ai"), 700);
		
		ThreadPoolManager.getInstance().scheduleAi(new Transform(-1, "transform", "ai"), 800);
		
		ThreadPoolManager.getInstance().scheduleAi(new FairyTrees(-1, "FairyTrees", "ai"), 900);
		
		ThreadPoolManager.getInstance().scheduleAi(new SummonMinions(-1, "SummonMinions", "ai"), 1000);
		
		ThreadPoolManager.getInstance().scheduleAi(new ZombieGatekeepers(-1, "ZombieGatekeepers", "ai"), 1100);
		
		ThreadPoolManager.getInstance().scheduleAi(new IceFairySirra(-1, "IceFairySirra", "ai"), 1200);
		
		ThreadPoolManager.getInstance().scheduleAi(new VarkaKetraAlly(-1, "Varka Ketra Ally", "ai"), 1600);
		
		ThreadPoolManager.getInstance().scheduleAi(new Orfen(-1, "Orfen", "ai"), 1800);
		
		ThreadPoolManager.getInstance().scheduleAi(new Zaken(-1, "Zaken", "ai"), 1900);
		
		ThreadPoolManager.getInstance().scheduleAi(new Frintezza(-1, "Frintezza", "ai"), 2000);
		
		ThreadPoolManager.getInstance().scheduleAi(new Valakas(-1, "valakas", "ai"), 2100);
	}
}
