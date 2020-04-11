import sys
from com.l2jfrozen import Config
from com.l2jfrozen.gameserver.ai import CtrlIntention
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.util.random import Rnd

SplendorId ={   # Npc:[NewNpc,% for chance by shot,ModeSpawn]
                # Modespawn 1=> delete and spawn the news npc
                # Modespawn 2=> just add 1 spawn the news npc
                # if Quest_Drop = 5 => 25% by shot to change mob
                21521:[21522,5,1],      # Claw of Splendor
                21524:[21525,5,1],      # Blade of Splendor
                21527:[21528,5,1],      # Anger of Splendor
                21537:[21538,5,1],      # Fang of Splendor
                21539:[21540,100,2]     # Wailing of Splendor
                }

# Main Quest Code
class splendor(JQuest):

  # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
  def __init__(self,id,name,descr):
      self.AlwaysSpawn = False
      # finally, don't forget to call the parent constructor to prepare the event triggering
      # mechanisms etc.
      JQuest.__init__(self,id,name,descr)

  def onAttack(self,npc,player,isPet,damage):
    npcId = npc.getNpcId()
    NewMob,chance,ModeSpawn = SplendorId[npcId]
    if Rnd.get(100) <= chance*Config.RATE_DROP_QUEST :
       if SplendorId.has_key(npcId) :
          if ModeSpawn == 1 :
             npc.deleteMe()
             newNpc = self.addSpawn(NewMob,npc)
             newNpc.addDamageHate(player,0,999)
             newNpc.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
          elif self.AlwaysSpawn :
             return
          elif ModeSpawn == 2 :
             self.AlwaysSpawn = True
             newNpc1 = self.addSpawn(NewMob,npc)
             newNpc1.addDamageHate(player,0,999)
             newNpc1.getAI().setIntention(CtrlIntention.AI_INTENTION_ATTACK, player)
    return

  def onKill (self,npc,player,isPet):
    npcId = npc.getNpcId()
    NewMob,chance,ModeSpawn = SplendorId[npcId]
    if SplendorId.has_key(npcId) :
       if ModeSpawn == 2 :
          self.AlwaysSpawn = False
    return

QUEST       = splendor(-1,"splendor","ai")

CREATED = State('Start', QUEST)
QUEST.setInitialState(CREATED)

for i in SplendorId.keys() :
   QUEST.addAttackId(i)

for j in SplendorId.keys() :
   QUEST.addKillId(j)