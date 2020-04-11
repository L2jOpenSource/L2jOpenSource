/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package l2r.gameserver.scripting.scriptengine;

/**
 * Implementation of L2J's script engine.<br>
 * <br>
 * PLEASE READ, THIS IS IMPORTANT INFORMATION!<br>
 * <br>
 * Do NOT instantiate L2JListener directly! Use its children!<br>
 * <br>
 * This package contains a ton of listeners to facilitate the creation and maintenance of datapack and core scripts.<br>
 * These listeners can handle most of the player's actions, interactions, etc...<br <br>
 * The use of abstract class instead of interface is because it was necessary to code some hard-coded functions/variables to make it easier and more straight-forward to understand and use for developers with less experience.<br>
 * <br>
 * The listeners are automatically registered once they are instantiated. This means that you do not need to call the object.addListener() method, it is done automatically for you! However, you will need to use the unregister() method if you wish to unregister it.<br>
 * <br>
 * NOTE: You can use the boolean return of the listeners as a "code blocker". Which means that if your method returns "false" the method that triggered the listener will stop. (i.e.: you can stop something from happening using this)<br>
 * <br>
 * Example of use of one of these classes:
 * 
 * <pre>
 *	ChatListener listener = new ChatListener()
 *	{
 *		@Override
 *		public String filter(String text, L2PcInstance origin, ChatTargetType targetType)
 *		{
 *			String modifiedText = text
 *			if(!origin.isGM())
 *			{
 *				modifiedText = someFilterMethod(modifiedText);
 *			}
 *			return modifiedText;
 *		}
 *		
 *		@Override
 *		public boolean onTalk(String text, L2PcInstance origin, String target, ChatTargetType targetType)
 *		{
 *			// DO SOMETHING...
 *		}
 *	};
 * </pre>
 * @author TheOne
 */
