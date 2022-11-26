/**
 * 中心代理的包
 * <p>
 * 这种类型的代理与世界的唯一交互是通过无线电通信,它们在仿真服务器中作为建筑物存在<br>
 * 中心代理共有三种类型:
 * <ul>
 *     <li>救护中心 Ambulance</li>
 *     <li>消防局 Fire</li>
 *     <li>警察局 Police</li>
 * </ul>
 * </p>
 * <p>
 * 它们主要有以下两种算法:
 * <ul>
 *     <li>命令选择器{@link adf.core.component.centralized.CommandPicker}
 *     <li>命令执行器{@link adf.core.component.centralized.CommandExecutor}
 * </ul>
 * </p>
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @see adf.core.component.centralized.CommandPicker
 * @see adf.core.component.centralized.CommandExecutor
 * @see adf.impl.centralized.DefaultCommandExecutorAmbulance
 * @see adf.impl.centralized.DefaultCommandExecutorFire
 * @see adf.impl.centralized.DefaultCommandExecutorPolice
 * @see adf.impl.centralized.DefaultCommandExecutorScout
 * @see adf.impl.centralized.DefaultCommandExecutorScoutPolice
 * @see adf.impl.centralized.DefaultCommandPickerAmbulance
 * @see adf.impl.centralized.DefaultCommandPickerFire
 * @see adf.impl.centralized.DefaultCommandPickerPolice
 */
package adf.impl.centralized;