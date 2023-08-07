package adf.impl.tactics;

import adf.core.agent.communication.MessageManager;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandPicker;
import adf.core.component.communication.CommunicationMessage;
import adf.core.component.module.complex.TargetAllocator;
import adf.core.component.tactics.TacticsFireStation;
import adf.core.debug.WorldViewLauncher;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

/**
 * 消防局的默认策略
 * <p>
 * 策略主要包括目标分配和指令生成两个组件
 * <p>
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsFireStation extends TacticsFireStation {

    private TargetAllocator allocator; // 目标分配组件
    private CommandPicker picker; // 指令生成组件
    private Boolean isVisualDebug; // 是否进行可视化调试

    /**
     * 初始化策略
     *
     * @param agentInfo      智能体信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   场景信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param debugData      调试数据
     */
    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           MessageManager messageManager, DevelopData debugData) {
        // 设置消息订阅者和消息协调器
        messageManager.setChannelSubscriber(moduleManager.getChannelSubscriber(
                "MessageManager.CenterChannelSubscriber",
                "adf.impl.module.comm.DefaultChannelSubscriber"));
        messageManager.setMessageCoordinator(moduleManager.getMessageCoordinator(
                "MessageManager.CenterMessageCoordinator",
                "adf.impl.module.comm.DefaultMessageCoordinator"));

        // 根据场景模式选择目标分配组件和指令生成组件
        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                this.allocator = moduleManager.getModule(
                        "TacticsFireStation.TargetAllocator",
                        "adf.impl.module.complex.DefaultFireTargetAllocator");
                this.picker = moduleManager.getCommandPicker(
                        "DefaultTacticsFireStation.CommandPicker",
                        "adf.impl.centralized.DefaultCommandPickerFire");
                break;
        }
        // 注册目标分配组件和指令生成组件
        registerModule(this.allocator);
        registerModule(this.picker);

        // 判断是否进行可视化调试
        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));
    }


    /**
     * 策略思考
     *
     * @param agentInfo      智能体信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   场景信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param debugData      调试数据
     */
    @Override
    public void think(AgentInfo agentInfo, WorldInfo worldInfo,
                      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                      MessageManager messageManager, DevelopData debugData) {
        // 更新模块信息
        modulesUpdateInfo(messageManager);

        // 如果进行可视化调试，则显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }

        // 进行目标分配
        Map<EntityID, EntityID> allocatorResult = this.allocator.calc().getResult();
        // 根据目标分配结果进行指令生成
        for (CommunicationMessage message : this.picker
                .setAllocatorResult(allocatorResult).calc().getResult()) {
            messageManager.addMessage(message);
        }
    }


    /**
     * 策略恢复
     *
     * @param agentInfo      智能体信息
     * @param worldInfo      世界信息
     * @param scenarioInfo   场景信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param debugData      调试数据
     */
    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData debugData) {
        // 模块恢复
        modulesResume(precomputeData);

        // 如果进行可视化调试，则显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }


    /**
     * 策略准备
     *
     * @param agentInfo     智能体信息
     * @param worldInfo     世界信息
     * @param scenarioInfo  场景信息
     * @param moduleManager 模块管理器
     * @param debugData     调试数据
     */
    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData debugData) {
        // 模块准备
        modulesPreparate();

        // 如果进行可视化调试，则显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }
}
