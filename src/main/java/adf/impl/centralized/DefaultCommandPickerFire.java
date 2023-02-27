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

/**
 * 消防队的命令选择器
 * <p>
 * 调用流程:{@link #setAllocatorResult(Map)} -> {@link #calc()} ->  {@link #getResult()}
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultCommandPickerFire extends CommandPicker {


    /**
     * 侦察距离
     */
    private int scoutDistance;
    /**
     * 分配完毕的命令的集合
     */
    private Collection<CommunicationMessage> messages;
    /**
     * 想要分配的数据
     */
    private Map<EntityID, EntityID> allocationData;

    /**
     * {@link DefaultCommandPickerFire}的构造函数
     *
     * @param ai            agentInfo     代理信息
     * @param wi            worldInfo     世界信息
     * @param si            scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param developData   开发数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    public DefaultCommandPickerFire(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
        super(ai, wi, si, moduleManager, developData);
        this.messages = new ArrayList<>();
        this.allocationData = null;
        this.scoutDistance = developData.getInteger(
                "adf.impl.centralized.DefaultCommandPickerFire.scoutDistance", 40000);
    }


    /**
     * 为命令选择器设置想要分配的数据
     *
     * @param allocationData 想要分配的数据
     * @return this
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public CommandPicker           //返回类型设置为其父类类型
    setAllocatorResult(Map<EntityID, EntityID> allocationData) {     //可能是从DefaultTacticsAmbulanceCentre类的think方法中传进数据来初始化配置数据
        this.allocationData = allocationData;//初始化数据中
        return this;//将初始化过数据后的对象返回
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
     * @author <a href="https://roozen.top">Roozen</a>
     */
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


    /**
     * 获得分配的结果
     *
     * @return 分配完毕的命令的集合
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public Collection<CommunicationMessage> getResult() {
        return this.messages;
    }
}