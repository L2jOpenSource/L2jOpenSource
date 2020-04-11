/*
 * Copyright (C) 2004-2017 L2J Server
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
package l2r.gameserver.dao.factory;

import l2r.gameserver.dao.ClanDAO;
import l2r.gameserver.dao.HennaDAO;
import l2r.gameserver.dao.ItemDAO;
import l2r.gameserver.dao.ItemReuseDAO;
import l2r.gameserver.dao.NoRestartZoneDAO;
import l2r.gameserver.dao.PremiumItemDAO;
import l2r.gameserver.dao.RecipeBookDAO;
import l2r.gameserver.dao.RecipeShopListDAO;
import l2r.gameserver.dao.ShortcutDAO;
import l2r.gameserver.dao.SkillDAO;
import l2r.gameserver.dao.SubclassDAO;
import l2r.gameserver.dao.TeleportBookmarkDAO;

/**
 * DAO Factory interface.
 * @author vGodFather
 */
public interface IDAOFactory
{
	// FriendDAO getFriendDAO();
	
	HennaDAO getHennaDAO();
	
	ItemDAO getItemDAO();
	
	ItemReuseDAO getItemReuseDAO();
	
	NoRestartZoneDAO getNoRestartZoneDAO();
	
	// PetDAO getPetDAO();
	
	// PetSkillSaveDAO getPetSkillSaveDAO();
	
	// PlayerDAO getPlayerDAO();
	
	// PlayerSkillSaveDAO getPlayerSkillSaveDAO();
	
	PremiumItemDAO getPremiumItemDAO();
	
	RecipeBookDAO getRecipeBookDAO();
	
	RecipeShopListDAO getRecipeShopListDAO();
	
	// RecommendationBonusDAO getRecommendationBonusDAO();
	
	// ServitorSkillSaveDAO getServitorSkillSaveDAO();
	
	ShortcutDAO getShortcutDAO();
	
	SkillDAO getSkillDAO();
	
	SubclassDAO getSubclassDAO();
	
	TeleportBookmarkDAO getTeleportBookmarkDAO();
	
	ClanDAO getClanDAO();
}
