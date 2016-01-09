package org.zet.cellularautomaton.algorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.Individual;

/**
 * Collects the state of each individual in the simulation.
 *
 * @author Jan-Philipp Kappmeier
 */
public class IndividualState {
    private static final String ERROR_NOT_IN_LIST = "Specified individual is not in list individuals.";

    private final Set<Individual> individuals = new HashSet<>();
    private final Set<Individual> deadIndividuals = new HashSet<>();
    private final Set<Individual> safeIndividuals = new HashSet<>();
    /** An ArrayList of all Individual objects, which are already out of the simulation because they are evacuated. */
//    private List<Individual> evacuatedIndividuals;
    private final Set<Individual> evacuatedIndividuals = new HashSet<>();
    private final Map<Individual, DeathCause> deathCause = new HashMap<>();

    void addIndividual(Individual i) {
        individuals.add(i);
    }


    /**
     * Returns a list of all individuals that are active in the simulation. The list does not contain individuals
     * which are dead or evacuated. Safe individuals are contained in the list.
     * 
     * @return list of active individuals
     */
    public List<Individual> getRemainingIndividuals() {
        List<Individual> remaining = new ArrayList<>(individuals.size() - evacuatedIndividuals.size());

        individuals.stream().filter(i -> !(isEvacuated(i) || isDead(i))).forEach(i -> remaining.add(i));

        return Collections.unmodifiableList(remaining);
    }

    
    /**
     * Indicates, if the individual is already safe; that means: on save or exit cells.
     */
    /**
     * Returns, if the individual is already safe; that means: on save- oder exit cells.
     *
     * @param i indicates wheather the individual is save or not
     * @return if the individual is already safe
     */
    public boolean isSafe(Individual i) {
        return safeIndividuals.contains(i);
    }

    /**
     * Sets the safe-status of the individual.
     *
     * @param i indicates wheather the individual is save or not
     */
    public void setSafe(Individual i) {
        safeIndividuals.add(i);
    }

    /**
     * Removes an individual from the list of all individuals of the building and adds it to the list of individuals,
     * which are out of the simulation because the are evacuated.
     *
     * @throws java.lang.IllegalArgumentException if the the specific individual does not exist in the list individuals
     * @param i specifies the Individual object which has to be removed from the list and added to the other list
     */
    public void setIndividualEvacuated(Individual i) {
        if (!individuals.remove(i)) {
            throw new IllegalArgumentException(ERROR_NOT_IN_LIST);
        }

        evacuatedIndividuals.add(i);
        //notSaveIndividualsCount--;
    }

    /**
     * Returns true, if the person is evacuated, false elsewise.
     *
     * @param i the individual
     * @return the evacuation status
     */
    public boolean isEvacuated(Individual i) {
        return evacuatedIndividuals.contains(i);
    }


    /**
     * Returns a view of all evacuated individuals.
     *
     * @return the view
     */
    public Collection<Individual> getEvacuatedIndividuals() {
        return Collections.unmodifiableSet(evacuatedIndividuals);
    }

    public int evacuatedIndividualsCount() {
        return evacuatedIndividuals.size();
    }

    public void die(Individual i, DeathCause cause) {
        deadIndividuals.add(i);
        deathCause.put(i, cause);
    }

    public boolean isDead(Individual i) {
        return deadIndividuals.contains(i);
    }

    /**
     * Returns the {@link DeathCause} of an individual.
     *
     * @param i the individual
     * @return the cause
     */
    public DeathCause getDeathCause(Individual i) {
        return deathCause.get(i);
    }

    public int deadIndividualsCount() {
        return deadIndividuals.size();
    }

    /**
     * Returns a view of all dead individuals.
     *
     * @return the view
     */
    public Collection<Individual> getDeadIndividuals() {
        return Collections.unmodifiableSet(deadIndividuals);
    }

    /**
     * Calculates the number of individuals that died by a specified death cause.
     *
     * @param deathCause the death cause
     * @return the number of individuals died by the death cause
     */
    public int getDeadIndividualCount(DeathCause deathCause) {
        int count = 0;
        count = getDeadIndividuals().stream().filter(i -> getDeathCause(i) == deathCause).
                map((_item) -> 1).reduce(count, Integer::sum);
        return count;
    }

}