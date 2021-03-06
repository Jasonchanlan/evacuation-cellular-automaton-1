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
package org.zet.algo.ca.util;

import java.util.Collection;
import org.zet.cellularautomaton.ExitCell;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.EvacCellInterface;

/**
 * @author Daniel R. Schmidt
 *
 */
public class PotentialUtils {

    /**
     * Private constructor for static utility class.
     */
    private PotentialUtils() {
    }

    /**
     * This method merges {@link StaticPotential}s into a new one. The new potential is calculated for each cell by taking the
     * minimum over all given static potentials. The attractivity of the new static potential is the average over all
     * attractivity values given by the specified static potentials to merge.
     *
     * @param potentialsToMerge Contains an ArrayList with the StaticPotential object to merge
     * @return the new potential
     */
    public static StaticPotential mergePotentials(Collection<StaticPotential> potentialsToMerge) {
        StaticPotential newStaticPotential = new StaticPotential();
        //stores the sum of all attractivity values
        int totalAttractivity = 0;
        // iterate over all mapped cells
        for (StaticPotential sp : potentialsToMerge) {
            totalAttractivity += sp.getAttractivity();
            for (EvacCellInterface c : sp.getMappedCells()) {
                int minPot = getMinPotential(c, potentialsToMerge);
                //put the current cell along with minPot in newSP
                newStaticPotential.setPotential(c, minPot);
            }
            for (ExitCell c : sp.getAssociatedExitCells()) {
                newStaticPotential.getAssociatedExitCells().add(c);
            }
        }
        if (!potentialsToMerge.isEmpty()) {// catch error if no exit is set!
            newStaticPotential.setAttractivity(totalAttractivity / potentialsToMerge.size());
        }
        return newStaticPotential;
    }

    /**
     * Computes the minimum potential for one cell over all potentials in potentialsToMerge.
     * @param c the cell
     * @param potentialsToMerge the list of potentials
     * @return the minimum potential
     */
    private static int getMinPotential(EvacCellInterface c, Collection<StaticPotential> potentialsToMerge) {
        int minPot = Integer.MAX_VALUE;
        for (StaticPotential localSP : potentialsToMerge) {
            if (localSP.getPotential(c) >= 0) { // if the potential is negative, it is invalid for the cell
                minPot = Math.min(minPot, localSP.getPotential(c));
            }
        }
        return minPot;
    }
}
