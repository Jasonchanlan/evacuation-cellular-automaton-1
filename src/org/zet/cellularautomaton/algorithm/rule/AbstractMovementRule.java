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
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.DoorCell;
import org.zet.cellularautomaton.Individual;
import java.util.ArrayList;
import java.util.List;
import org.zetool.common.debug.Debug;

/**
 * @author Jan-Philipp Kappmeier
 *
 */
public abstract class AbstractMovementRule extends AbstractEvacuationRule {

    protected double speed;
    protected double dist;
    
    private boolean directExecute;
    private boolean moveCompleted;
    private List<EvacCell> possibleTargets;

    public AbstractMovementRule() {
        directExecute = true;
        moveCompleted = false;
    }
    /**
     * Computes and returns possible targets and also sets them, such that they can be retrieved using {@link #getPossibleTargets()
     * }.
     *
     * @param fromCell
     * @param onlyFreeNeighbours
     * @return
     */
    protected List<EvacCell> computePossibleTargets(EvacCell fromCell, boolean onlyFreeNeighbours) {
        possibleTargets = new ArrayList<>();
        List<EvacCell> neighbors = onlyFreeNeighbours ? fromCell.getFreeNeighbours() : fromCell.getNeighbours();

        Direction8 dir = fromCell.getIndividual().getDirection();

        for (EvacCell c : neighbors) {
            if (fromCell.getIndividual().isSafe() && !c.isSafe()) {
                continue; // ignore all moves that would mean walking out of safe areas
            }
            if (fromCell instanceof DoorCell && c instanceof DoorCell) {
                possibleTargets.add(c);
                continue;
            }
            Direction8 rel = fromCell.getRelative(c);
            if (dir == rel) {
                possibleTargets.add(c);
            } else if (dir == rel.getClockwise()) {
                possibleTargets.add(c);
            } else if (dir == rel.getClockwise().getClockwise()) {
                possibleTargets.add(c);
            } else if (dir == rel.getCounterClockwise()) {
                possibleTargets.add(c);
            } else if (dir == rel.getCounterClockwise().getCounterClockwise()) {
                possibleTargets.add(c);
            }
        }
        return possibleTargets;
    }
    
    /**
     * Returns the possible targets already sorted by priority. The possible targets either have been set before using {@link #setPossibleTargets(java.util.ArrayList)
     * }
     * ore been computed using {@link #getPossibleTargets(ds.ca.evac.EvacCell, boolean) }.
     *
     * @return a list of possible targets.
     */
    public List<EvacCell> getPossibleTargets() {
        return possibleTargets;
    }

    /**
     * In this simple implementation always the first possible cell is returned. As this method should be overridden, a
     * warning is printed to the err log if it is used.
     *
     * @param cell not used in the simple imlementation
     * @param targets possible targets (only the first one is used)
     * @return the first cell of the possible targets
     */
    public EvacCell selectTargetCell(EvacCell cell, List<EvacCell> targets) {
        Debug.globalLogger.warning("Not-overriden target cell selection is used.");
        if( targets.isEmpty()) {
            throw new IllegalArgumentException("Target list cannot be empty.");
        }
        return targets.get(0);
    }

    protected double getSwayDelay(Individual ind, Direction8 direction) {
        if (ind.getDirection() == direction) {
            return 0;
        } else if (ind.getDirection() == direction.getClockwise()
                || ind.getDirection() == direction.getCounterClockwise()) {
            return 0.5;
        } else if (ind.getDirection() == direction.getClockwise().getClockwise()
                || ind.getDirection() == direction.getCounterClockwise().getCounterClockwise()) {
            return 1;
        } else {
            return 2;
        }
    }

    /**
     * Sets the time when the current movement is over for an individual and actualizates the needed time in the
     * cellular automaton. Fractional values are accepted and are rounded up to the next integral value, to be used
     * in the integral cellular automaton. Updates also the step end time for the given individual (the time is not
     * rounded).
     *
     * @param i the individual
     * @param d the (real) time when the movement is over
     */
    protected void setStepEndTime(Individual i, double d) {
        i.setStepEndTime(d);
        esp.getCa().setNeededTime((int) Math.ceil(d));
    }

    public boolean isDirectExecute() {
        return directExecute;
    }

    public void setDirectExecute(boolean directExecute) {
        this.directExecute = directExecute;
    }

    public boolean isMoveCompleted() {
        return moveCompleted;
    }

    protected void setMoveRuleCompleted(boolean moveCompleted) {
        this.moveCompleted = moveCompleted;
    }

    public abstract void move(EvacCell from, EvacCell target);

    public abstract void swap(EvacCell cell1, EvacCell cell2);
}
