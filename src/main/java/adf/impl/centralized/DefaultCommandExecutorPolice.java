package adf.impl.centralized;

import adf.core.agent.action.Action;
import adf.core.agent.action.common.ActionMove;
import adf.core.agent.action.common.ActionRest;
import adf.core.agent.communication.MessageManager;
import adf.core.agent.communication.standard.bundle.centralized.CommandPolice;
import adf.core.agent.communication.standard.bundle.centralized.MessageReport;
import adf.core.agent.develop.DevelopData;
import adf.core.agent.info.AgentInfo;
import adf.core.agent.info.ScenarioInfo;
import adf.core.agent.info.WorldInfo;
import adf.core.agent.module.ModuleManager;
import adf.core.agent.precompute.PrecomputeData;
import adf.core.component.centralized.CommandExecutor;
import adf.core.component.extaction.ExtAction;
import adf.core.component.module.algorithm.PathPlanning;
import rescuecore2.standard.entities.*;
import rescuecore2.worldmodel.EntityID;

import java.util.List;
import java.util.Objects;

import static rescuecore2.standard.entities.StandardEntityURN.BLOCKADE;
import static rescuecore2.standard.entities.StandardEntityURN.REFUGE;

/**
 *警察的默认命令执行器
 *
 * @author <a href="https://downsxu.github.io/">DownsXu</a>
 */
public class DefaultCommandExecutorPolice extends CommandExecutor<CommandPolice> {

  /**
   * 未知动作
   */
  private static final int ACTION_UNKNOWN = -1;
  /**
   * 休息
   */
  private static final int ACTION_REST = CommandPolice.ACTION_REST;
  /**
   *移动
   */
  private static final int ACTION_MOVE = CommandPolice.ACTION_MOVE;
  /**
   * 清理
   */
  private static final int ACTION_CLEAR = CommandPolice.ACTION_CLEAR;
  /**
   * 自主行动
   */
  private static final int ACTION_AUTONOMY = CommandPolice.ACTION_AUTONOMY;

  /**
   * 命令类型
   */
  private int commandType;

  /**
   * 命令目标的EntityID
   */
  private EntityID target;

  /**
   * 命令执行者的EntityID
   */
  private EntityID commanderID;

  /**
   * 路径规划算法
   */
  private PathPlanning pathPlanning;

  /**
   * 扩展行动：清理
   */
  private ExtAction actionExtClear;

  /**
   * 扩展行动：移动
   */
  private ExtAction actionExtMove;

  /**
   * {@link DefaultCommandExecutorPolice}的构造函数
   *
   * @param ai     代理信息
   * @param wi     世界信息
   * @param si  场景信息
   * @param moduleManager 模块管理器
   * @param developData   开发数据
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  public DefaultCommandExecutorPolice(AgentInfo ai, WorldInfo wi, ScenarioInfo si, ModuleManager moduleManager, DevelopData developData) {
    super(ai, wi, si, moduleManager, developData);
    this.commandType = ACTION_UNKNOWN;

    switch (scenarioInfo.getMode()) {
      case PRECOMPUTATION_PHASE:
      case PRECOMPUTED:
      case NON_PRECOMPUTE:
        this.pathPlanning = moduleManager.getModule(
            "DefaultCommandExecutorPolice.PathPlanning",
            "adf.impl.module.algorithm.DijkstraPathPlanning");
        this.actionExtClear = moduleManager.getExtAction(
            "DefaultCommandExecutorPolice.ExtActionClear",
            "adf.impl.extaction.DefaultExtActionClear");
        this.actionExtMove = moduleManager.getExtAction(
            "DefaultCommandExecutorPolice.ExtActionMove",
            "adf.impl.extaction.DefaultExtActionMove");
        break;
    }
  }


  /**
   * 设置命令
   *
   * @param command 警察命令
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */

  @Override
  public CommandExecutor setCommand(CommandPolice command) {
    EntityID agentID = this.agentInfo.getID();
    if (command.isToIDDefined() && Objects.requireNonNull(command.getToID())
        .getValue() == agentID.getValue()) {
      this.commandType = command.getAction();
      this.target = command.getTargetID();
      this.commanderID = command.getSenderID();
    }
    return this;
  }

  /**
   * 预计算时执行的方法
   *
   * @param precomputeData 预计算数据
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  public CommandExecutor precompute(PrecomputeData precomputeData) {
    super.precompute(precomputeData);
    if (this.getCountPrecompute() >= 2) {
      return this;
    }
    this.pathPlanning.precompute(precomputeData);
    this.actionExtClear.precompute(precomputeData);
    this.actionExtMove.precompute(precomputeData);
    return this;
  }

  /**
   *预计算模式的初始化处理方法
   *
   * @param precomputeData 预计算数据
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  public CommandExecutor resume(PrecomputeData precomputeData) {
    super.resume(precomputeData);
    if (this.getCountResume() >= 2) {
      return this;
    }
    this.pathPlanning.resume(precomputeData);
    this.actionExtClear.resume(precomputeData);
    this.actionExtMove.resume(precomputeData);
    return this;
  }


  /**
   *无预计算模式的初始化处理方法
   *
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  public CommandExecutor preparate() {
    super.preparate();
    if (this.getCountPreparate() >= 2) {
      return this;
    }
    this.pathPlanning.preparate();
    this.actionExtClear.preparate();
    this.actionExtMove.preparate();
    return this;
  }

  /**
   *每个回合都会执行这个方法来更新agent所持有的信息
   *
   * @param messageManager
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  public CommandExecutor updateInfo(MessageManager messageManager) {
    super.updateInfo(messageManager);
    if (this.getCountUpdateInfo() >= 2) {
      return this;
    }
    this.pathPlanning.updateInfo(messageManager);
    this.actionExtClear.updateInfo(messageManager);
    this.actionExtMove.updateInfo(messageManager);

    if (this.isCommandCompleted()) {
      if (this.commandType != ACTION_UNKNOWN) {
        messageManager
            .addMessage(new MessageReport(true, true, false, this.commanderID));
        this.commandType = ACTION_UNKNOWN;
        this.target = null;
        this.commanderID = null;
      }
    }
    return this;
  }


  /**
   * 计算应该执行的动作,将结果保存在{@link #result}中
   *
   * @return this
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  @Override
  public CommandExecutor calc() {
    this.result = null;
    EntityID position = this.agentInfo.getPosition();
    switch (this.commandType) {
      case ACTION_REST:
        if (this.target == null) {
          if (worldInfo.getEntity(position).getStandardURN() != REFUGE) {
            this.pathPlanning.setFrom(position);
            this.pathPlanning
                .setDestination(this.worldInfo.getEntityIDsOfType(REFUGE));
            List<EntityID> path = this.pathPlanning.calc().getResult();
            if (path != null && path.size() > 0) {
              Action action = this.actionExtClear
                  .setTarget(path.get(path.size() - 1)).calc().getAction();
              if (action == null) {
                action = new ActionMove(path);
              }
              this.result = action;
              return this;
            }
          }
        } else if (position.getValue() != this.target.getValue()) {
          List<EntityID> path = this.pathPlanning.getResult(position,
              this.target);
          if (path != null && path.size() > 0) {
            Action action = this.actionExtClear
                .setTarget(path.get(path.size() - 1)).calc().getAction();
            if (action == null) {
              action = new ActionMove(path);
            }
            this.result = action;
            return this;
          }
        }
        this.result = new ActionRest();
        return this;
      case ACTION_MOVE:
        if (this.target != null) {
          this.result = this.actionExtClear.setTarget(this.target).calc()
              .getAction();
        }
        return this;
      case ACTION_CLEAR:
        if (this.target != null) {
          this.result = this.actionExtClear.setTarget(this.target).calc()
              .getAction();
        }
        return this;
      case ACTION_AUTONOMY:
        if (this.target == null) {
          return this;
        }
        StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
        if (targetEntity.getStandardURN() == REFUGE) {
          PoliceForce agent = (PoliceForce) this.agentInfo.me();
          if (agent.getDamage() > 0) {
            if (position.getValue() != this.target.getValue()) {
              List<EntityID> path = this.pathPlanning.getResult(position,
                  this.target);
              if (path != null && path.size() > 0) {
                Action action = this.actionExtClear
                    .setTarget(path.get(path.size() - 1)).calc().getAction();
                if (action == null) {
                  action = new ActionMove(path);
                }
                this.result = action;
                return this;
              }
            }
            this.result = new ActionRest();
          } else {
            this.result = this.actionExtClear.setTarget(this.target).calc()
                .getAction();
          }
        } else if (targetEntity instanceof Area) {
          this.result = this.actionExtClear.setTarget(this.target).calc()
              .getAction();
          return this;
        } else if (targetEntity instanceof Human) {
          Human h = (Human) targetEntity;
          if ((h.isHPDefined() && h.getHP() == 0)) {
            return this;
          }
          if (h.isPositionDefined()
              && this.worldInfo.getPosition(h) instanceof Area) {
            this.target = h.getPosition();
            this.result = this.actionExtClear.setTarget(this.target).calc()
                .getAction();
          }
        } else if (targetEntity.getStandardURN() == BLOCKADE) {
          Blockade blockade = (Blockade) targetEntity;
          if (blockade.isPositionDefined()) {
            this.target = blockade.getPosition();
            this.result = this.actionExtClear.setTarget(this.target).calc()
                .getAction();
          }
        }
    }
    return this;
  }

  /**
   * 判断命令是否执行完毕
   *
   * @return true:执行完毕 / false ：未执行完毕
   * @author <a href="https://downsxu.github.io/">DownsXu</a>
   */
  private boolean isCommandCompleted() {
    PoliceForce agent = (PoliceForce) this.agentInfo.me();
    switch (this.commandType) {
      case ACTION_REST:
        if (this.target == null) {
          return (agent.getDamage() == 0);
        }
        if (this.worldInfo.getEntity(this.target).getStandardURN() == REFUGE) {
          if (agent.getPosition().getValue() == this.target.getValue()) {
            return (agent.getDamage() == 0);
          }
        }
        return false;
      case ACTION_MOVE:
        return this.target == null || (this.agentInfo.getPosition()
            .getValue() == this.target.getValue());
      case ACTION_CLEAR:
        if (this.target == null) {
          return true;
        }
        StandardEntity entity = this.worldInfo.getEntity(this.target);
        if (entity instanceof Road) {
          Road road = (Road) entity;
          if (road.isBlockadesDefined()) {
            return road.getBlockades().isEmpty();
          }
          if (this.agentInfo.getPosition().getValue() != this.target
              .getValue()) {
            return false;
          }
        }
        return true;
      case ACTION_AUTONOMY:
        if (this.target != null) {
          StandardEntity targetEntity = this.worldInfo.getEntity(this.target);
          if (targetEntity.getStandardURN() == REFUGE) {
            this.commandType = agent.getDamage() > 0 ? ACTION_REST
                : ACTION_CLEAR;
            return this.isCommandCompleted();
          } else if (targetEntity instanceof Area) {
            this.commandType = ACTION_CLEAR;
            return this.isCommandCompleted();
          } else if (targetEntity instanceof Human) {
            Human h = (Human) targetEntity;
            if ((h.isHPDefined() && h.getHP() == 0)) {
              return true;
            }
            if (h.isPositionDefined()
                && this.worldInfo.getPosition(h) instanceof Area) {
              this.target = h.getPosition();
              this.commandType = ACTION_CLEAR;
              return this.isCommandCompleted();
            }
          } else if (targetEntity.getStandardURN() == BLOCKADE) {
            Blockade blockade = (Blockade) targetEntity;
            if (blockade.isPositionDefined()) {
              this.target = blockade.getPosition();
              this.commandType = ACTION_CLEAR;
              return this.isCommandCompleted();
            }
          }
        }
        return true;
    }
    return true;
  }
}