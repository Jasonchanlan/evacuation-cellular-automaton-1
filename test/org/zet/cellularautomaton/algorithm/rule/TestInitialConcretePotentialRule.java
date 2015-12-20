package org.zet.cellularautomaton.algorithm.rule;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.jmock.AbstractExpectations.any;
import static org.jmock.AbstractExpectations.returnValue;
import static org.jmock.AbstractExpectations.same;
import static org.zet.cellularautomaton.algorithm.rule.RuleTestMatchers.executeableOn;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;
import org.zet.cellularautomaton.DeathCause;
import org.zet.cellularautomaton.EvacCell;
import org.zet.cellularautomaton.EvacuationCellularAutomaton;
import org.zet.cellularautomaton.Individual;
import org.zet.cellularautomaton.Room;
import org.zet.cellularautomaton.RoomCell;
import org.zet.cellularautomaton.potential.PotentialManager;
import org.zet.cellularautomaton.potential.StaticPotential;
import org.zet.cellularautomaton.statistic.CAStatisticWriter;

/**
 *
 * @author Jan-Philipp Kappmeier
 */
public class TestInitialConcretePotentialRule {
    private final Mockery context = new Mockery();
    InitialConcretePotentialRule rule;
    EvacCell cell;
    Individual i;
    EvacuationCellularAutomaton eca;
    EvacuationState es;
    private final static CAStatisticWriter statisticWriter = new CAStatisticWriter();
    
    @Before
    public void init() {
        rule = new InitialConcretePotentialRule();
        Room room = context.mock(Room.class);
        es = context.mock(EvacuationState.class);
        eca = new EvacuationCellularAutomaton();
        i = new Individual();
        context.checking(new Expectations() {
            {
                allowing(es).getCellularAutomaton();
                will(returnValue(eca));
                allowing(room).getID();
                will(returnValue(1));
                allowing(room).addIndividual(with(any(EvacCell.class)), with(i));
                allowing(room).removeIndividual(with(i));
                allowing(es).getStatisticWriter();
                will(returnValue(statisticWriter));
            }
        });
        cell = new RoomCell(1, 0, 0, room);
        i.setCell(cell);
        cell.getState().setIndividual(i);

        rule.setEvacuationSimulationProblem(es);
        eca.addIndividual(cell, i);
    }
    
    @Test
    public void testAppliccableIfNotEmpty() {
        cell = new RoomCell(0, 0);
        assertThat(rule, is(not(executeableOn(cell))));

        Individual i = new Individual();
        cell.getState().setIndividual(i);
        assertThat(rule, is(executeableOn(cell)));
    }
    
    @Test
    public void testNotApplicableIfPotentialSet() {
        StaticPotential sp = new StaticPotential();
        i.setStaticPotential(sp);
        assertThat(rule, is(not(executeableOn(cell))));
    }
    
    @Test
    public void testDeadIfNoPotentials() {
        context.checking(new Expectations() {
            {
                
                allowing(es).setIndividualDead(with(i), with(DeathCause.EXIT_UNREACHABLE));
            }
        });
        rule.execute(cell);
    }
    
    @Test
    public void testDeadIfPotentialsBad() {
        StaticPotential sp = new StaticPotential();
        PotentialManager pm = eca.getPotentialManager();

        pm.addStaticPotential(sp);
        context.checking(new Expectations() {
            {
                
                allowing(es).setIndividualDead(with(i), with(DeathCause.EXIT_UNREACHABLE));
            }
        });
        rule.execute(cell);
    }
    
    @Test
    public void testSinglePotentialTaken() {
        StaticPotential sp = new StaticPotential();
        PotentialManager pm = eca.getPotentialManager();
        sp.setPotential(cell, 1);

        pm.addStaticPotential(sp);
        
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(sp)));
    }
    
    @Test
    public void testHighFamiliarityChoosesBest() {
        StaticPotential targetPotential = initFamiliarPotential( eca.getPotentialManager());
        
        i.setFamiliarity(1);
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(targetPotential)));
    }
    
    @Test
    public void testLowFamiliarityChoosesAttractive() {
        StaticPotential targetPotential = initUnfamiliarPotential( eca.getPotentialManager());
        
        i.setFamiliarity(0);
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(targetPotential)));
    }
    
    @Test
    public void testMediumFamiliarity() {
        StaticPotential targetPotential = initMediumPotential( eca.getPotentialManager());
        
        i.setFamiliarity(0.5);
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(targetPotential)));
    }
    
    @Test
    public void testAttractivePotentialShort() {
        StaticPotential targetPotential = initAttractiveShortPotential( eca.getPotentialManager());
        
        i.setFamiliarity(0.5);
        rule.execute(cell);
        assertThat(i.isDead(), is(false));
        assertThat(i.getDeathCause(), is(nullValue()));
        assertThat(i.getStaticPotential(), is(same(targetPotential)));
    }
    
    private StaticPotential initFamiliarPotential(PotentialManager pm) {
        StaticPotential shortDistance = new StaticPotential();
        StaticPotential mediumDistance = new StaticPotential();
        StaticPotential longDistance = new StaticPotential();
        shortDistance.setPotential(cell, 1);
        mediumDistance.setPotential(cell, 2);
        longDistance.setPotential(cell, 3);
        
        shortDistance.setAttractivity(10);
        mediumDistance.setAttractivity(50);
        longDistance.setAttractivity(100);

        pm.addStaticPotential(longDistance);
        pm.addStaticPotential(shortDistance);
        pm.addStaticPotential(mediumDistance);
        return shortDistance;
    }

    private StaticPotential initUnfamiliarPotential(PotentialManager pm) {
        StaticPotential shortDistance = new StaticPotential();
        StaticPotential mediumDistance = new StaticPotential();
        StaticPotential longDistance = new StaticPotential();
        shortDistance.setPotential(cell, 1);
        mediumDistance.setPotential(cell, 2);
        longDistance.setPotential(cell, 3);
        shortDistance.setAttractivity(10);
        mediumDistance.setAttractivity(50);
        longDistance.setAttractivity(100);

        pm.addStaticPotential(longDistance);
        pm.addStaticPotential(shortDistance);
        pm.addStaticPotential(mediumDistance);
        return longDistance;
    }

    private StaticPotential initMediumPotential(PotentialManager pm) {
        StaticPotential shortDistance = new StaticPotential();
        StaticPotential mediumDistance = new StaticPotential();
        StaticPotential longDistance = new StaticPotential();
        shortDistance.setPotential(cell, 1);
        mediumDistance.setPotential(cell, 2);
        longDistance.setPotential(cell, 3);
        shortDistance.setAttractivity(10);
        mediumDistance.setAttractivity(50);
        longDistance.setAttractivity(100);

        pm.addStaticPotential(longDistance);
        pm.addStaticPotential(shortDistance);
        pm.addStaticPotential(mediumDistance);
        return mediumDistance;
    }

    private StaticPotential initAttractiveShortPotential(PotentialManager pm) {
        StaticPotential shortDistance = new StaticPotential();
        StaticPotential mediumDistance = new StaticPotential();
        StaticPotential longDistance = new StaticPotential();
        shortDistance.setPotential(cell, 1);
        mediumDistance.setPotential(cell, 2);
        longDistance.setPotential(cell, 3);
        shortDistance.setAttractivity(1000);
        mediumDistance.setAttractivity(50);
        longDistance.setAttractivity(100);

        pm.addStaticPotential(longDistance);
        pm.addStaticPotential(shortDistance);
        pm.addStaticPotential(mediumDistance);
        return shortDistance;
    }
}
