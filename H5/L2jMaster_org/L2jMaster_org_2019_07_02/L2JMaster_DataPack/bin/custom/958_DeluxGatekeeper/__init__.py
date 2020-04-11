import sys
from java.lang import System
from java.util import Iterator
from com.l2jserver.commons.database.pool.impl import ConnectionFactory
from com.l2jserver.gameserver.model.quest import State
from com.l2jserver.gameserver.model.quest import QuestState
from com.l2jserver.gameserver.model.quest import Quest as JQuest
from com.l2jserver.gameserver.datatables import ItemTable
from com.l2jserver.gameserver.instancemanager import TownManager
from com.l2jserver.gameserver.instancemanager import SiegeManager
from com.l2jserver.gameserver.network import SystemMessageId
from com.l2jserver.gameserver.network.serverpackets import SystemMessage
from com.l2jserver.gameserver.network.serverpackets import NpcHtmlMessage

##############################
ALLOW_VIP = False            #
##############################
VIP_ACCESS_LEVEL = 1         #
##############################
ALLOW_KARMA_PLAYER=False     #
##############################
FREE_TELEPORT = False        #
##############################


npcId         = 958
QuestId       = 958
QuestName     = "DeluxGatekeeper"
QUEST_INFO    = str(QuestId)+"_"+QuestName
QuestDesc     = "custom"

def getitemname(case):
	try: val =ItemTable.getInstance().createDummyItem(case).getItemName()
	except: val = "0"
	return val

def getevent(type,text) :
	MESSAGE = "<html><head><title>Delux Gatekeeper</title></head><body><center><img src=\"L2UI_CH3.herotower_deco\" width=256 height=32><br>"
	MESSAGE += "<font color=\"LEVEL\">"+type+"</font><br>"+text+"<br>"
	return MESSAGE

class Quest (JQuest) :
	
	def __init__(self,id,name,descr): JQuest.__init__(self,id,name,descr)

	def onAdvEvent (self,event,npc,player) :
		try: st = player.getQuestState(QUEST_INFO)
		except: return
		if event[0] == "g" and event[4] == "t" and event[11] == "t":
			event = event.replace("]","")
			event = event.replace("["," ")
			varSplit = event.split(" ")
			try: noblesGK = int(varSplit[2])
			except : noblesGK = 0
			if st.player.isGM() == 1 :
				try:
					st.player.teleToLocation(int(varSplit[5]), int(varSplit[6]), int(varSplit[7]), True)
					#st.player.sendMessage("Has sido teleportado a las coordenadas " + varSplit[5] +" "+varSplit[6]+ " "+varSplit[7])
				except : st.player.sendMessage("Tell an administrator that this coordinate does not work!")
				return

			if noblesGK == 1 and st.player.isNoble() == 0 :
				return getevent("Sorry, only a Noble can be teleported to this location!")

			if noblesGK > 1 and st.player.isGM() == 0 :
				return getevent("Sorry, only a GM can be teleported to this location!")

			else:
				newevent="confteleport["+varSplit[3]+"]["+varSplit[4]+"] "+varSplit[5]+" "+varSplit[6]+" "+varSplit[7]
				filename = "data/html/teleporter/gatekeeper/confirmation.htm"
				html = NpcHtmlMessage(npc.getObjectId())	
				html.setFile(None,filename)
				html.replace("%place%", varSplit[1].replace("-"," "))
				html.replace("%itemName%", str(getitemname(int(varSplit[3]))))
				html.replace("%reqitem%", varSplit[4])
				html.replace("%event%", newevent)
				html.replace("%objectId%", str(npc.getObjectId()))
				st.player.sendPacket(html)
			return

		if event[0] == "c" and event[1] == "o" and event[4] == "t" and event[5] == "e":
			eventSplit = event.split(" ")
			event = eventSplit[0]
			eventParam1 = eventSplit[1]
			eventParam2 = eventSplit[2]
			eventParam3 = eventSplit[3]
			event = event.replace("]","")
			event = event.replace("["," ")
			GKSplit = event.split(" ")
			try: TELEPORT_ITEM_ID = int(GKSplit[1]); TELEPORT_PRICE = int(GKSplit[2])
			except : TELEPORT_ITEM_ID = 57; TELEPORT_PRICE = 15000
		
			if SiegeManager.getInstance().getSiege(int(eventParam1), int(eventParam2), int(eventParam3)) != None:
				st.player.sendPacket(SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE))
				return
			elif TownManager.townHasCastleInSiege(int(eventParam1), int(eventParam2)) :
				st.player.sendPacket(SystemMessage(SystemMessageId.NO_PORT_THAT_IS_IN_SIGE))
				return
			elif ALLOW_KARMA_PLAYER == False and st.player.getKarma() > 0:
				st.player.sendMessage("Out!, You are not welcome here.")
				return
			elif st.player.isAlikeDead():
				return
			if FREE_TELEPORT == False :
				if st.getQuestItemsCount(TELEPORT_ITEM_ID) < TELEPORT_PRICE :
					return getevent("Sorry","You don't have enough items:<br>You will need: <font color =\"LEVEL\">"+str(TELEPORT_PRICE)+" "+str(getitemname(TELEPORT_ITEM_ID))+"!")
				else :
					st.takeItems(TELEPORT_ITEM_ID,TELEPORT_PRICE)
					st.player.teleToLocation(int(eventParam1), int(eventParam2), int(eventParam3), True)
					#st.player.sendMessage("Has sido teleportado a las coordenadas " + eventParam1 +" "+eventParam2+ " "+eventParam3)
			else: 
				st.player.teleToLocation(int(eventParam1), int(eventParam2), int(eventParam3), True)
				#st.player.sendMessage("Has sido teleportado a las coordenadas " + eventParam1 +" "+eventParam2+ " "+eventParam3)	

		else: 	return

	def onFirstTalk (self,npc,player):
		st = player.getQuestState(QUEST_INFO)
		if not st : st = self.newQuestState(player)
		st.setState(State.STARTED)
		if player.isGM(): 
			filename = "data/html/teleporter/gatekeeper/teleports.htm"
			html = NpcHtmlMessage(npc.getObjectId())	
			html.setFile(None,filename)
			html.replace("%objectId%", str(npc.getObjectId()))
			st.player.sendPacket(html)  	
		
		if ALLOW_VIP == False or player.getAccessLevel().getLevel() == VIP_ACCESS_LEVEL and ALLOW_VIP == True:
            #verifica player en modo de combate/flag/karma.
			if ALLOW_KARMA_PLAYER == False and player.getKarma() > 0 : #Player con Karma
				htmltext = "<html><head><body> </body></html>"
			elif st.player.isInCombat() : #Player modo de Combate
				htmltext = "<html><head><body> </body></html>"
			elif st.player.getPvpFlag() > 0 : #Player Flag
					htmltext = "<html><head><body> </body></html>"
			else: 
				filename = "data/html/teleporter/gatekeeper/teleports.htm"
				html = NpcHtmlMessage(npc.getObjectId())	
				html.setFile(None,filename)
				html.replace("%objectId%", str(npc.getObjectId()))
				st.player.sendPacket(html)
				
		else: return getevent("Sorry, this NPC is only for Premium!<br>Contact a GM<br> for more information!")


QUEST = Quest(QuestId,QUEST_INFO,QuestDesc)

QUEST.addStartNpc(npcId)
QUEST.addFirstTalkId(npcId)
QUEST.addTalkId(npcId)