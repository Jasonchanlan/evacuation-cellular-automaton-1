/* zet evacuation tool copyright (c) 2007-15 zet evacuation team
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
package org.zet.cellularautomaton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A StaticPotential is special type of {@link PotentialMap}, of which exist several in one {@link PotentialManager}.
 * Therefore it has a unique ID. The {@code StaticPotential} consists of two potentials. Both describe the distance to
 * an {@link ExitCell}. But the first one represents this distance with an smoothly calculated value while the other
 * one contains the exact distance.
 */
public class StaticPotential extends PotentialMap {

    protected String name = "DefaultNameForStaticPotential";

    /** Counts the number of existing StaticPotentials. Every new StaticPotential gets automatically a unique ID. */
    protected static int idCount = 0;

    /** Attractivity for this cell. */
    private int attractivity;

    /** Id of the StaticPotential. */
    protected int id;

    /** contains the associated ExitCells. */
    private List<ExitCell> associatedExitCells;

    /** A HashMap that assign each EvacCell a Int value which represents the real distance). */
    private final HashMap<EvacCell, Double> cellToDistance;

    /**
     * Creates a StaticPotential with a automatic generated unique ID, that can not be changed.
     */
    public StaticPotential() {
        super();
        this.id = idCount;
        idCount++;
        cellToDistance = new HashMap<>();
        associatedExitCells = new ArrayList<>();
    }
    
    protected StaticPotential(HashMap<EvacCell, Double> initialDistance) {
        cellToDistance = initialDistance;
    }

    /**
     * Get the ID of this StaticPotential.
     *
     * @return ID of this StaticPotential
     */
    public int getID() {
        return id;
    }

    public int getAttractivity() {
        return attractivity;
    }

    public void setAttractivity(int attractivity) {
        this.attractivity = attractivity;
    }

    /**
     * Stores the specified distance for an {@link EvacCell} in this {@code StaticPotential}. If an {@link EvacCell}
     * is specified that already exists, the value will be overwritten.
     *
     * @param cell cell which has to be updated or mapped
     * @param i distance of the cell
     */
    public void setDistance(EvacCell cell, double i) {
        cellToDistance.put(cell, i);
    }

    /**
     * Gets the distance of a specified EvacCell. The method returns -1 if you try to get the distance of a cell that
     * does not exists.
     *
     * @param cell A cell which distance you want to know.
     * @return distance of the specified cell or -1 if the cell is not mapped by this potential
     */
    public double getDistance(EvacCell cell) throws IllegalArgumentException {
        Double distance = cellToDistance.get(cell);
        return distance == null ? -1.0 : distance;
    }

    public double getMaxDistance() {
        double max = 0;
        for (EvacCell cell : cellToDistance.keySet()) {
            if (getDistance(cell) > max) {
                max = getDistance(cell);
            }
        }
        return max;
    }

    /**
     * Removes the mapping for the specified EvacCell. The method throws {@code IllegalArgumentExceptions} if you try to
     * remove the mapping of a EvacCell that does not exists.
     *
     * @param cell an {@link EvacCell} that mapping you want to remove.
     * @throws IllegalArgumentException if no distance was stored for cell
     */
    public void deleteDistanceCell(EvacCell cell) {
        if (!(cellToDistance.containsKey(cell))) {
            throw new IllegalArgumentException("The Cell must be insert previously!");
        }
        cellToDistance.remove(cell);
    }

    /**
     * Returns {@code true} if the mapping for the specified {@link EvacCell} exists.
     *
     * @param cell an {@link EvacCell} of that you want to know if it exists.
     * @return {@code true} if the distance has been defined, {@code false} otherwise
     */
    public boolean containsDistance(EvacCell cell) {
        return cellToDistance.containsKey(cell);
    }

    public List<ExitCell> getAssociatedExitCells() {
        return associatedExitCells;
    }

    public void setAssociatedExitCells(List<ExitCell> associatedExitCells) {
        this.associatedExitCells = associatedExitCells;
        setName(associatedExitCells.get(0).getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTruePotential(EvacCell cell) {
        return getPotential(cell);
    }

    public EvacPotential getAsEvacPotential(Individual i, CellularAutomatonDirectionChecker checker) {
        EvacPotential evacPotential = new EvacPotential(i, checker, cellToDistance);
        evacPotential.setAssociatedExitCells(this.associatedExitCells);
        evacPotential.setAttractivity(this.attractivity);
        evacPotential.cellToPotential = this.cellToPotential;
        evacPotential.setName(this.name);
        evacPotential.id = this.id;
        return evacPotential;
    }

}
