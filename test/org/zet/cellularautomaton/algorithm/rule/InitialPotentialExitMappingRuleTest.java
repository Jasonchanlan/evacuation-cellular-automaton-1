package org.zet.cellularautomaton.algorithm.rule;

import org.zet.cellularautomaton.algorithm.state.EvacuationState;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.any;
import static org.jmock.AbstractExpectations.returnValue;
import static org.zet.cellularautomaton.algorithm.rule.RuleTestMatchers.executeableOn;

import java.util.LinkedList;
import java.util.List;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.States;
import org.junit.Before;
import org.junit.Test;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.EvacuationCellularAutomaton;
import org.zet.cellularautomaton.ExitCell;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.IndividualBuilder;
import org.zet.cellularautomaton.IndividualToExitMapping;
import org.zet.cellularautomaton.Room;
import org.zet.cellularautomaton.RoomCell;
import org.zet.cellularautomaton.algorithm.EvacuationSimulationProblem;
import org.zet.cellularautomaton.algorithm.state.IndividualProperty;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.statistic.CAStatisticWriter;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class InitialPotentialExitMappingRuleTest {

    private final Mockery context = new Mockery();
    private States testState;
    InitialPotentialExitMappingRule rule;
    EvacCell cell;
    Individual i;
    private IndividualProperty ip;
    EvacuationCellularAutomaton eca;
    EvacuationSimulationProblem esp;
    ExitCell target;
    EvacuationState es;
    private final static IndividualBuilder builder = new IndividualBuilder();
    
    private final IndividualToExitMapping exitMapping = (Individual individual) -> {
        if (individual == InitialPotentialExitMappingRuleTest.this.i) {
            return target;
        }
        throw new IllegalStateException("Called with bad individual.");
    };

    @Before
    public void init() {
        rule = new InitialPotentialExitMappingRule();
        Room room = context.mock(Room.class);
        esp = context.mock(EvacuationSimulationProblem.class);
        es = context.mock(EvacuationState.class);
        eca = new EvacuationCellularAutomaton();
        i = builder.build();
        ip = new IndividualProperty(i);
        testState = context.states("normal-test");
        testState.become("normal-test");
        context.checking(new Expectations() {
            {
                allowing(es).getCellularAutomaton();
                will(returnValue(eca));
                allowing(room).getID();
                will(returnValue(1));
                allowing(room).addIndividual(with(any(EvacCell.class)), with(i));
                allowing(room).removeIndividual(with(i));
                allowing(es).getStatisticWriter();
                will(returnValue(new CAStatisticWriter(es)));
                allowing(es).propertyFor(i);
                will(returnValue(ip));
                allowing(es).getIndividualToExitMapping(); when(testState.is("normal-test"));
                will(returnValue(exitMapping));
            }
        });
        cell = new RoomCell(1, 0, 0, room);
        ip.setCell(cell);
        cell.getState().setIndividual(i);
        rule.setEvacuationState(es);

        target = new ExitCell(1.0, 0, 0, room);
    
    }

    @Test
    public void executableEvenIfPotentialAssigned() {
        StaticPotential sp = new StaticPotential();
        ip.setStaticPotential(sp);
        assertThat(rule, is(executeableOn(cell)));
    }

    @Test
    public void testAppliccableIfNotEmpty() {
        cell = new RoomCell(0, 0);
        assertThat(rule, is(not(executeableOn(cell))));

        Individual i = builder.build();
        cell.getState().setIndividual(i);
        assertThat(rule, is(executeableOn(cell)));
    }

    @Test
    public void assignedExitsAreAssigned() {
        
        StaticPotential sp = new StaticPotential();
        List<ExitCell> spExits = new LinkedList<>();
        spExits.add(target);
        sp.setAssociatedExitCells(spExits);

        eca.addStaticPotential(sp);
        
        rule.execute(cell);
        assertThat(ip.getStaticPotential(), is(equalTo(sp)));
    }


    @Test(expected = IllegalStateException.class)
    public void noPotentialForTargetFails() {
        rule.execute(cell);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void twoPotentialsForOneCellFails() {
        StaticPotential sp1 = new StaticPotential();
        List<ExitCell> sp1Exits = new LinkedList<>();
        sp1Exits.add(target);
        sp1.setAssociatedExitCells(sp1Exits);
        
        StaticPotential sp2 = new StaticPotential();
        List<ExitCell> sp2Exits = new LinkedList<>();
        sp2Exits.add(target);
        sp2.setAssociatedExitCells(sp2Exits);
        
        eca.addStaticPotential(sp1);
        eca.addStaticPotential(sp2);
        
        rule.execute(cell);
        assertThat(ip.getStaticPotential(), is(equalTo(sp2)));
    }
    
    @Test
    public void fallbackShortest() {
        StaticPotential shortDistance = new StaticPotential();
        StaticPotential longDistance = new StaticPotential();
        shortDistance.setPotential(cell, 1);
        longDistance.setPotential(cell, 2);
        
        testState.become("special-case");
        context.checking(new Expectations() {{
                IndividualToExitMapping mapping = _unused -> null;
                allowing(es).getIndividualToExitMapping(); when(testState.is("special-case"));
                will(returnValue(mapping));
            }});
        
        eca.addStaticPotential(longDistance);
        eca.addStaticPotential(shortDistance);
        
        rule.execute(cell);
        assertThat(ip.getStaticPotential(), is(equalTo(shortDistance)));
    }

}