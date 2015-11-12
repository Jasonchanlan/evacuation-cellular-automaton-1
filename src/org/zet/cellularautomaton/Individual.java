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

import org.zet.cellularautomaton.potential.PotentialMemory;
import org.zet.cellularautomaton.potential.DynamicPotential;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zetool.common.util.Direction8;
import org.zetool.container.mapping.Identifiable;
import java.util.UUID;
import org.zet.cellularautomaton.potential.Potential;

/**
 * A Individual represets a Person in the evacuationtool with the following characteristics: familiarity, panic,
 * slackness, relativeMaxSpeed. Also an * exhaustion factor exists, which simulates exhaustion after walking a long way.
 * An Individual is located in a {@link EvacCell} of the building and each {@code Individual} has a
 * {@link StaticPotential}, which guides the person to an exit.
 */
public class Individual implements Identifiable {

    private int age;
    private double familiarity;
    private double panic = 0.0001;
    private double panicFactor;
    private double slackness;
    private double exhaustion = 0;
    private double exhaustionFactor;
    private double relativeSpeed;
    private double relativeMaxSpeed;
    private double absoluteMaxSpeed;
    private boolean alarmed;
    private double reactionTime;
    private EvacCell cell;
    private StaticPotential staticPotential;
    private DynamicPotential dynamicPotential;
    private DeathCause deathCause;
    /**
     * The number of the individual. Each Individual of an CA should have a unique identifier.
     */
    private int individualNumber = 0;
    /**
     * The (accurate) time when the moving of the individual is over. Initializes with 0 as step 0 is the first cellular
     * automaton step.
     */
    private double stepEndTime = 0;
    /**
     * The (accurate) time when the first moving of the individual starts. Initializes invalid.
     */
    private double stepStartTime = -1;
    /**
     * Unique ID of the assignment type this individual is in
     */
    private UUID uid;
    /**
     * The time, when the individual has last entered an area, where it is safe ( = area of save- and exitcells)
     */
    private int safetyTime;
    /**
     * Indicates, if the individual is already safe; that means: on save- oder exitcells
     */
    private boolean safe;
    private boolean isEvacuated = false;
    private boolean isDead = false;
    private PotentialMemory potentialMemoryStart;
    private PotentialMemory potentialMemoryEnd;
    private int memoryIndex;
    int cellCountToChange;
    Direction8 dir;

    public Individual() {
    }

    public Individual(int age, double familiarity, double panicFactor, double slackness, double exhaustionFactor, double relativeMaxSpeed, double reactiontime, UUID uid) {
        this.age = age;
        this.familiarity = familiarity;
        this.panicFactor = panicFactor;
        this.slackness = slackness;
        this.exhaustionFactor = exhaustionFactor;
        this.relativeMaxSpeed = relativeMaxSpeed;
        this.relativeSpeed = relativeMaxSpeed;
        this.alarmed = false;
        this.cell = null;
        this.staticPotential = null;
        this.dynamicPotential = null;
        this.reactionTime = reactiontime;
        this.uid = uid;
        safe = false;
        safetyTime = -1;
        this.dir = Direction8.Top; // Just use an arbitrary direction

        /**
         * Calibratingfactor - The bigger {@code cellCountToChange}, the longer an individual moves before a possible
         * potential change
         */
        cellCountToChange = (int) Math.round(relativeSpeed * 15 / 0.4);
        potentialMemoryStart = new PotentialMemory();
        potentialMemoryEnd = new PotentialMemory();
        memoryIndex = 0;
    }

    public PotentialMemory getPotentialMemoryStart() {
        return potentialMemoryStart;
    }

    public PotentialMemory getPotentialMemoryEnd() {
        return potentialMemoryEnd;
    }

    public void setPotentialMemoryStart(PotentialMemory start) {
        potentialMemoryStart = start;
    }

    public void setPotentialMemoryEnd(PotentialMemory end) {
        potentialMemoryEnd = end;
    }

    public int getCellCountToChange() {
        return cellCountToChange;
    }

    /**
     * Used for some potential change rule...
     *
     * @return
     */
    public int getMemoryIndex() {
        return memoryIndex;
    }

    /**
     * Used for some potential change rule...
     *
     * @param index
     */
    public void setMemoryIndex(int index) {
        memoryIndex = index;
    }

    public double getStepEndTime() {
        return stepEndTime;
    }

    public void setStepEndTime(double stepEndTime) {
        this.stepEndTime = stepEndTime;
    }

    public double getStepStartTime() {
        return stepStartTime;
    }

    public void setStepStartTime(double stepStartTime) {
        this.stepStartTime = stepStartTime;
    }

    /**
     * Returns true, if the person is evacuated, false elsewise.
     *
     * @return the evacuation status
     */
    public boolean isEvacuated() {
        return this.isEvacuated;
    }

    /**
     * Sets this {@code Individual} evacuated.
     */
    public void setEvacuated() {
        isEvacuated = true;
    }

    /**
     * Returns the {@link DeathCause} of an individual.
     *
     * @return the cause
     */
    public DeathCause getDeathCause() {
        return deathCause;
    }

    /**
     * Returns the time when the individual is safe.
     *
     * @return The time when the individual is safe.
     */
    public int getSafetyTime() {
        return safetyTime;
    }

    /**
     * Sets the time when the individual is evacuated.
     *
     * @param time The time when the individual is evacuated.
     */
    public void setSafetyTime(int time) {
        safetyTime = time;
    }

    /**
     * Get the age of the individual.
     *
     * @return The age
     */
    public int getAge() {
        return age;
    }

    /**
     * Returns, if the individual is already safe; that means: on save- oder exit cells.
     *
     * @return if the individual is already safe
     */
    public boolean isSafe() {
        return safe;
    }

    /**
     * Sets the safe-status of the individual.
     *
     * @param saveStatus indicates wheather the individual is save or not
     */
    public void setSafe(boolean saveStatus) {
        safe = saveStatus;
    }

    /**
     * Get the left reaction time of the individual.
     *
     * @return the left reaction time
     */
    public double getReactionTime() {
        return reactionTime;
    }

    /**
     * Alarms the Individual and also alarms the room of the cell of the individual.
     *
     * @param alarmed decides wheather the individual is alarmed, or if it is stopped being alarmed
     */
    public void setAlarmed(boolean alarmed) {
        this.alarmed = alarmed;
    }

    /**
     * Get the setAlarmed status of the individual.
     *
     * @return true if the individual is alarmed, false otherwise
     */
    public boolean isAlarmed() {
        return alarmed;
    }

    /**
     * Get the exhaustion of the individual.
     *
     * @return The exhaustion
     */
    public double getExhaustion() {
        return exhaustion;
    }

    /**
     * Set the exhaustion of the Individual to a specified value.
     *
     * @param val the exhaustion
     */
    public void setExhaustion(double val) {
        this.exhaustion = val;
    }

    /**
     * Returns the exchaustion factor of the {@code Individual}.
     *
     * @return the exhaustion factor
     */
    public double getExhaustionFactor() {
        return this.exhaustionFactor;
    }

    /**
     * Sets the exhaustion factor of the {@code Individual} to a specified value.
     *
     * @param val the exhaustion factor
     */
    public void setExhaustionFactor(double val) {
        this.exhaustionFactor = val;
    }

    /**
     * Get the familiarity of the individual.
     *
     * @return The familiarity
     */
    public double getFamiliarity() {
        return familiarity;
    }

    /**
     * Set the familiarity of the individual.
     *
     * @param val the familiarity value
     */
    public void setFamiliarity(double val) {
        this.familiarity = val;
    }

    /**
     * Returns the identifier of this individual.
     *
     * @return the number
     */
    public int getNumber() {
        return individualNumber;
    }

    /**
     * Sets the identification Number of the {@code Individual}.
     *
     * @param i the number
     */
    public void setNumber(int i) {
        individualNumber = i;
    }

    /**
     * Returns the identifier of this individual.
     *
     * @return the number
     */
    @Override
    public int id() {
        return individualNumber;
    }

    public Direction8 getDirection() {
        return dir;
    }

    public void setDirection(Direction8 dir) {
        this.dir = dir;
    }

    /**
     * Get the panic of the individual.
     *
     * @return The panic
     */
    public double getPanic() {
        return panic;
    }

    public void setPanic(double val) {
        this.panic = val;
    }

    public double getPanicFactor() {
        return panicFactor;
    }

    /**
     * Set the panic-factor of the individual.
     *
     * @param val
     */
    public void setPanicFactor(double val) {
        this.panicFactor = val;
    }

    /**
     * Get the slackness of the individual.
     *
     * @return The slackness
     */
    public double getSlackness() {
        return slackness;
    }

    /**
     * Set the slackness of the individual.
     *
     * @param val
     */
    public void setSlackness(double val) {
        this.slackness = val;
    }

    /**
     * Set the relativeMaxSpeed of the individual.
     *
     * @param maxSpeed the maximal speed
     */
    public void setMaxSpeed(double maxSpeed) {
        this.relativeMaxSpeed = maxSpeed;
    }

    /**
     * Returns the relativeMaxSpeed of the individual.
     *
     * @return The relativeMaxSpeed
     */
    public double getMaxSpeed() {
        return relativeMaxSpeed;
    }

    /**
     * Set the current relative speed of the individual. The relative speed is a percentage of the maximum speed .
     *
     * @param relativeSpeed the new speed
     */
    public void setRelativeSpeed(double relativeSpeed) {
        this.relativeSpeed = relativeSpeed;
    }

    /**
     * Returns the current relative speed of the individual. The relativity is with respect to the individuals max
     * speed.
     *
     * @return the current speed
     */
    public double getRelativeSpeed() {
        return relativeSpeed;
    }

    /**
     * Set the {@link ds.ca.EvacCell} on which the {@code Individual} stands.
     *
     * @param c the cell
     */
    public void setCell(EvacCell c) {
        this.cell = c;
    }

    /**
     * Returns the {@link ds.ca.EvacCell} on which the {@code Individual} stands.
     *
     * @return The EvacCell
     */
    public EvacCell getCell() {
        return cell;
    }

    /**
     * Set the dynamicPotential of the individual.
     *
     * @param dp
     */
    public void setDynamicPotential(DynamicPotential dp) {
        this.dynamicPotential = dp;
    }

    /**
     * Get the dynamicPotential of the individual.
     *
     * @return The dynamicPotential
     */
    public DynamicPotential getDynamicPotential() {
        return dynamicPotential;
    }

    /**
     * Set the staticPotential of the individual.
     *
     * @param sp
     */
    public void setStaticPotential(StaticPotential sp) {
        this.staticPotential = sp;
    }

    /**
     * Get the staticPotential of the individual.
     *
     * @return The staticPotential
     */
    public StaticPotential getStaticPotential() {
        return staticPotential;
    }

    public void die(DeathCause cause) {
        this.deathCause = cause;
        isDead = true;
    }

    public boolean isDead() {
        return isDead;
    }

    /**
     * Returns a copy of itself as a new Object.
     * @return 
     */
    @Override
    public Individual clone() {
        Individual aClone = new Individual();
        aClone.absoluteMaxSpeed = this.absoluteMaxSpeed;
        aClone.age = this.age;
        aClone.cell = this.cell;
        aClone.relativeSpeed = this.relativeSpeed;
        aClone.deathCause = this.deathCause;
        aClone.dynamicPotential = this.dynamicPotential;
        aClone.exhaustion = this.exhaustion;
        aClone.exhaustionFactor = this.exhaustionFactor;
        aClone.familiarity = this.familiarity;
        aClone.individualNumber = this.individualNumber;
        aClone.alarmed = this.alarmed;
        aClone.isEvacuated = this.isEvacuated;
        aClone.safe = this.safe;
        aClone.relativeMaxSpeed = this.relativeMaxSpeed;
        aClone.panic = this.panic;
        aClone.panicFactor = this.panicFactor;
        aClone.reactionTime = this.reactionTime;
        aClone.safetyTime = this.safetyTime;
        aClone.slackness = this.slackness;
        aClone.staticPotential = this.staticPotential;
        aClone.stepEndTime = this.stepEndTime;
        aClone.stepStartTime = this.stepStartTime;
        aClone.uid = this.uid;
        return aClone;
    }

    /**
     * Returns a string "Individual" and the id number of the individual.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return "Individual " + id();
    }

    /**
     * Returns a string containing all parameters of the individueal, such as familiarity, exhaustion etc.
     *
     * @return the property string
     */
    public String toStringProperties() {
        return "Familiarity: " + familiarity + "\n"
                + "Panic: " + panic + "\n"
                + "Panic factor: " + panicFactor + "\n"
                + "Slackness: " + slackness + "\n"
                + "Exhaustion: " + exhaustion + "\n"
                + "Exhaustion factor: " + exhaustionFactor + "\n"
                + "MaxSpeed: " + relativeMaxSpeed + "\n"
                + "Absolute max speed: " + absoluteMaxSpeed;
    }

    /**
     * The hashcode of individuals is their id numer.
     *
     * @return the hashcode of the individual
     */
    @Override
    public int hashCode() {
        return getNumber();
    }

    /**
     * Two individuals are equal, if they have both the same id.
     *
     * @param o the reference object with which to compare.
     * @return {@code true} if this object is the same as {@code o}; {@code false} otherwise.
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof Individual ? ((Individual) o).id() == id() : false;
    }

    /////////////////////////////////////////////////////////////////////////////
    // alter stuff. todo ändern
    public UUID getUid() {
        return uid;
    }

    public void setUid(UUID uid) {
        this.uid = uid;
    }
}
