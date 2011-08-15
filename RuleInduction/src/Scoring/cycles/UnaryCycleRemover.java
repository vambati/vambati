package Scoring.cycles;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.StrongConnectivityInspector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DirectedSubgraph;

import Scoring.FeatureManager;
import Scoring.RuleException;
import Scoring.ScoreableRule;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;

public class UnaryCycleRemover {

	private DirectedGraph<String, ScoreableRule> g =
			new DefaultDirectedGraph<String, ScoreableRule>(ScoreableRule.class);
	private FeatureManager fman;
	
	public UnaryCycleRemover(FeatureManager fman) {
		this.fman = fman;
	}

	public void addUnaryRule(ScoreableRule rule) throws RuleException {

		if (rule.getSourceAntecedents().length != 1) {
			throw new RuntimeException("Expected a unary rule: " + rule.toString());
		}

		String consequent = ScoreableRule.getLabelfromConsequent(rule.getConsequent());
		String antecedent = ScoreableRule.getLabelfromAntecedent(rule.getSourceAntecedents()[0]);
		g.addVertex(consequent);
		g.addVertex(antecedent);
		g.addEdge(consequent, antecedent, rule);
	}

	public Set<ScoreableRule> getRulesForRemoval() {

		Set<ScoreableRule> rulesToRemove = new HashSet<ScoreableRule>();

		Iterator<DirectedSubgraph<String, ScoreableRule>> cycles = getCycles();
		while (cycles.hasNext()) {

			// deal with one cycle per iteration
			DirectedSubgraph<String, ScoreableRule> cycle = cycles.next();

			if (cycle.edgeSet().size() == 0) {
				throw new RuntimeException("At least one rule must be in edge set. Internal error.");
			}

			// remove the edge with lowest frequency

			Ordering<ScoreableRule> ordering = new Ordering<ScoreableRule>() {
				public int compare(ScoreableRule rule1, ScoreableRule rule2) {
					float freq1 = rule1.getCount(fman);
					float freq2 = rule2.getCount(fman);
					return Float.compare(freq1, freq2);
				}
			};
			ScoreableRule minFreqRule = ordering.min(cycle.edgeSet());
			float minFreq = minFreqRule.getCount(fman);

			System.err.println("Removing " + minFreqRule + " with frequency " + minFreq);
			g.removeEdge(minFreqRule);
			rulesToRemove.add(minFreqRule);

			cycles = getCycles();
		}

		return rulesToRemove;
	}

	private Iterator<DirectedSubgraph<String, ScoreableRule>> getCycles() {

		StrongConnectivityInspector<String, ScoreableRule> cycleDetector =
				new StrongConnectivityInspector<String, ScoreableRule>(g);
		List<DirectedSubgraph<String, ScoreableRule>> stronglyConnectedComponents =
				cycleDetector.stronglyConnectedSubgraphs();

		// identify cycles within connected components
		Predicate<DirectedSubgraph<String, ScoreableRule>> cycleIdentifier =
				new Predicate<DirectedSubgraph<String, ScoreableRule>>() {
					public boolean apply(DirectedSubgraph<String, ScoreableRule> subgraph) {
						return (subgraph.edgeSet().size() > 1);
					}
				};

		Iterable<DirectedSubgraph<String, ScoreableRule>> cycles =
				Iterables.filter(stronglyConnectedComponents, cycleIdentifier);

		return cycles.iterator();
	}
}
