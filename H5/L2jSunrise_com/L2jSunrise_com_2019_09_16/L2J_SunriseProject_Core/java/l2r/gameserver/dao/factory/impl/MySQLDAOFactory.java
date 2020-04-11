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
package l2r.gameserver.dao.factory.impl;

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
import l2r.gameserver.dao.factory.IDAOFactory;
import l2r.gameserver.dao.impl.mysql.ClanDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.HennaDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.ItemDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.ItemReuseDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.NoRestartZoneDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.PremiumItemDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.RecipeBookDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.RecipeShopListDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.ShortcutDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.SkillDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.SubclassDAOMySQLImpl;
import l2r.gameserver.dao.impl.mysql.TeleportBookmarkDAOMySQLImpl;

/**
 * MySQL DAO Factory implementation.
 * @author vGodFather
 */
enum MySQLDAOFactory implements IDAOFactory
{
	INSTANCE;
	
	// private final FriendDAO friendDAO = new FriendDAOMySQLImpl();
	private final HennaDAO hennaDAO = new HennaDAOMySQLImpl();
	private final ItemDAO itemDAO = new ItemDAOMySQLImpl();
	private final ItemReuseDAO itemReuseDAO = new ItemReuseDAOMySQLImpl();
	private final NoRestartZoneDAO noRestartZoneDAO = new NoRestartZoneDAOMySQLImpl();
	// private final PetDAO petDAO = new PetDAOMySQLImpl();
	// private final PetSkillSaveDAO petSkillSaveDAO = new PetSkillSaveDAOMySQL();
	// private final PlayerDAO playerDAO = new PlayerDAOMySQLImpl();
	// private final PlayerSkillSaveDAO playerSkillSaveDAO = new PlayerSkillSaveDAOMySQLImpl();
	private final PremiumItemDAO premiumItemDAO = new PremiumItemDAOMySQLImpl();
	private final RecipeBookDAO recipeBookDAO = new RecipeBookDAOMySQLImpl();
	private final RecipeShopListDAO recipeShopListDAO = new RecipeShopListDAOMySQLImpl();
	// private final RecommendationBonusDAO recommendationBonusDAO = new RecommendationBonusDAOMySQLImpl();
	// private final ServitorSkillSaveDAO servitorSkillSaveDAO = new ServitorSkillSaveDAOMySQLImpl();
	private final ShortcutDAO shortcutDAO = new ShortcutDAOMySQLImpl();
	private final SkillDAO skillDAO = new SkillDAOMySQLImpl();
	private final SubclassDAO subclassDAO = new SubclassDAOMySQLImpl();
	private final TeleportBookmarkDAO teleportBookmarkDAO = new TeleportBookmarkDAOMySQLImpl();
	private final ClanDAO clanDAO = new ClanDAOMySQLImpl();
	
	// @Override
	// public FriendDAO getFriendDAO()
	// {
	// return friendDAO;
	// }
	//
	@Override
	public HennaDAO getHennaDAO()
	{
		return hennaDAO;
	}
	
	@Override
	public ItemDAO getItemDAO()
	{
		return itemDAO;
	}
	
	@Override
	public ItemReuseDAO getItemReuseDAO()
	{
		return itemReuseDAO;
	}
	
	@Override
	public NoRestartZoneDAO getNoRestartZoneDAO()
	{
		return noRestartZoneDAO;
	}
	
	// @Override
	// public PetDAO getPetDAO()
	// {
	// return petDAO;
	// }
	//
	// @Override
	// public PetSkillSaveDAO getPetSkillSaveDAO()
	// {
	// return petSkillSaveDAO;
	// }
	//
	// @Override
	// public PlayerDAO getPlayerDAO()
	// {
	// return playerDAO;
	// }
	//
	// @Override
	// public PlayerSkillSaveDAO getPlayerSkillSaveDAO()
	// {
	// return playerSkillSaveDAO;
	// }
	
	@Override
	public PremiumItemDAO getPremiumItemDAO()
	{
		return premiumItemDAO;
	}
	
	@Override
	public RecipeBookDAO getRecipeBookDAO()
	{
		return recipeBookDAO;
	}
	
	@Override
	public RecipeShopListDAO getRecipeShopListDAO()
	{
		return recipeShopListDAO;
	}
	
	// @Override
	// public RecommendationBonusDAO getRecommendationBonusDAO()
	// {
	// return recommendationBonusDAO;
	// }
	//
	// @Override
	// public ServitorSkillSaveDAO getServitorSkillSaveDAO()
	// {
	// return servitorSkillSaveDAO;
	// }
	
	@Override
	public ShortcutDAO getShortcutDAO()
	{
		return shortcutDAO;
	}
	
	@Override
	public SkillDAO getSkillDAO()
	{
		return skillDAO;
	}
	
	@Override
	public SubclassDAO getSubclassDAO()
	{
		return subclassDAO;
	}
	
	@Override
	public TeleportBookmarkDAO getTeleportBookmarkDAO()
	{
		return teleportBookmarkDAO;
	}
	
	@Override
	public ClanDAO getClanDAO()
	{
		return clanDAO;
	}
}
