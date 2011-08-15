package grammar;

public interface RuleVisitor {

	// Perform an action on the rule and return a bool
	public boolean action(Rule r);
}
