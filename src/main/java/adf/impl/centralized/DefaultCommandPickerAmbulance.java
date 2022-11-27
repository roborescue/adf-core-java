package adf.impl.centralized;

import adf.core.agent.communication.standard.bundle.centralized.CommandAmbulance;
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

/**
 * 救护队的命令选择器
 * <p>
 * 调用流程:{@link #setAllocatorResult(Map)} -> {@link #calc()} ->  {@link #getResult()}
 *
 * @author <a href="https://downsxu.github.io/">DownsXu</a>
 */
public class DefaultCommandPickerAmbulance extends CommandPicker {

    /**
     * 侦察距离
     */
    private int scoutDistance;

    /**
     *分配完毕的命令的集合
     */
    private Collection<CommunicationMessage> messages;

    /**
     *想要分配的数据
     */
    private Map<EntityID, EntityID> allocationData;

    /**
     * {@link DefaultCommandPickerAmbulance}的构造函数
     *
     * @param ai     代理信息
     * @param wi     世界信息
     * @param si  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://downsxu.github.io/">DownsXu</a>
     */
    public DefaultCommandPickerAmbulance(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
        super(ai, wi, si, moduleManager, developData);
    this.messages = new ArrayList<>();
    this.allocationData = null;
    this.scoutDistance = developData.getInteger(
        "adf.impl.centralized.DefaultCommandPickerAmbulance.scoutDistance",
        40000);
  }


    /**
     *为命令选择器设置想要分配的数据
     *
     * @param allocationData 想要分配的数据
     * @return this
     * @author <a href="https://downsxu.github.io/">DownsXu</a>
     */
  @Override
  public CommandPicker
      setAllocatorResult(Map<EntityID, EntityID> allocationData) {
    this.allocationData = allocationData;
    return this;
  }


    /**
     * 计算数据,分配好命令,并添加到{@link #messages}中
     * <p>
     * 根据每条数据agent的目标类型,分配不同的命令:
     * <ul>
     *     <li>如果目标是人类({@link Human}),则让agent自主行动
     *     <li>如果目标是区域({@link Area}),则让agent去侦查
     * </ul>
     *
     * @return this
     * @author <a href="https://downsxu.github.io/">DownsXu</a>
     */
  @Override
  public CommandPicker calc() {
    this.messages.clear();
    if (this.allocationData == null) {
      return this;
    }
    for (EntityID agentID : this.allocationData.keySet()) {
      StandardEntity agent = this.worldInfo.getEntity(agentID);
      if (agent != null
          && agent.getStandardURN() == StandardEntityURN.AMBULANCE_TEAM) {
        StandardEntity target = this.worldInfo
            .getEntity(this.allocationData.get(agentID));
        if (target != null) {
          if (target instanceof Human) {
            CommandAmbulance command = new CommandAmbulance(true, agentID,
                target.getID(), CommandAmbulance.ACTION_AUTONOMY);
            this.messages.add(command);
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


    /**
     *获得分配的结果
     *
     * @return 分配完毕的命令的集合
     * @author <a href="https://downsxu.github.io/">DownsXu</a>
     */
  @Override
  public Collection<CommunicationMessage> getResult() {
    return this.messages;
  }
}