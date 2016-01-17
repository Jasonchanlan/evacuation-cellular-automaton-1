/* zet evacuation tool copyright (c) 2007-14 zet evacuation team
 *
 * This program is free software; you can redistribute it and/or
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package org.zet.cellularautomaton.algorithm.rule;

import java.util.ArrayList;
import java.util.List;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.algorithm.EvacuationState;
import org.zet.cellularautomaton.algorithm.EvacuationStateController;
import org.zet.cellularautomaton.algorithm.EvacuationStateControllerInterface;
import org.zet.cellularautomaton.potential.StaticPotential;

/**
 * This rule chooses an Individual's (the one standing on the current cell) initial StaticPotential according to the
 * distances to the exits. If the Individual standing on "cell" is caged (it cannot leave the building, because there is
 * now passable way to an exit), this Individual has to die because it cannot be evacuated.
 *
 * @author Marcel Preuß
 * @author Sylvie Temme
 *
 */
public class InitialPotentialShortestPathRule extends AbstractInitialRule {

    /**
     * This rule chooses an initial {@link StaticPotential} for the {@link Individual} on a cell. This potential is
     * chosen such that it leads to the nearest exit. If the {@code Individual} standing is caged, i.e. it cannot leave
     * the building, because there is now passable way to an exit, it dies.
     *
     * @param cell the cell
     */
    @Override
    protected void onExecute(EvacCell cell) {
        assignShortestPathPotential(cell, this.es, this.ec);
    }

    public static void assignShortestPathPotential(EvacCell cell, EvacuationState es, EvacuationStateControllerInterface ec) {
        Individual individual = cell.getState().getIndividual();
        List<StaticPotential> staticPotentials = new ArrayList<>();
        staticPotentials.addAll(es.getCellularAutomaton().getStaticPotentials());
        StaticPotential initialPotential = new StaticPotential();
        double minDistanceToEvacArea = Double.POSITIVE_INFINITY;
        double distanceToEvacArea;
        for (StaticPotential sp : staticPotentials) {
            distanceToEvacArea = sp.getDistance(es.propertyFor(individual).getCell());
            if (distanceToEvacArea >= 0 && distanceToEvacArea <= minDistanceToEvacArea) {
                minDistanceToEvacArea = sp.getDistance(es.propertyFor(individual).getCell());
                initialPotential = sp;
            }
        }

        // Check whether the individual is caged and cannot leave the building -> it has to die
        if (Double.isInfinite(minDistanceToEvacArea)) {
            ec.die(individual, DeathCause.EXIT_UNREACHABLE);
        }

        es.propertyFor(individual).setStaticPotential(initialPotential);
        es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addMinDistancesToStatistic(individual, minDistanceToEvacArea, initialPotential.getDistance(cell));
        es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addChangedPotentialToStatistic(individual, 0);
        es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addExhaustionToStatistic(individual, 0, es.propertyFor(individual).getExhaustion());
        es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addPanicToStatistic(individual, 0, es.propertyFor(individual).getPanic());
    }
}
