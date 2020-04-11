package net.sf.l2j.gameserver.scripting.scripts.custom;

import net.sf.l2j.gameserver.model.actor.Npc;
import net.sf.l2j.gameserver.model.actor.Player;
import net.sf.l2j.gameserver.scripting.Quest;
import net.sf.l2j.gameserver.scripting.QuestState;
import net.sf.l2j.gameserver.scripting.scripts.village_master.FirstClassChange;
import net.sf.l2j.gameserver.scripting.scripts.village_master.SecondClassChange;

public class ShadowWeapon extends Quest
{
	private static final String qn = "ShadowWeapon";
	
	private static final int D_GRADE_COUPON = 8869;
	private static final int C_GRADE_COUPON = 8870;
	
	public ShadowWeapon()
	{
		super(-1, "custom");
		
		addStartNpc(FirstClassChange.FIRST_CLASS_NPCS);
		addTalkId(FirstClassChange.FIRST_CLASS_NPCS);
		
		addStartNpc(SecondClassChange.SECOND_CLASS_NPCS);
		addTalkId(SecondClassChange.SECOND_CLASS_NPCS);
	}
	
	@Override
	public String onTalk(Npc npc, Player player)
	{
		QuestState st = player.getQuestState(qn);
		String htmltext = getNoQuestMsg();
		if (st == null)
			return htmltext;
		
		boolean hasD = st.hasQuestItems(D_GRADE_COUPON);
		boolean hasC = st.hasQuestItems(C_GRADE_COUPON);
		
		if (hasD || hasC)
		{
			// let's assume character had both c & d-grade coupons, we'll confirm later
			String multisell = "306893003";
			if (!hasD) // if s/he had c-grade only...
				multisell = "306893002";
			else if (!hasC) // or d-grade only.
				multisell = "306893001";
			
			// finally, return htm with proper multisell value in it.
			htmltext = getHtmlText("exchange.htm").replace("%msid%", multisell);
		}
		else
			htmltext = "exchange-no.htm";
		
		st.exitQuest(true);
		return htmltext;
	}
}