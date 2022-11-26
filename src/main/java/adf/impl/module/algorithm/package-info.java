/**
 * 一些基本算法的包
 * <p>
 * 包括:
 * <ul>
 *     <li>聚类算法{@link adf.core.component.module.algorithm.Clustering}:
 *     <ul>
 *         <li>动态聚类{@link adf.core.component.module.algorithm.DynamicClustering}
 *         <ul>具体的实现类：
 *             <li>消防员聚类算法{@link adf.impl.module.algorithm.FireClustering}</li>
 *         </ul>
 *         </li>
 *         <li>静态聚类：{@link adf.core.component.module.algorithm.StaticClustering}</li>
 *         <ul>具体的实现类：
 *             <li><a href="https://zhuanlan.zhihu.com/p/78798251">KMeans算法(基于欧式距离的聚类算法)</a>{@link adf.impl.module.algorithm.KMeansClustering}</li>
 *         </ul>
 *     </ul>
 *     </li>
 *     <li>路径规划算法{@link adf.core.component.module.algorithm.PathPlanning}
 *     <ul>具体的实现类：
 *             <li>A*算法{@link adf.impl.module.algorithm.AStarPathPlanning}</li>
 *             <li>迪杰斯特拉(Dijkstra)算法{@link adf.impl.module.algorithm.DijkstraPathPlanning}</li>
 *     </ul>
 *     </li>
 * </ul>
 * </p>
 *
 * @author <a href="https://roozen.top">Roozen</a>
 * @version 1.0
 * @see adf.core.component.module.algorithm.Clustering
 * @see adf.core.component.module.algorithm.PathPlanning
 * @see adf.impl.module.algorithm.AStarPathPlanning
 * @see adf.impl.module.algorithm.DijkstraPathPlanning
 * @see adf.impl.module.algorithm.FireClustering
 * @see adf.impl.module.algorithm.KMeansClustering
 */
package adf.impl.module.algorithm;