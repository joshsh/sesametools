package net.fortytwo.sesametools;

import org.eclipse.rdf4j.model.Statement;

import java.util.Comparator;

/**
 * Implements a Comparator for OpenRDF Statements
 * using the order Subject-&gt;Predicate-&gt;Object
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class ContextInsensitiveStatementComparator implements Comparator<Statement> {
    public final static int BEFORE = -1;
    public final static int EQUAL = 0;
    public final static int AFTER = 1;

    @Override
    public int compare(Statement first, Statement second) {
        if (first == second) {
            return EQUAL;
        }

        if (first.getSubject().equals(second.getSubject())) {
            if (first.getPredicate().equals(second.getPredicate())) {
                if (first.getObject().equals(second.getObject())) {
                    return EQUAL;
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
