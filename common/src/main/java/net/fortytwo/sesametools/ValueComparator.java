package net.fortytwo.sesametools;

import org.openrdf.model.BNode;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;

import java.util.Comparator;

/**
 * Implements a Comparator for OpenRDF Value objects where
 * the order for Values is:
 * <ol>
 * <li> Blank Node's </li>
 * <li> URI's </li>
 * <li> Literals </li>
 * </ol>
 * <p/>
 * with null Values sorted before others
 *
 * @author Peter Ansell p_ansell@yahoo.com
 */
public class ValueComparator implements Comparator<Value> {

    /**
     * A thread-safe pre-instantiated instance of ValueComparator.
     */
    private final static ValueComparator INSTANCE = new ValueComparator();

    /**
     * A thread-safe pre-instantiated instance of ValueComparator.
     */
    public final static ValueComparator getInstance() {
        return INSTANCE;
    }

    public final static int BEFORE = -1;
    public final static int EQUALS = 0;
    public final static int AFTER = 1;

    /**
     * Sorts in the order nulls&gt;BNodes&gt;URIs&gt;Literals
     * <p>
     * This is due to the fact that nulls are only applicable to contexts,
     * and according to the OpenRDF documentation, the type of the null
     * cannot be sufficiently distinguished from any other Value to make
     * an intelligent comparison to other Values:
     * </p>
     * <p>
     * BNodes are sorted according to the lexical compare of their identifiers,
     * which provides a way to sort statements with the same BNodes in the same positions, near each other
     * BNode sorting is not specified across sessions
     * </p>
     */
    @Override
    public int compare(Value first, Value second) {
        if (first == null) {
            if (second == null) {
                return EQUALS;
            } else {
                return BEFORE;
            }
        } else if (second == null) {
            // always sort null Values before others, so if the second is null, but the first wasn't, sort the first after the second
            return AFTER;
        }

        if (first == second || first.equals(second)) {
            return EQUALS;
        }

        if (first instanceof BNode) {
            if (second instanceof BNode) {
                // if both are BNodes, sort based on the lexical value of the internal ID
                // Although this sorting is not guaranteed to be consistent across sessions,
                // it provides a consistent sorting of statements in every case
                // so that statements with the same BNode are sorted near each other
                return ((BNode) first).getID().compareTo(((BNode) second).getID());
            } else {
                return BEFORE;
            }
        } else if (second instanceof BNode) {
            // sort BNodes before other things, and first was not a BNode
            return AFTER;
        } else if (first instanceof URI) {
            if (second instanceof URI) {
                return ((URI) first).stringValue().compareTo(((URI) second).stringValue());
            } else {
                return BEFORE;
            }
        } else if (second instanceof URI) {
            // sort URIs before Literals
            return AFTER;
        }
        // they must both be Literal's, so sort based on the lexical value of the Literal
        else {
            Literal firstLiteral = (Literal) first;
            Literal secondLiteral = (Literal) second;
            int cmp = firstLiteral.getLabel().compareTo(secondLiteral.getLabel());

            if (EQUALS == cmp) {
                String firstLang = firstLiteral.getLanguage();
                String secondLang = secondLiteral.getLanguage();
                if (null != firstLang) {
                    if (null != secondLang) {
                        return firstLang.compareTo(secondLang);
                    } else {
                        return AFTER;
                    }
                } else if (null != secondLang) {
                    return BEFORE;
                }

                URI firstType = firstLiteral.getDatatype();
                URI secondType = secondLiteral.getDatatype();
                if (null == firstType) {
                    if (null == secondType) {
                        return EQUALS;
                    } else {
                        return BEFORE;
                    }
                } else if (null == secondType) {
                    return AFTER;
                } else {
                    return firstType.stringValue().compareTo(secondType.stringValue());
                }
            } else {
                return cmp;
            }
        }
    }
}
