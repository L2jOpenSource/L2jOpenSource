/*
 * Copyright (C) 2004-2019 L2J DataPack
 * 
 * This file is part of L2J DataPack.
 * 
 * L2J DataPack is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * L2J DataPack is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package custom.events.Preguntas;

import java.util.ArrayList;

import com.l2jserver.gameserver.model.actor.L2Npc;
import com.l2jserver.gameserver.model.actor.instance.L2PcInstance;
import com.l2jserver.gameserver.model.events.EventType;
import com.l2jserver.gameserver.model.events.ListenerRegisterType;
import com.l2jserver.gameserver.model.events.annotations.RegisterEvent;
import com.l2jserver.gameserver.model.events.annotations.RegisterType;
import com.l2jserver.gameserver.model.events.impl.character.player.OnPlayerChat;
import com.l2jserver.gameserver.util.Broadcast;
import com.l2jserver.util.Rnd;

import ai.npc.AbstractNpcAI;

/**
 * @author Eze
 */
public class Preguntas extends AbstractNpcAI
{
	private boolean started = false;
	// indicar el tiempo de ciclo del evento en minutos
	private final int ciclo = 30;
	// indica el numero de preguntas
	private final int random = 1;
	private int num;
	private final ArrayList<L2PcInstance> winners = new ArrayList<>();
	// indicar las preguntas
	private final String[] question =
	{
		"What is the wizard class that has the empowering echo?",
		"Who is the lord of the Seed of Infinity?",
		"What was the name of C3?",
		"In which village can you find Esmeralda?",
		"What kind of monster is gremlin?"
	};
	private final String[] answers =
	{
		"spellhowler",
		"ekimus",
		"rise of darkness",
		"hunters village",
		"fairy"
	};
	// indicar la id de los items y la cantidad de reward
	private final int[][] items =
	{
		{
			6392,
			1
		},
		{
			6393,
			1
		}
	};
	
	private Preguntas(int i, String string, String string2)
	{
		super(Preguntas.class.getSimpleName(), "custom/events/Preguntas");
		
		startQuestTimer("pregunta", 1 * 60000, null, null);
	}
	
	@Override
	public String onAdvEvent(String event, L2Npc npc, L2PcInstance player)
	{
		if (event.equals("question"))
		{
			winners.clear();
			num = Rnd.get(random);
			Broadcast.toAllOnlinePlayers("Question Event. The one who answers the correct answer in small letter by Shout will be given a reward :", false);
			Broadcast.toAllOnlinePlayers(question[num], true);
			started = true;
		}
		else if (event.equals("Finalized"))
		{
			Broadcast.toAllOnlinePlayers("The winner is " + winners.get(0).getName() + ". Next event in 1 Hour", false);
			for (int[] itemId : items)
			{
				winners.get(0).addItem("winner", itemId[0], itemId[1], null, true);
			}
			startQuestTimer("question", ciclo * 60000, null, null);
		}
		
		return super.onAdvEvent(event, npc, player);
	}
	
	@RegisterEvent(EventType.ON_PLAYER_CHAT)
	@RegisterType(ListenerRegisterType.GLOBAL)
	public void Chat(OnPlayerChat event)
	{
		
		if ((!started) || (started && (event.getChatType() != 1)))
		{
			return;
		}
		if (!event.getText().equals(answers[num]))
		{
			return;
		}
		winners.add(event.getActiveChar());
		started = false;
		startQuestTimer("Finalized", 1 * 1000, null, null);
	}
	
	public static void main(String[] args)
	{
		new Preguntas(-1, "Preguntas", "custom/events/Preguntas");
		_log.info("Preguntas Event: Enabled.");
	}
}