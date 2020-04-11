package com.l2jfrozen.gameserver.network.clientpackets;

import org.apache.log4j.Logger;

import com.l2jfrozen.Config;
import com.l2jfrozen.gameserver.model.actor.instance.L2PcInstance;
import com.l2jfrozen.gameserver.network.SystemMessageId;

/**
 * @author Dezmond_snz - Packet Format: cddd
 */
public final class DlgAnswer extends L2GameClientPacket
{
	private static Logger LOGGER = Logger.getLogger(DlgAnswer.class);
	private int messageId, answer, requestId;
	
	@Override
	protected void readImpl()
	{
		messageId = readD();
		answer = readD();
		requestId = readD();
	}
	
	@Override
	public void runImpl()
	{
		final L2PcInstance activeChar = getClient().getActiveChar();
		if (activeChar == null)
		{
			return;
		}
		
		if (Config.DEBUG)
		{
			LOGGER.debug(getType() + ": Answer acepted. Message ID " + messageId + ", asnwer " + answer + ", unknown field " + requestId);
		}
		
		final Long answerTime = getClient().getActiveChar().getConfirmDlgRequestTime(requestId);
		if (answer == 1 && answerTime != null && System.currentTimeMillis() > answerTime)
		{
			answer = 0;
		}
		getClient().getActiveChar().removeConfirmDlgRequestTime(requestId);
		
		if (messageId == SystemMessageId.RESSURECTION_REQUEST.getId())
		{
			activeChar.reviveAnswer(answer);
		}
		else if (messageId == SystemMessageId.S1_WISHES_TO_SUMMON_YOU_FROM_S2_DO_YOU_ACCEPT.getId())
		{
			activeChar.teleportAnswer(answer, requestId);
		}
		else if (messageId == SystemMessageId.WOULD_YOU_LIKE_TO_OPEN_THE_GATE.getId())
		{
			activeChar.gatesAnswer(answer, 1);
		}
		else if (messageId == SystemMessageId.WOULD_YOU_LIKE_TO_CLOSE_THE_GATE.getId())
		{
			activeChar.gatesAnswer(answer, 0);
		}
		else if (messageId == 614 && Config.L2JMOD_ALLOW_WEDDING)
		{
			activeChar.engageAnswer(answer);
		}
		else if (messageId == SystemMessageId.S1.getId())
		{
			if (activeChar.dialog != null)
			{
				activeChar.dialog.onDlgAnswer(activeChar);
				activeChar.dialog = null;
			}
		}
	}
	
	@Override
	public String getType()
	{
		return "[C] C5 DlgAnswer";
	}
}