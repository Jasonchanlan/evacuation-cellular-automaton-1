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

import org.zetool.common.util.Direction8;
import org.zetool.rndutils.RandomUtils;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.results.IndividualStateChangeAction;
import org.zet.cellularautomaton.results.VisualResultsRecorder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Daniel R. Schmidt
 */
public class WaitingMovementRule extends SimpleMovementRule2 {

    @Override
    protected void onExecute(org.zet.cellularautomaton.EvacCell cell) {
        ind = cell.getIndividual();
        if (ind.isAlarmed() == true) {
            if (canMove(ind)) {
                if (slack(ind)) {
                    updateExhaustion(ind, cell);
                    setMoveRuleCompleted(true);
                    noMove();
                } else {
                    if (isDirectExecute()) {
                        EvacCell targetCell = selectTargetCell(cell, computePossibleTargets(cell, true));
                        setMoveRuleCompleted(true);
                        move(targetCell);
                    } else {
                        computePossibleTargets(cell, false);
                        setMoveRuleCompleted(true);
                    }
                }
                this.updateSpeed(ind);
            } else {
                // Individual can't move, it is already moving
                setMoveRuleCompleted(false);
            }
        } else { // Individual is not alarmed, that means it remains standing on the cell
            setMoveRuleCompleted(true);
            noMove();
        }
        VisualResultsRecorder.getInstance().recordAction(new IndividualStateChangeAction(ind));
    }

    @Override
    public void move(EvacCell from, EvacCell targetCell) {
        Individual ind = from.getIndividual();
        updatePanic(ind, targetCell);
        updateExhaustion(ind, targetCell);
        super.move(from, targetCell);
    }

    protected void updatePanic(Individual i, EvacCell targetCell) {
        double oldPanic = i.getPanic();
        esp.getParameterSet().updatePanic(i, targetCell, this.neighboursByPriority(i.getCell()));
        if (oldPanic != i.getPanic()) {
            esp.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addPanicToStatistic(i, esp.getCa().getTimeStep(), i.getPanic());
        }
    }

    protected void updateSpeed(Individual i) {
        esp.getParameterSet().updatePreferredSpeed(i);
    }

    /**
     * Returns all reachable neighbours sorted according to their priority which is calculated by mergePotential(). The
     * first element in the list is the most probable neighbour, the last element is the least probable neighbour.
     *
     * @param cell The cell whose neighbours are to be sorted
     * @return A sorted list of the neighbour cells of {@code cell}, sorted in an increasing fashion according to their
     * potential computed by {@code mergePotential}.
     */
    protected ArrayList<EvacCell> neighboursByPriority(EvacCell cell) {
        class CellPrioritySorter implements Comparator<EvacCell> {

            final EvacCell referenceCell;

            CellPrioritySorter(EvacCell referenceCell) {
                this.referenceCell = referenceCell;
            }

            @Override
            public int compare(EvacCell cell1, EvacCell cell2) {
                final double potential1 = esp.getParameterSet().effectivePotential(referenceCell, cell1);
                final double potential2 = esp.getParameterSet().effectivePotential(referenceCell, cell2);
                if (potential1 < potential2) {
                    return -1;
                } else if (potential1 == potential2) {
                    return 0;
                } else {
                    return 1;
                }
            }
        }

        ArrayList<EvacCell> result = new ArrayList<>(cell.getNeighbours());
        Collections.sort(result, new CellPrioritySorter(cell));
        return result;
    }

    /**
     * Given a starting cell, this method picks one of its reachable neighbors at random. The i-th neighbor is chosen
     * with probability {@code p(i) := N * exp[mergePotentials(i, cell)]} where N is a constant used for normalization.
     *
     * @param cell The starting cell
     * @return A neighbor of {@code cell} chosen at random.
     */
    @Override
    public EvacCell selectTargetCell(EvacCell cell, List<EvacCell> targets) {
        if (targets.isEmpty()) {
            return cell;
        }

        double p[] = new double[targets.size()];

        double max = Integer.MIN_VALUE;
        int max_index = 0;

        for (int i = 0; i < targets.size(); i++) {
            p[i] = Math.exp(esp.getParameterSet().effectivePotential(cell, targets.get(i)));
            if (p[i] > max) {
                max = p[i];
                max_index = i;
            }
        }

        boolean directPath = true; // notice, that direct path is a deterministic rule!
        if (directPath) {
            return targets.get(max_index);
        }

        // raising probablities only makes sense if the cell and all its neighbours are in the same room
        boolean inSameRoom = true;
        for (int i = 0; i < targets.size(); i++) {
            if (!(cell.getRoom().equals(targets.get(i).getRoom()))) {
                inSameRoom = false;
                break;
            }
        }
        if (inSameRoom) {
            EvacCell mostProbableTarget = targets.get(max_index);

            Individual i = cell.getIndividual();
            Direction8 oldDir = i.getDirection();
            Direction8 newDir = cell.equals(mostProbableTarget) ? oldDir : cell.getRelative(mostProbableTarget);

            if (oldDir.equals(newDir)) {
                // No swaying
            } else {
                // swaying!
                // check, if one of the targets is in the old direction

                for (int j = 0; j < targets.size(); ++j) {
                    EvacCell target = targets.get(j);
                    if (target != cell && oldDir.equals(cell.getRelative(target))) {
                        // We found a cell in the current direciton
                        p[j] = p[j] * 10.5;

                    }
                }
            }
        }// end if inSameRoom

        int number = RandomUtils.getInstance().chooseRandomlyAbsolute(p);
        return targets.get(number);
    }

    /**
     * Updates the exhaustion for the individual and updates the statistic.
     *
     * @param i
     * @param targetCell
     */
    protected void updateExhaustion(Individual i, EvacCell targetCell) {
        double oldExhaustion = i.getExhaustion();
        esp.getParameterSet().updateExhaustion(i, targetCell);
        if (oldExhaustion != i.getExhaustion()) {
            esp.getStatisticWriter().getStoredCAStatisticResults().getStoredCAStatisticResultsForIndividuals().addExhaustionToStatistic(i, esp.getCa().getTimeStep(), i.getExhaustion());
        }
    }

    /**
     * Decides randomly if an individual idles.
     *
     * @param i An individual with a given slackness
     * @return {@code true} with a probability of slackness or {@code false} otherwise.
     */
    protected boolean slack(Individual i) {
        double randomNumber = RandomUtils.getInstance().getRandomGenerator().nextDouble();
        return (esp.getParameterSet().idleThreshold(i) > randomNumber);
    }
}