package adf.impl.centralized;

import adf.core.agent.communication.standard.bundle.centralized.CommandFire;
import adf.core.agent.communication.standard.bundle.centralized.CommandScout;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.component.centralized.CommandPicker;
import adf.core.component.communication.CommunicationMessage;
import rescuecore2.standard.entities.Area;
import rescuecore2.standard.entities.Human;
import rescuecore2.standard.entities.StandardEntity;
import rescuecore2.standard.entities.StandardEntityURN;
import rescuecore2.worldmodel.EntityID;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

public class DefaultCommandPickerFire extends CommandPicker {

    private int scoutDistance;//消防员可以侦测的距离

    private Collection<CommunicationMessage> messages;//消防员交流消息的集合
    private Map<EntityID, EntityID> allocationData;//用图存储的配置的数据

    //接受数据，并传入父类CommandPickerFire作为一个构造函数
    public DefaultCommandPickerFire(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
        super(ai, wi, si, moduleManager, developData);
    this.messages = new ArrayList<>();//创建一个ArrayList保存信息
    this.allocationData = null;//将allocationData设置为空
    this.scoutDistance = developData.getInteger(
        "adf.impl.centralized.DefaultCommandPickerFire.scoutDistance", 40000);
  }//设置侦测距离属性的构造方法


  @Override
  public CommandPicker           //返回类型设置为其父类类型
      setAllocatorResult(Map<EntityID, EntityID> allocationData) {     //可能是从DefaultTacticsAmbulanceCentre类的think方法中传进数据来初始化配置数据
    this.allocationData = allocationData;//初始化数据中
    return this;//将初始化过数据后的对象返回
  }


  @Override
  public CommandPicker calc() {
    this.messages.clear();                 //将对象原有的交流消息集合清空
    if (this.allocationData == null) {     //如果数据为空，则直接返回
      return this;
    }
    for (EntityID agentID : this.allocationData.keySet()) {    //对allocationData的key进行循环
      StandardEntity agent = this.worldInfo.getEntity(agentID);//通过ID获得智能体所有信息并返回
      if (agent != null                                        //如果返回的智能体不会空且类型为消防员
          && agent.getStandardURN() == StandardEntityURN.FIRE_BRIGADE) {
        StandardEntity target = this.worldInfo
            .getEntity(this.allocationData.get(agentID));//通过从配置数据中获得的智能体ID获取另一个智能体的相关信息
        if (target != null) {                         //如果智能体2号不为空
          if (target instanceof Human) {             //如果智能体2号为人
            CommandFire command = new CommandFire(true, agentID, target.getID(),
                CommandFire.ACTION_AUTONOMY);//创建一个用于通知消防员搜救的信息
            this.messages.add(command);//将上面创建的救援信息添加进该对象的信息列表中
          } else if (target instanceof Area) {
            CommandScout command = new CommandScout(true, agentID,
                target.getID(), this.scoutDistance);
            this.messages.add(command);
          }
        }
      }
    }
    return this;
  }


  @Override
  public Collection<CommunicationMessage> getResult() {
    return this.messages;
  }
}