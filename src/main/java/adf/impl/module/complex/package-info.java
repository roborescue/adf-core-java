/**
 * 复杂算法的包
 * <p>
 * <strong>有如下几类:</strong>
 * <ul>
 *     <li>目标探测器{@link adf.core.component.module.complex.TargetDetector}：
 *     <ul>
 *         <li>道路探测器：{@link adf.core.component.module.complex.RoadDetector} </li>
 *         <li>建筑物探测器：{@link adf.core.component.module.complex.BuildingDetector} </li>
 *         <li>人类探测器：{@link adf.core.component.module.complex.HumanDetector} </li>
 *     </ul>
 *     </li>
 *     <li>目标分配器{@link adf.core.component.module.complex.TargetAllocator}：
 *     <ul>
 *         <li>救护队目标分配器：{@link adf.core.component.module.complex.AmbulanceTargetAllocator}</li>
 *         <li>消防队目标分配器：{@link adf.core.component.module.complex.FireTargetAllocator}</li>
 *         <li>警察目标分配器：{@link adf.core.component.module.complex.PoliceTargetAllocator}</li>
 *     </ul>
 *     </li>
 *     <li>搜索算法{@link adf.core.component.module.complex.Search}</li>
 * </ul>
 * </p>
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @see adf.core.component.module.complex
 * @see adf.impl.module.complex.DefaultAmbulanceTargetAllocator
 * @see adf.impl.module.complex.DefaultBuildingDetector
 * @see adf.impl.module.complex.DefaultFireTargetAllocator
 * @see adf.impl.module.complex.DefaultHumanDetector
 * @see adf.impl.module.complex.DefaultPoliceTargetAllocator
 * @see adf.impl.module.complex.DefaultRoadDetector
 * @see adf.impl.module.complex.DefaultSearch
 */
package adf.impl.module.complex;