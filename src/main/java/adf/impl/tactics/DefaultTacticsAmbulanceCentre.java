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
import adf.core.component.tactics.TacticsAmbulanceCentre;
import adf.core.debug.WorldViewLauncher;
import rescuecore2.worldmodel.EntityID;

import java.util.Map;

/**
 * 避难所的默认策略类
 *
 * @author <a href="https://roozen.top">Roozen</a>
 */
public class DefaultTacticsAmbulanceCentre extends TacticsAmbulanceCentre {

    private TargetAllocator allocator;
    private CommandPicker picker;
    private Boolean isVisualDebug;

    /**
     * 初始化方法
     * 在此方法中进行模块的初始化和注册
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void initialize(AgentInfo agentInfo, WorldInfo worldInfo,
                           ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                           MessageManager messageManager, DevelopData developData) {
        // 设置消息管理器的订阅者和协调者
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
                // 获取目标分配器和指令选择器
                this.allocator = moduleManager.getModule(
                        "DefaultTacticsAmbulanceCentre.TargetAllocator",
                        "adf.impl.module.complex.DefaultAmbulanceTargetAllocator");
                this.picker = moduleManager.getCommandPicker(
                        "DefaultTacticsAmbulanceCentre.CommandPicker",
                        "adf.impl.centralized.DefaultCommandPickerAmbulance");
                break;
        }
        // 注册模块
        registerModule(this.allocator);
        registerModule(this.picker);

        // 判断是否可视化调试
        this.isVisualDebug = (scenarioInfo.isDebugMode() && moduleManager
                .getModuleConfig().getBooleanValue("VisualDebug", false));
    }

    /**
     * 思考方法
     * 在此方法中进行模块信息更新、可视化调试和发送消息等操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param messageManager 消息管理器
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void think(AgentInfo agentInfo, WorldInfo worldInfo,
                      ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                      MessageManager messageManager, DevelopData developData) {
        // 更新模块信息
        modulesUpdateInfo(messageManager);

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }

        // 获取目标分配器的结果，并根据结果发送消息
        Map<EntityID, EntityID> allocatorResult = this.allocator.calc().getResult();
        for (CommunicationMessage message : this.picker
                .setAllocatorResult(allocatorResult).calc().getResult()) {
            messageManager.addMessage(message);
        }
    }

    /**
     * 恢复方法
     * 在precompute恢复阶段进行模块的恢复操作
     *
     * @param agentInfo      代理的信息
     * @param worldInfo      世界的信息
     * @param scenarioInfo   场景的信息
     * @param moduleManager  模块管理器
     * @param precomputeData 预计算数据
     * @param developData    开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void resume(AgentInfo agentInfo, WorldInfo worldInfo,
                       ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                       PrecomputeData precomputeData, DevelopData developData) {
        // 模块恢复操作
        modulesResume(precomputeData);

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }

    /**
     * 准备方法
     * 在precompute准备阶段进行模块的准备操作
     *
     * @param agentInfo     代理的信息
     * @param worldInfo     世界的信息
     * @param scenarioInfo  场景的信息
     * @param moduleManager 模块管理器
     * @param developData   开发调试数据
     * @author <a href="https://roozen.top">Roozen</a>
     */
    @Override
    public void preparate(AgentInfo agentInfo, WorldInfo worldInfo,
                          ScenarioInfo scenarioInfo, ModuleManager moduleManager,
                          DevelopData developData) {
        // 模块准备操作
        modulesPreparate();

        // 判断是否可视化调试，并显示时间步信息
        if (isVisualDebug) {
            WorldViewLauncher.getInstance().showTimeStep(agentInfo, worldInfo,
                    scenarioInfo);
        }
    }
}
