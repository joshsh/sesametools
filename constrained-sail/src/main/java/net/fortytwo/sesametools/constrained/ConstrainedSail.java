
package net.fortytwo.sesametools.constrained;

import org.openrdf.sail.Sail;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.openrdf.sail.helpers.SailWrapper;
import org.openrdf.query.Dataset;
import org.openrdf.model.URI;

import net.fortytwo.sesametools.SimpleDatasetImpl;

/**
 * A StackableSail which is constrained in reading and writing triples by a pair
 * of Dataset objects.  A connection may only read statements from the set of
 * named graphs in the readable Dataset, and may only write statements to the
 * named graphs in the writeable Dataset.
 * <p/>
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class ConstrainedSail extends SailWrapper {

    private final URI defaultWriteContext;
    private final boolean hideNonWritableContexts;
    protected final Dataset readableSet;
    protected final Dataset writableSet;

    /**
     * Constructor.
     *
     * @param baseSail                a Sail upon which to stack this ACLSail
     *                                is to be controlled
     * @param readableSet             the Dataset from which this Sail may read
     * @param writableSet             the Dataset to which this Sail may write
     * @param defaultWriteContext     a context to which statements will be written
     *                                when no other context is specified.  May be null, in which case no
     *                                statements will be written unless a context is specified.
     * @param hideNonWritableContexts if true, the context information will be
     *                                removed from those statements returned by a generic (context-free)
     *                                getStatements query whenever the context in question is not writable.
     *                                This is a way of letting the user know which statements can be deleted.
     */
    public ConstrainedSail(final Sail baseSail,
                           final Dataset readableSet,
                           final Dataset writableSet,
                           final URI defaultWriteContext,
                           final boolean hideNonWritableContexts) {
        super(baseSail);
        this.readableSet = readableSet;
        this.writableSet = writableSet;
        this.defaultWriteContext = defaultWriteContext;
        this.hideNonWritableContexts = hideNonWritableContexts;
    }

    public ConstrainedSail(final Sail baseSail,
                           final URI defaultWriteContext,
                           final boolean hideNonWritableContexts) {
        this(baseSail, new SimpleDatasetImpl(), new SimpleDatasetImpl(), defaultWriteContext, hideNonWritableContexts);
    }

    public void addReadableGraph(final URI g) {
        //System.out.println("adding readable analysis: " + g);
        readableSet.getDefaultGraphs().add(g);
    }

    public void addWritableGraph(final URI g) {
        //System.out.println("adding writable analysis: " + g);
        writableSet.getDefaultGraphs().add(g);
    }

    public SailConnection getConnection() throws SailException {
        // For now, use reasonable defaults for namespace and null context access.
        return new ConstrainedSailConnection(getBaseSail().getConnection(),
                getValueFactory(),
                readableSet,
                writableSet,
                defaultWriteContext,
                true, true, hideNonWritableContexts);
    }

    public URI getDefaultWriteContext() {
        return defaultWriteContext;
    }

    public void initialize() throws SailException {
    }

    /**
     * This Sail is writable if the base Sail is writable, and insofar as the
     * the statements to be written are permitted by the access control
     * mechanism.
     */
    public boolean isWritable() throws SailException {
        return getBaseSail().isWritable()
                && (writableSet.getNamedGraphs().size() > 0);  // TODO: OR the default analysis is writable
    }

    public void shutDown() throws SailException {
    }
}
