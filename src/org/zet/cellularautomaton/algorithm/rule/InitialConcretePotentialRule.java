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
import java.util.Collections;
import java.util.List;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.potential.PotentialMemory;

/**
 * This rule chooses an {@link Individual}'s initial {@link StaticPotential} according to the attractivity value of
 * those {@link ExitCells}, whose distances to the individual are acceptable according to its familarity value. If the
 * Individual standing on a cell is caged (it cannot leave the building, because there is now passable way to an exit),
 * this individual has to die because it cannot be evacuated.
 *
 * @author Marcel Preuß
 *
 */
public class InitialConcretePotentialRule extends AbstractInitialRule {

    /**
     * This rule chooses an Individual's (the one standing on "cell") initial StaticPotential according to the
     * attractivity value of those Exit-Cells, whose distances to the Individual are acceptable according to the
     * Individual's familarity value. If the Individual standing on "cell" is caged (it cannot leave the building,
     * because there is now passable way to an ExitCell), this Individual has to die because it cannot be evacuated.
     *
     * @param cell the cell
     */
    @Override
    protected void onExecute(EvacCell cell) {
        Individual individual = cell.getState().getIndividual();
        List<PotentialMemory<StaticPotential>> potentialToLengthOfWayMapper = new ArrayList<>();
        List<StaticPotential> staticPotentials = new ArrayList<>();
        staticPotentials.addAll(es.getCellularAutomaton().getStaticPotentials());
        double minDistanceToEvacArea = Double.MAX_VALUE;
        double distanceToEvacArea;
        for (StaticPotential sp : staticPotentials) {
            distanceToEvacArea = sp.getDistance(es.propertyFor(individual).getCell());
            if (distanceToEvacArea >= 0 && distanceToEvacArea <= minDistanceToEvacArea) {
                minDistanceToEvacArea = sp.getDistance(es.propertyFor(individual).getCell());
            }
            if (sp.getDistance(es.propertyFor(individual).getCell()) >= 0) {// if this StaticPotential can lead the individual to an ExitCell
                potentialToLengthOfWayMapper.add(new PotentialMemory<>(es.propertyFor(individual).getCell(), sp));
            }
        }
        
        // Sort the Individual's StaticPotentials according to their familiarity value
        Collections.sort(potentialToLengthOfWayMapper);
        // Check whether the individual is caged and cannot leave the building -> it has to die
        if (potentialToLengthOfWayMapper.isEmpty()) {
            ec.die(individual, DeathCause.EXIT_UNREACHABLE);
        } else {
            int nrOfPossiblePotentials = (int) (Math.round((1 - individual.getFamiliarity()) * potentialToLengthOfWayMapper.size()));
            if (nrOfPossiblePotentials < 1) {
                nrOfPossiblePotentials = 1;
                // select the potential with the highest attractivity from one of the nrOfPossiblePotentials
            }
            int best = 0;
            for (int i = 1; i < nrOfPossiblePotentials; i++) {
                if (potentialToLengthOfWayMapper.get(i).getStaticPotential().getAttractivity() > potentialToLengthOfWayMapper.get(best).getStaticPotential().getAttractivity() ) {
                    best = i;
                }
            }
            es.propertyFor(individual).setStaticPotential(potentialToLengthOfWayMapper.get(best).getStaticPotential());
            es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addMinDistancesToStatistic(individual, minDistanceToEvacArea, potentialToLengthOfWayMapper.get(best).getStaticPotential().getDistance(cell));
            es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addChangedPotentialToStatistic(individual, 0);
            es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addExhaustionToStatistic(individual, 0, es.propertyFor(individual).getExhaustion());
            es.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addPanicToStatistic(individual, 0, es.propertyFor(individual).getPanic());
        }
    }
}
