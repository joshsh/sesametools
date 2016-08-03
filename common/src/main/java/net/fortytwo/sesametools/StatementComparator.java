package net.fortytwo.sesametools;

import org.eclipse.rdf4j.model.Statement;

import java.util.Comparator;

/**
 * Implements a Comparator for OpenRDF Statements
 * using the order Subject-&gt;Predicate-&gt;Object-&gt;Context
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class StatementComparator implements Comparator<Statement> {
    /**
     * A thread-safe pre-instantiated instance of StatementComparator.
     */
    private final static StatementComparator INSTANCE = new StatementComparator();

    /**
     * @return A thread-safe pre-instantiated instance of StatementComparator.
     */
    public static StatementComparator getInstance() {
        return INSTANCE;
    }

    public final static int BEFORE = -1;
    public final static int EQUAL = 0;
    public final static int AFTER = 1;

    @Override
    public int compare(Statement first, Statement second) {
        // Cannot use Statement.equals as it does not take Context into account,
        // but can check for reference equality (==)
        if (first == second) {
            return EQUAL;
        }

        if (first.getSubject().equals(second.getSubject())) {
            if (first.getPredicate().equals(second.getPredicate())) {
                if (first.getObject().equals(second.getObject())) {
                    // Context is the only part of a statement that should legitimately be null
                    if (first.getContext() == null) {
                        if (second.getContext() == null) {
                            return EQUAL;
                        } else {
                            return BEFORE;
                        }
                    } else if (second.getContext() == null) {
                        return AFTER;
                    } else {
                        return ValueComparator.getInstance().compare(first.getContext(), second.getContext());
                    }
                } else {
                    return ValueComparator.getInstance().compare(first.getObject(), second.getObject());
                }
            } else {
                return ValueComparator.getInstance().compare(first.getPredicate(), second.getPredicate());
            }
        } else {
            return ValueComparator.getInstance().compare(first.getSubject(), second.getSubject());
        }
    }

}
