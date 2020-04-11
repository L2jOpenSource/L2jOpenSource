/*
 * Copyright (C) 2004-2019 L2J Server
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
package com.l2jserver.tools.util.app;

/**
 * @author HorridoJoho
 */
public interface IApplicationFrontend
{
	void reportInfo(boolean drawAttention, String message);
	
	void reportInfo(boolean drawAttention, String message, Object... args);
	
	void reportWarn(boolean drawAttention, String message);
	
	void reportWarn(boolean drawAttention, String message, Object... args);
	
	void reportError(boolean drawAttention, String message);
	
	void reportError(boolean drawAttention, Throwable t, String message);
	
	void reportError(boolean drawAttention, Throwable t, String message, Object... args);
	
	String requestUserInput(String message, Object... args);
	
	boolean requestUserConfirm(String message, Object... args);
	
	void close();
}