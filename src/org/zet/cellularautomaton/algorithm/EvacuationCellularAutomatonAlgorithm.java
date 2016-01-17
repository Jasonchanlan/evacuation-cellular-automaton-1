package org.zet.cellularautomaton.algorithm;

import static org.zetool.common.util.Helper.in;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.logging.Level;
import org.zet.cellularautomaton.algorithm.rule.EvacuationRule;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.algo.ca.util.IndividualDistanceComparator;
import org.zet.cellularautomaton.EvacuationCellularAutomaton;
import org.zet.cellularautomaton.EvacuationCellularAutomatonInterface;
import org.zet.cellularautomaton.algorithm.parameter.DefaultParameterSet;
import org.zet.cellularautomaton.statistic.results.StoredCAStatisticResults;
import org.zetool.algorithm.simulation.cellularautomaton.AbstractCellularAutomatonSimulationAlgorithm;

/**
 * An implementation of a general cellular automaton algorithm specialized for evacuation simulation. The cells of the
 * cellular automaton are populized by {@link Individual}s and the simulation is rulebased performed only on these
 * populated cells. The algorithm is itself abstract and implementations have to specify the order in which the rules
 * are executed for the populating individuals.
 *
 * @author Jan-Philipp Kappmeier
 */
public class EvacuationCellularAutomatonAlgorithm
        extends AbstractCellularAutomatonSimulationAlgorithm<EvacuationCellularAutomatonInterface, EvacCell, EvacuationSimulationProblem, EvacuationSimulationResult> {

    /**
     * The order in which the individuals are asked for.
     */
    public static final Function<List<Individual>, Iterator<Individual>> DEFAULT_ORDER = x -> x.iterator();

    /**
     * The distance comparator.
     */
    private final IndividualDistanceComparator DISTANCE_COMPARATOR = new IndividualDistanceComparator<>();
    /**
     * Sorts the individuals by increasing distance to the exit.
     */
//    public static final Function<List<Individual>, Iterator<Individual>> FRONT_TO_BACK = (List<Individual> t) -> {
//        List<Individual> copy = new ArrayList<>(t);
//        Collections.sort(copy, DISTANCE_COMPARATOR);
//        return copy.iterator();
//    };

    private static Function<List<Individual>, Iterator<Individual>> getFrontToBack(IndividualDistanceComparator c) {
        return (List<Individual> t) -> {
            List<Individual> copy = new ArrayList<>(t);
            Collections.sort(copy, c);
            return copy.iterator();
        };
    }

    /**
     * Sorts the individuals by decreasing distance to the exit.
     */
//    public static final Function<List<Individual>, Iterator<Individual>> BACK_TO_FRONT = (List<Individual> t) -> {
//        List<Individual> copy = new ArrayList<>(t);
//        Collections.sort(copy, DISTANCE_COMPARATOR);
//        Collections.reverse(copy);
//        return copy.iterator();
//    };

    private static Function<List<Individual>, Iterator<Individual>> getBackToFront(IndividualDistanceComparator c) {
        return (List<Individual> t) -> {
            List<Individual> copy = new ArrayList<>(t);
            Collections.sort(copy, c);
            Collections.reverse(copy);
            return copy.iterator();
        };
    }
    
    private static class MIndividualDistanceComparator extends IndividualDistanceComparator<Individual> {
        EvacuationCellularAutomatonAlgorithm algo;

        @Override
        public int compare(Individual i1, Individual i2) {
            setEs(algo.es);
            return super.compare(i1, i2);
        }
    }
    
    public static EvacuationCellularAutomatonAlgorithm getBackToFrontAlgorithm() {
        MIndividualDistanceComparator comparator = new MIndividualDistanceComparator();
        EvacuationCellularAutomatonAlgorithm algo = new EvacuationCellularAutomatonAlgorithm(getBackToFront(comparator));
        comparator.algo = algo;
        return algo;
    }
    
    public static EvacuationCellularAutomatonAlgorithm getFrontToBackAlgorithm() {
        MIndividualDistanceComparator comparator = new MIndividualDistanceComparator();
        EvacuationCellularAutomatonAlgorithm algo = new EvacuationCellularAutomatonAlgorithm(getFrontToBack(comparator));
        comparator.algo = algo;
        return algo;
    }

    /**
     * The ordering used in the evacuation cellular automaton.
     */
    private Function<List<Individual>, Iterator<Individual>> reorder;
    EvacuationState es = new MutableEvacuationState(new DefaultParameterSet(), new EvacuationCellularAutomaton(),
            Collections.EMPTY_LIST);
    EvacuationStateController ec = null;

    public EvacuationCellularAutomatonAlgorithm() {
        this(DEFAULT_ORDER);
    }

    public EvacuationCellularAutomatonAlgorithm(Function<List<Individual>, Iterator<Individual>> reorder) {
        this.reorder = reorder;
    }

    @Override
    protected void initialize() {
        initRulesAndState();
        DISTANCE_COMPARATOR.setEs(es);

        setMaxSteps(getProblem().getEvacuationStepLimit());
        log.log(Level.INFO, "{0} is executed. ", toString());

        getProblem().getCellularAutomaton().start();
        Individual[] individualsCopy = es.getIndividualState().getInitialIndividuals().toArray(
                new Individual[es.getIndividualState().getInitialIndividuals().size()]);
        for (Individual i : individualsCopy) {
            Iterator<EvacuationRule> primary = getProblem().getRuleSet().primaryIterator();
            EvacCell c = es.propertyFor(i).getCell();
            while (primary.hasNext()) {
                EvacuationRule r = primary.next();
                r.execute(c);
            }
        }
        es.removeMarkedIndividuals();
    }

    public void setNeededTime(int i) {
        es.setNeededTime(i);
    }

    private void initRulesAndState() {
        es = new MutableEvacuationState(getProblem().getParameterSet(), getProblem().getCellularAutomaton(),
                getProblem().getIndividuals());
        for (Map.Entry<Individual, EvacCell> e : getProblem().individualStartPositions().entrySet()) {
            es.propertyFor(e.getKey()).setCell(e.getValue());
        }
        ec = new EvacuationStateController((MutableEvacuationState) es);
        for (EvacuationRule r : getProblem().getRuleSet()) {
            r.setEvacuationState(es);
        }
    }

    @Override
    protected void performStep() {
        super.performStep();
        super.increaseStep();

        es.removeMarkedIndividuals();
        getProblem().getCellularAutomaton().updateDynamicPotential(
                getProblem().getParameterSet().probabilityDynamicIncrease(),
                getProblem().getParameterSet().probabilityDynamicDecrease());

        fireProgressEvent(getProgress(), String.format("%1$s von %2$s individuals evacuated.",
                es.getIndividualState().getInitialIndividualCount() - es.getIndividualState().getRemainingIndividualCount(),
                es.getIndividualState().getInitialIndividualCount()));
    }

    @Override
    protected final void execute(EvacCell cell) {
        Individual i = Objects.requireNonNull(cell.getState().getIndividual(),
                "Execute called on EvacCell that does not contain an individual!");
        for (EvacuationRule r : in(getProblem().getRuleSet().loopIterator())) {
            r.execute(es.propertyFor(i).getCell());
        }
    }

    @Override
    protected EvacuationSimulationResult terminate() {
        // let die all individuals which are not already dead and not safe
        if (es.getIndividualState().getNotSafeIndividualsCount() != 0) {
            Individual[] individualsCopy = es.getIndividualState().getRemainingIndividuals().toArray(
                    new Individual[es.getIndividualState().getRemainingIndividuals().size()]);
            for (Individual i : individualsCopy) {
                if (!es.getIndividualState().isSafe(es.propertyFor(i).getCell().getState().getIndividual())) {
                    ec.die(i, DeathCause.NOT_ENOUGH_TIME);
                }
            }
        }
        fireProgressEvent(1, "Simulation complete.");

        EvacuationSimulationProblem p = getProblem();
        p.getCellularAutomaton().stop();
        log("Time steps: " + getStep());
        return new EvacuationSimulationResult(getStep());
    }

    public StoredCAStatisticResults getStatisticResults() {
        return es.getStatisticWriter().getStoredCAStatisticResults();
    }

    @Override
    protected boolean isFinished() {
        boolean thisFinished = allIndividualsSave() && timeOver();
        return super.isFinished() || thisFinished;
    }

    private boolean allIndividualsSave() {
        return es.getIndividualState().getNotSafeIndividualsCount() == 0;
    }

    private boolean timeOver() {
        return getStep() > es.getNeededTime();
    }

    /**
     * Sends a progress event. The progress is defined as the maximum of the percentage of already evacuated individuals
     * and the fraction of time steps of the maximum amount of time steps already simulated.
     *
     * @return the current progress as percentage of safe individuals
     */
    @Override
    protected final double getProgress() {
        double timeProgress = super.getProgress();
        double individualProgress = 1.0 - ((double) es.getIndividualState().getRemainingIndividualCount()
                / getProblem().getIndividuals().size());
        return Math.max(individualProgress, timeProgress);
    }

    /**
     * An iterator that iterates over all cells of the cellular automaton that contains an individual. The rules of the
     * simulation algorithm are being executed on each of the occupied cells.
     *
     * @return iterator of all occupied cells
     */
    @Override
    public final Iterator<EvacCell> iterator() {
        return new CellIterator(reorder.apply(es.getIndividualState().getRemainingIndividuals()), es);
    }

    /**
     * A simple iterator that iterates over all cells of the cellular automaton that contain an individual. The
     * iteration order equals the order of the individuals given.
     */
    private static class CellIterator implements Iterator<EvacCell> {

        private final Iterator<Individual> individuals;
        private final EvacuationState es;

        /**
         * Initializes the object with a list of individuals whose cells are iterated over.
         *
         * @param individuals the individuals
         */
        private CellIterator(Iterator<Individual> individuals, EvacuationState es) {
            this.individuals = Objects.requireNonNull(individuals, "Individuals list must not be null.");
            this.es = es;
        }

        @Override
        public boolean hasNext() {
            return individuals.hasNext();
        }

        @Override
        public EvacCell next() {
            return es.propertyFor(individuals.next()).getCell();
        }

        @Override
        public void remove() {
            throw new AssertionError("Attempted cell removal.");
        }
    }

    public EvacuationState getEvacuationState() {
        return es;
    }

}
