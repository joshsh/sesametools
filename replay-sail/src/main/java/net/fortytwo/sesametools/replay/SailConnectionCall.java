
package net.fortytwo.sesametools.replay;

import net.fortytwo.sesametools.Formatting;
import net.fortytwo.sesametools.replay.calls.AddStatementCall;
import net.fortytwo.sesametools.replay.calls.BeginCall;
import net.fortytwo.sesametools.replay.calls.ClearCall;
import net.fortytwo.sesametools.replay.calls.ClearNamespacesCall;
import net.fortytwo.sesametools.replay.calls.CloseConnectionCall;
import net.fortytwo.sesametools.replay.calls.CloseIterationCall;
import net.fortytwo.sesametools.replay.calls.CommitCall;
import net.fortytwo.sesametools.replay.calls.ConstructorCall;
import net.fortytwo.sesametools.replay.calls.EvaluateCall;
import net.fortytwo.sesametools.replay.calls.GetContextIDsCall;
import net.fortytwo.sesametools.replay.calls.GetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.GetNamespacesCall;
import net.fortytwo.sesametools.replay.calls.GetStatementsCall;
import net.fortytwo.sesametools.replay.calls.HasNextCall;
import net.fortytwo.sesametools.replay.calls.NextCall;
import net.fortytwo.sesametools.replay.calls.RemoveCall;
import net.fortytwo.sesametools.replay.calls.RemoveNamespaceCall;
import net.fortytwo.sesametools.replay.calls.RemoveStatementsCall;
import net.fortytwo.sesametools.replay.calls.RollbackCall;
import net.fortytwo.sesametools.replay.calls.SetNamespaceCall;
import net.fortytwo.sesametools.replay.calls.SizeCall;
import org.openrdf.model.BNode;
import org.openrdf.model.IRI;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.SimpleValueFactory;
import org.openrdf.rio.ntriples.NTriplesUtil;
import org.openrdf.sail.SailException;

import java.util.StringTokenizer;

/**
 * @author Joshua Shinavier (http://fortytwo.net).
 */
public abstract class SailConnectionCall<T, R> {
    protected static final char DELIM = '\t';
    
    // Use a comma instead of whitespace so that lists of named graphs appear as a single token.
    private static final String COMMA = ",";

    private static ValueFactory valueFactory = SimpleValueFactory.getInstance();

    public enum Type {
        ADD_STATEMENT,
        BEGIN,
        CLEAR,
        CLEAR_NAMESPACES,
        CLOSE_CONNECTION,
        CLOSE_ITERATION,
        COMMIT,
        CONSTRUCT,
        EVALUATE,
        GET_CONTEXT_IDS,
        GET_NAMESPACE,
        GET_NAMESPACES,
        GET_STATEMENTS,
        HAS_NEXT,
        NEXT,
        REMOVE,
        REMOVE_NAMESPACE,
        REMOVE_STATEMENTS,
        ROLLBACK,
        SET_NAMESPACE,
        SIZE
    }

    protected String id;
    protected Type type;

    protected SailConnectionCall(final String id, final Type type) {
        this.id = id;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public Type getType() {
        return type;
    }

    public abstract R execute(T t) throws SailException;

    public static SailConnectionCall construct(final String s) {
        StringTokenizer tok = new StringTokenizer(s, "\t");
        String id = tok.nextToken();
        Type type = Type.valueOf(tok.nextToken());
        switch (type) {
            case ADD_STATEMENT:
                return new AddStatementCall(id, type, tok);
            case BEGIN:
                return new BeginCall(id, type, tok);
            case CLEAR:
                return new ClearCall(id, type, tok);
            case CLEAR_NAMESPACES:
                return new ClearNamespacesCall(id, type, tok);
            case CLOSE_CONNECTION:
                return new CloseConnectionCall(id, type, tok);
            case CLOSE_ITERATION:
                return new CloseIterationCall(id, type, tok);
            case COMMIT:
                return new CommitCall(id, type, tok);
            case CONSTRUCT:
                return new ConstructorCall(id, type, tok);
            case EVALUATE:
                return new EvaluateCall(id, type, tok);
            case GET_CONTEXT_IDS:
                return new GetContextIDsCall(id, type, tok);
            case GET_NAMESPACE:
                return new GetNamespaceCall(id, type, tok);
            case GET_NAMESPACES:
                return new GetNamespacesCall(id, type, tok);
            case GET_STATEMENTS:
                return new GetStatementsCall(id, type, tok);
            case HAS_NEXT:
                return new HasNextCall(id, type, tok);
            case NEXT:
                return new NextCall(id, type, tok);
            case REMOVE:
                return new RemoveCall(id, type, tok);
            case REMOVE_NAMESPACE:
                return new RemoveNamespaceCall(id, type, tok);
            case REMOVE_STATEMENTS:
                return new RemoveStatementsCall(id, type, tok);
            case ROLLBACK:
                return new RollbackCall(id, type, tok);
            case SET_NAMESPACE:
                return new SetNamespaceCall(id, type, tok);
            case SIZE:
                return new SizeCall(id, type, tok);
            default:
                throw new IllegalArgumentException("bad callback: " + s);
        }
    }

    protected IRI parseIRI(final String s) {
        if (s.equals("null")) {
            return null;
        }

        return NTriplesUtil.parseURI(s, valueFactory);
    }

    protected Resource parseResource(final String s) {
        if (s.equals("null")) {
            return null;
        }

        return NTriplesUtil.parseResource(s, valueFactory);
    }

    protected Value parseValue(final String s) {
        if (s.equals("null")) {
            return null;
        }

        return NTriplesUtil.parseValue(s, valueFactory);
    }

    protected boolean parseBoolean(final String s) {
        return s.equals("true");
    }

    protected Resource[] parseContexts(final String s) {
        //System.out.println("s = " + s);
        String s2 = s.substring(1, s.length() - 1);
        if (0 == s2.length()) {
            return new Resource[0];
        } else {
            String[] vals = s2.split(COMMA);
            Resource[] contexts = new Resource[vals.length];
            for (int i = 0; i < vals.length; i++) {
                contexts[i] = parseResource(vals[i]);
            }

            return contexts;
        }
    }

    protected String parseString(final String s) {
        if (s.equals("null")) {
            return null;
        }

        return Formatting.unescapeString(s.substring(1, s.length() - 1));
    }

    protected String toString(final String v) {
        if (null == v) {
            return "null";
        }

        return "\"" + Formatting.escapeString(v) + "\"";
    }

    protected String toString(final Value v) {
        if (null == v) {
            return "null";
        }

        return (v instanceof Resource)
                ? toString((Resource) v)
                : toString((Literal) v);
    }

    protected String toString(final Literal v) {
        if (null == v) {
            return "null";
        }

        return NTriplesUtil.toNTriplesString(v);
    }

    protected String toString(final Resource v) {
        if (null == v) {
            return "null";
        }

        return (v instanceof IRI)
                ? toString((IRI) v)
                : toString((BNode) v);
    }

    protected String toString(final IRI v) {
        if (null == v) {
            return "null";
        }

        return NTriplesUtil.toNTriplesString(v);
    }

    protected String toString(final BNode v) {
        if (null == v) {
            return "null";
        }

        return NTriplesUtil.toNTriplesString(v);
    }

    protected String toString(final boolean v) {
        return v ? "true" : "false";
    }

    protected String toString(final Resource[] v) {
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Resource ctx : v) {
            if (first) {
                first = false;
            } else {
                sb.append(COMMA);
            }

            sb.append(toString(ctx));
        }
        sb.append("}");
        return sb.toString();
    }

}
