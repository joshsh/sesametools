package net.fortytwo.sesametools.ldserver.query;

import info.aduna.iteration.CloseableIteration;

import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.QueryResultHandlerException;
import org.openrdf.query.impl.MapBindingSet;
import org.openrdf.query.parser.ParsedBooleanQuery;
import org.openrdf.query.parser.ParsedQuery;
import org.openrdf.query.parser.sparql.SPARQLParser;
import org.openrdf.query.resultio.TupleQueryResultWriter;
import org.openrdf.query.resultio.sparqljson.SPARQLResultsJSONWriter;
import org.openrdf.query.resultio.sparqlxml.SPARQLResultsXMLWriter;
import org.openrdf.sail.SailConnection;
import org.openrdf.sail.SailException;
import org.restlet.data.MediaType;
import org.restlet.representation.Variant;

import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class SparqlTools {
    public enum SparqlResultFormat {
        // Note: the XML format is defined first, so that it is the default format.
        XML("application/sparql-results+xml"),
        JSON("application/sparql-results+json");

        private static List<Variant> VARIANTS;

        private final MediaType mediaType;

        SparqlResultFormat(final String mimeType) {
            mediaType = new MediaType(mimeType);
        }

        public MediaType getMediaType() {
            return mediaType;
        }

        public static SparqlResultFormat lookup(final MediaType mediaType) {
            for (SparqlResultFormat f : SparqlResultFormat.values()) {
                if (f.mediaType.equals(mediaType)) {
                    return f;
                }
            }

            return null;
        }

        public static List<Variant> getVariants() {
            if (null == VARIANTS) {
                VARIANTS = new LinkedList<>();
                for (SparqlResultFormat f : SparqlResultFormat.values()) {
                    VARIANTS.add(new Variant(f.mediaType));
                }
            }

            return VARIANTS;
        }
    }

    private static final String BASE_URI = "http://example.org/bogusBaseURI";

    private static ParsedQuery parseQuery(final String query) throws MalformedQueryException {
        SPARQLParser parser = new SPARQLParser();
        return parser.parseQuery(query, BASE_URI);

    }

    public static synchronized CloseableIteration<? extends BindingSet, QueryEvaluationException>
    evaluateQuery(final ParsedQuery query,
                  final SailConnection sc) throws QueryException {
        MapBindingSet bindings = new MapBindingSet();
        boolean includeInferred = false;
        try {
            return sc.evaluate(query.getTupleExpr(), query.getDataset(), bindings, includeInferred);
        } catch (SailException e) {
            throw new QueryException(e);
        }
    }

    public static void executeQuery(final String queryStr,
                                    final SailConnection sc,
                                    final OutputStream out,
                                    final int limit,
                                    final SparqlResultFormat format) throws QueryException {
        TupleQueryResultWriter w;

        switch (format) {
            case JSON:
                w = new SPARQLResultsJSONWriter(out);
                break;
            case XML:
                w = new SPARQLResultsXMLWriter(out);
                break;
            default:
                throw new QueryException(new Throwable("bad query result format: " + format));
        }

        try {
            final ParsedQuery query = parseQuery(queryStr);

            try (CloseableIteration<? extends BindingSet, QueryEvaluationException> iter = evaluateQuery(query, sc)) {
                if (query instanceof ParsedBooleanQuery) {
                    w.handleBoolean(iter.hasNext());
                } else {
                    final List<String> columnHeaders = new LinkedList<>();
                    columnHeaders.addAll(query.getTupleExpr().getBindingNames());
                    // FIXME: *do* specify the column headers
                    // columnHeaders.add("post");
                    // columnHeaders.add("content");
                    // columnHeaders.add("screen_name");

                    w.startQueryResult(columnHeaders);
                    int count = 0;
                    while (iter.hasNext() && count < limit) {
                        w.handleSolution(iter.next());
                        count++;
                    }
                    w.endQueryResult();
                }

            } catch (QueryEvaluationException | QueryResultHandlerException e) {
                throw new QueryException(e);
            }
        } catch (MalformedQueryException | QueryEvaluationException e) {
            throw new QueryException(e);
        }
    }
}
