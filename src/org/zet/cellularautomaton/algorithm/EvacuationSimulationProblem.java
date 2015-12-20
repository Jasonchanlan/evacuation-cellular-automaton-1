package org.zet.cellularautomaton.algorithm;

import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.EvacuationCellularAutomatonInterface;
import org.zet.cellularautomaton.algorithm.parameter.ParameterSet;
import org.zetool.algorithm.simulation.cellularautomaton.CellularAutomatonSimulationProblem;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public interface EvacuationSimulationProblem extends CellularAutomatonSimulationProblem<EvacuationCellularAutomatonInterface, EvacCell> {

    public ParameterSet getParameterSet();

    public PotentialController getPotentialController();

    public EvacuationRuleSet getRuleSet();
    
    /**
     * The maximal time linit in steps
     * @return the maximal time linit in seconds
     */
    public int getEvacuationStepLimit();
    
}
