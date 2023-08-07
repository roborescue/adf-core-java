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
import adf.core.component.tactics.TacticsPoliceOffice;
import adf.core.debug.WorldViewLauncher;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

/**
 * 警察局的默认策略
 * <p>
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsPoliceOffice extends TacticsPoliceOffice {

    private TargetAllocator allocator; // 目标分配器
    private CommandPicker picker; // 命令选择器
    private Boolean isVisualDebug; // 是否启用可视化调试模式

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

        switch (scenarioInfo.getMode()) {
            case PRECOMPUTATION_PHASE:
            case PRECOMPUTED:
            case NON_PRECOMPUTE:
                // 根据不同的模式，获取目标分配器和命令选择器
                this.allocator = moduleManager.getModule(
                        "DefaultTacticsPoliceOffice.TargetAllocator",
                        "adf.impl.module.complex.DefaultPoliceTargetAllocator");
                this.picker = moduleManager.getCommandPicker(
                        "DefaultTacticsPoliceOffice.CommandPicker",
                        "adf.impl.centralized.DefaultCommandPickerPolice");
                break;
        }
        registerModule(this.allocator); // 注册目标分配器模块
        registerModule(this.picker); // 注册命令选择器模块

        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));
    }


    @Override
    public void think(AgentInfo agentInfo, WorldInfo worldInfo,
                      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                      MessageManager messageManager, DevelopData debugData) {
        modulesUpdateInfo(messageManager); // 更新模块信息

        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo); // 在可视化窗口中显示当前时间步信息
        }
        Map<EntityID, EntityID> allocatorResult = this.allocator.calc().getResult(); // 计算目标分配器结果
        for (CommunicationMessage message : this.picker
                .setAllocatorResult(allocatorResult).calc().getResult()) {
            messageManager.addMessage(message); // 将命令选择器的结果添加到消息列表中
        }
    }


    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData debugData) {
        modulesResume(precomputeData); // 模块恢复

        this.allocator.resume(precomputeData); // 目标分配器恢复
        this.picker.resume(precomputeData); // 命令选择器恢复
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo); // 在可视化窗口中显示当前时间步信息
        }
    }


    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData debugData) {
        modulesPreparate(); // 模块准备

        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo); // 在可视化窗口中显示当前时间步信息
        }
    }
}
