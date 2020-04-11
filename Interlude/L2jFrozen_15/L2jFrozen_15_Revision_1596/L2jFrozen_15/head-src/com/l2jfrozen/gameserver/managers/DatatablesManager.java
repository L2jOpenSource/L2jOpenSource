package com.l2jfrozen.gameserver.managers;

import com.l2jfrozen.gameserver.datatables.GmListTable;
import com.l2jfrozen.gameserver.datatables.sql.AdminCommandAccessRights;
import com.l2jfrozen.gameserver.datatables.sql.ClanTable;
import com.l2jfrozen.gameserver.datatables.sql.HelperBuffTable;
import com.l2jfrozen.gameserver.datatables.xml.AugmentationData;
import com.l2jfrozen.gameserver.datatables.xml.ExperienceData;

public class DatatablesManager
{
	public static void reloadAll()
	{
		AdminCommandAccessRights.reload();
		GmListTable.reload();
		AugmentationData.reload();
		ClanTable.reload();
		HelperBuffTable.reload();
		ExperienceData.getInstance();
	}
}
