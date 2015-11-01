package org.zet.cellularautomaton.algorithm.rule;

import java.util.LinkedList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import org.jmock.Mockery;
import static org.junit.Assert.assertThat;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.RoomCell;
import org.zet.cellularautomaton.algorithm.EvacuationSimulationProblem;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestAbstractEvacuationRule {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private final Mockery context = new Mockery();

    @Test
    public void testExecutableIfOccupied() {
        AbstractEvacuationRule rule = new AbstractEvacuationRule() {

            @Override
            protected void onExecute(EvacCell cell) {
            }
        };
        RoomCell cell = new RoomCell(0, 0);
        assertThat(rule.executableOn(cell), is(false));

        Individual i = new Individual();
        cell.getState().setIndividual(i);
        assertThat(rule.executableOn(cell), is(true));
    }
    
    @Test
    public void executesIfApplicable() {
        final LinkedList<EvacCell> executedOn = new LinkedList<>();
        AbstractEvacuationRule rule = new AbstractEvacuationRule() {

            @Override
            protected void onExecute(EvacCell cell) {
                executedOn.add(cell);
            }

            @Override
            public boolean executableOn(EvacCell cell) {
                return true;
            }
        };
        RoomCell cell = new RoomCell(0, 0);
        rule.execute(cell);
        assertThat(executedOn, hasItem(cell));
        assertThat(executedOn.size(), is(equalTo(1)));
    }
    
    @Test
    public void executesNot() {
        AbstractEvacuationRule rule = new AbstractEvacuationRule() {

            @Override
            protected void onExecute(EvacCell cell) {
                throw new AssertionError("onExecute called!");
            }

            @Override
            public boolean executableOn(EvacCell cell) {
                return false;
            }
        };
        rule.execute(new RoomCell(0, 0));
    }

    @Test
    public void testSettingCellularAutomatonFails() {
        EvacuationSimulationProblem p = context.mock(EvacuationSimulationProblem.class);
        AbstractEvacuationRule rule = new AbstractEvacuationRule() {

            @Override
            protected void onExecute(EvacCell cell) {
            }
        };
        rule.setEvacuationSimulationProblem(p);
        assertThat(rule.esp, is(equalTo(p)));

        exception.expect(RuntimeException.class);
        rule.setEvacuationSimulationProblem(p);
    }
    
    @Test(expected = NullPointerException.class)
    public void testEvacuationSimulationProblemNotNull() {
        AbstractEvacuationRule rule = new AbstractEvacuationRule() {

            @Override
            protected void onExecute(EvacCell cell) {
            }
        };
        rule.setEvacuationSimulationProblem(null);
    }
}
