import sys
from com.l2jfrozen.gameserver.model.quest import State
from com.l2jfrozen.gameserver.model.quest import QuestState
from com.l2jfrozen.gameserver.model.quest.jython import QuestJython as JQuest
from com.l2jfrozen.util.random import Rnd

class scarlet_stokate_noble(JQuest) :

    # init function.  Add in here variables that you'd like to be inherited by subclasses (if any)
    def __init__(self,id,name,descr):
        self.scarlet_stokate_noble = 21378
        self.scarlet_stokate_noble_b = 21652
        JQuest.__init__(self,id,name,descr) 

    def onKill (self,npc,player,isPet):
        npcId = npc.getNpcId()
        if npcId == self.scarlet_stokate_noble:
            if Rnd.get(100) <= 20:
                self.addSpawn(self.scarlet_stokate_noble_b,npc.getX(), npc.getY(), npc.getZ(),npc.getHeading(),True,0)
                self.addSpawn(self.scarlet_stokate_noble_b,npc.getX(), npc.getY(), npc.getZ(),npc.getHeading(),True,0)
                self.addSpawn(self.scarlet_stokate_noble_b,npc.getX(), npc.getY(), npc.getZ(),npc.getHeading(),True,0)
                self.addSpawn(self.scarlet_stokate_noble_b,npc.getX(), npc.getY(), npc.getZ(),npc.getHeading(),True,0)
                self.addSpawn(self.scarlet_stokate_noble_b,npc.getX(), npc.getY(), npc.getZ(),npc.getHeading(),True,0)
        return 

QUEST = scarlet_stokate_noble(-1,"scarlet_stokate_noble","ai")

CREATED = State('Start', QUEST)
QUEST.setInitialState(CREATED)

QUEST.addKillId(QUEST.scarlet_stokate_noble)