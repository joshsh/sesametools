package net.fortytwo.sesametools.sesamize;

import org.openrdf.query.Binding;
import org.openrdf.query.BindingSet;
import org.openrdf.query.TupleQueryResultHandlerException;
import org.openrdf.query.resultio.TupleQueryResultFormat;
import org.openrdf.query.resultio.TupleQueryResultWriter;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * User: josh
 * Date: Jul 5, 2010
 * Time: 7:28:15 PM
 */
public class SPARQLResultsTabWriter implements TupleQueryResultWriter {
    private final PrintStream ps;

    public SPARQLResultsTabWriter(final OutputStream outputStream) {
        ps = new PrintStream(outputStream);
    }

    public TupleQueryResultFormat getTupleQueryResultFormat() {
        return null;
    }

    public void startQueryResult(List<String> strings) throws TupleQueryResultHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void endQueryResult() throws TupleQueryResultHandlerException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleSolution(final BindingSet bindingSet) throws TupleQueryResultHandlerException {
        boolean first = true;

        for (Binding b : bindingSet) {
            if (first) {
                first = false;
            } else {
                ps.print("\t");
            }

            ps.print(b.getValue());
        }

        ps.println("");
    }
}
