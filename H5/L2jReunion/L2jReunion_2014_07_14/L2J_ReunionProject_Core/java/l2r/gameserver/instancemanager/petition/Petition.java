/*
 * Copyright (C) 2004-2014 L2J Server
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
package l2r.gameserver.instancemanager.petition;

import java.util.List;

import javolution.util.FastList;
import l2r.gameserver.idfactory.IdFactory;
import l2r.gameserver.model.actor.instance.L2PcInstance;
import l2r.gameserver.network.SystemMessageId;
import l2r.gameserver.network.serverpackets.CreatureSay;
import l2r.gameserver.network.serverpackets.L2GameServerPacket;
import l2r.gameserver.network.serverpackets.PetitionVotePacket;
import l2r.gameserver.network.serverpackets.SystemMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Petition
{
	protected static final Logger _log = LoggerFactory.getLogger(Petition.class);
	
	private final long _submitTime = System.currentTimeMillis();
	
	private final int _id;
	private final PetitionType _type;
	private PetitionState _state = PetitionState.PENDING;
	private final String _content;
	
	private final List<CreatureSay> _messageLog = new FastList<>();
	
	private final L2PcInstance _petitioner;
	private L2PcInstance _responder;
	
	public Petition(L2PcInstance petitioner, String petitionText, int petitionType)
	{
		petitionType--;
		_id = IdFactory.getInstance().getNextId();
		if (petitionType >= PetitionType.values().length)
		{
			_log.warn(getClass().getSimpleName() + ": Petition : invalid petition type (received type was +1) : " + petitionType);
		}
		_type = PetitionType.values()[petitionType];
		_content = petitionText;
		
		_petitioner = petitioner;
	}
	
	public boolean addLogMessage(CreatureSay cs)
	{
		return _messageLog.add(cs);
	}
	
	public List<CreatureSay> getLogMessages()
	{
		return _messageLog;
	}
	
	public boolean endPetitionConsultation(PetitionState endState)
	{
		setState(endState);
		
		if ((getResponder() != null) && getResponder().isOnline())
		{
			if (endState == PetitionState.RESPONDER_REJECT)
			{
				getPetitioner().sendMessage("Your petition was rejected. Please try again later.");
			}
			else
			{
				// Ending petition consultation with <Player>.
				SystemMessage sm = SystemMessage.getSystemMessage(SystemMessageId.PETITION_ENDED_WITH_C1);
				sm.addString(getPetitioner().getName());
				getResponder().sendPacket(sm);
				
				if (endState == PetitionState.PETITIONER_CANCEL)
				{
					// Receipt No. <ID> petition cancelled.
					sm = SystemMessage.getSystemMessage(SystemMessageId.RECENT_NO_S1_CANCELED);
					sm.addInt(getId());
					getResponder().sendPacket(sm);
				}
			}
		}
		
		// End petition consultation and inform them, if they are still online. And if petitioner is online, enable Evaluation button
		if ((getPetitioner() != null) && getPetitioner().isOnline())
		{
			getPetitioner().sendPacket(SystemMessageId.THIS_END_THE_PETITION_PLEASE_PROVIDE_FEEDBACK);
			getPetitioner().sendPacket(PetitionVotePacket.STATIC_PACKET);
		}
		
		PetitionManager.getInstance().getCompletedPetitions().put(getId(), this);
		return (PetitionManager.getInstance().getPendingPetitions().remove(getId()) != null);
	}
	
	public String getContent()
	{
		return _content;
	}
	
	public int getId()
	{
		return _id;
	}
	
	public L2PcInstance getPetitioner()
	{
		return _petitioner;
	}
	
	public L2PcInstance getResponder()
	{
		return _responder;
	}
	
	public long getSubmitTime()
	{
		return _submitTime;
	}
	
	public PetitionState getState()
	{
		return _state;
	}
	
	public String getTypeAsString()
	{
		return _type.toString().replace("_", " ");
	}
	
	public void sendPetitionerPacket(L2GameServerPacket responsePacket)
	{
		if ((getPetitioner() == null) || !getPetitioner().isOnline())
		{
			// Allows petitioners to see the results of their petition when
			// they log back into the game.
			
			// endPetitionConsultation(PetitionState.Petitioner_Missing);
			return;
		}
		
		getPetitioner().sendPacket(responsePacket);
	}
	
	public void sendResponderPacket(L2GameServerPacket responsePacket)
	{
		if ((getResponder() == null) || !getResponder().isOnline())
		{
			endPetitionConsultation(PetitionState.RESPONDER_MISSING);
			return;
		}
		
		getResponder().sendPacket(responsePacket);
	}
	
	public void setState(PetitionState state)
	{
		_state = state;
	}
	
	public void setResponder(L2PcInstance respondingAdmin)
	{
		if (getResponder() != null)
		{
			return;
		}
		
		_responder = respondingAdmin;
	}
}