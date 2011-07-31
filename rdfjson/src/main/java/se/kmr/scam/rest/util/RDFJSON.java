package se.kmr.scam.rest.util;

import net.fortytwo.sesametools.rdfjson.OrderedGraphImpl;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.openrdf.model.BNode;
import org.openrdf.model.Graph;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * A utility class to help converting Sesame Graphs from and to RDF/JSON.
 *
 * @author Hannes Ebner <hebner@kth.se>, with tweaks by Joshua Shinavier and the ordered implementation by Peter Ansell
 */
public class RDFJSON {

    private static final String STRING_GRAPHS = "graphs";
	private static final String STRING_URI = "uri";
	private static final String STRING_BNODE = "bnode";
	private static final String STRING_DATATYPE = "datatype";
	private static final String STRING_LITERAL = "literal";
	private static final String STRING_LANG = "lang";
	private static final String STRING_TYPE = "type";
	private static final String STRING_VALUE = "value";
	
	private static Logger log = LoggerFactory.getLogger(RDFJSON.class);

    /**
     * Implementation using the json.org API.
     *
     * @param json The RDF/JSON string to be parsed and converted into a Sesame
     *             Graph.
     * @return A Sesame Graph if successful, otherwise null.
     */
    public static Graph rdfJsonToGraph(String json) {
        Graph result = new OrderedGraphImpl();
        ValueFactory vf = result.getValueFactory();

        try {
            JSONObject input = JSONObject.fromObject(json);
        	Iterator<String> subjects = input.keys();
            while (subjects.hasNext()) {
                String subjStr = subjects.next();
                Resource subject = null;
                subject = subjStr.startsWith("_:")
                        ? vf.createBNode(subjStr.substring(2))
                        : vf.createURI(subjStr);
                JSONObject pObj = input.getJSONObject(subjStr);
                Iterator<String> predicates = pObj.keys();
                while (predicates.hasNext()) {
                    String predStr = predicates.next();
                    URI predicate = vf.createURI(predStr);
                    JSONArray predArr = pObj.getJSONArray(predStr);
                	for (int i = 0; i < predArr.size(); i++) {
                        Value object = null;
                        JSONObject obj = predArr.getJSONObject(i);
                        if (!obj.has(RDFJSON.STRING_VALUE)) {
                            continue;
                        }
                        String value = obj.getString(RDFJSON.STRING_VALUE);
                        if (!obj.has(RDFJSON.STRING_TYPE)) {
                            continue;
                        }
                        String type = obj.getString(RDFJSON.STRING_TYPE);
                        String lang = null;
                        if (obj.has(RDFJSON.STRING_LANG)) {
                            lang = obj.getString(RDFJSON.STRING_LANG);
                        }
                        String datatype = null;
                        if (obj.has(RDFJSON.STRING_DATATYPE)) {
                            datatype = obj.getString(RDFJSON.STRING_DATATYPE);
                        }
                        if (RDFJSON.STRING_LITERAL.equals(type)) {
                            if (lang != null) {
                                object = vf.createLiteral(value, lang);
                            } else if (datatype != null) {
                                object = vf.createLiteral(value, vf.createURI(datatype));
                            } else {
                                object = vf.createLiteral(value);
                            }
                        } else if (RDFJSON.STRING_BNODE.equals(type)) {
                            object = vf.createBNode(value.substring(2));
                        } else if (RDFJSON.STRING_URI.equals(type)) {
                            object = vf.createURI(value);
                        }

                        if (obj.has(RDFJSON.STRING_GRAPHS)) {
                            JSONArray a = obj.getJSONArray(RDFJSON.STRING_GRAPHS);
                            //System.out.println("a.length() = " + a.length());
                            for(int j = 0; j < a.size(); j++)
                            {
                                // Note: any nulls here will result in statements in the default context.
                                String s = a.getString(j);
                                Resource context = s.equals("null") ? null : vf.createURI(s);
                                //System.out.println("context = " + context);
                                result.add(subject, predicate, object, context);
                            }
                        } else {
                            result.add(subject, predicate, object);
                        }
                    }
                }
            }
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        return result;
    }

    /**
     * Implementation using the org.json API.
     *
     * @param graph A Sesame Graph.
     * @return An RDF/JSON string if successful, otherwise null.
     */
    public static String graphToRdfJson(Graph graph) {
        JSONObject result = new JSONObject();
        try {
            Collection<Resource> subjects = new LinkedList<Resource>();
            for (Statement s1 : graph) {
                subjects.add(s1.getSubject());
            }
            for (Resource subject : subjects) {
                JSONObject predicateObj = new JSONObject();
                Collection<URI> predicates = new LinkedList<URI>();
                Iterator<Statement> s2 = graph.match(subject, null, null);
                while (s2.hasNext()) {
                    predicates.add(s2.next().getPredicate());
                }
                for (URI predicate : predicates) {
                    JSONArray valueArray = new JSONArray();
                    Iterator<Statement> stmnts = graph.match(subject, predicate, null);
                    Collection<Value> objects = new LinkedList<Value>();
                    while (stmnts.hasNext()) {
                        objects.add(stmnts.next().getObject());
                    }
                    for (Value object : objects) {
                        Iterator<Statement> stmnts2 = graph.match(subject, predicate, object);
                        JSONArray contexts = new JSONArray();
                        int i = 0;
                        boolean nonDefaultContext = false;
                        while (stmnts2.hasNext()) {
                            Resource context = stmnts2.next().getContext();
                            contexts.add(i, null == context ? null : context.stringValue());
                            if (null != context) {
                                nonDefaultContext = true;
                            }
                            i++;
                        }
                        
                        
                        JSONObject valueObj = new JSONObject();
                        valueObj.put(RDFJSON.STRING_VALUE, object.stringValue());
                        if (object instanceof Literal) {
                            valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_LITERAL);
                            Literal l = (Literal) object;
                            if (l.getLanguage() != null) {
                                valueObj.put(RDFJSON.STRING_LANG, l.getLanguage());
                            } else if (l.getDatatype() != null) {
                                valueObj.put(RDFJSON.STRING_DATATYPE, l.getDatatype().stringValue());
                            }
                        } else if (object instanceof BNode) {
                            valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_BNODE);
                        } else if (object instanceof URI) {
                            valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_URI);
                        }
                        if (nonDefaultContext) {
                            valueObj.put(RDFJSON.STRING_GRAPHS, contexts);
                        }
                        valueArray.add(valueObj);
                    }
                    predicateObj.put(predicate.stringValue(), valueArray);
                }
                result.put(subject.stringValue(), predicateObj);
            }
            return result.toString(2);
        } catch (JSONException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }

    /**
	 * Outputs an ordered Graph directly to JSON
     *
     * @param graph A Sesame Graph that has been preordered in the order subject>predicate>object>context so that it can be output directly without any further checks
     * @return An RDF/JSON string if successful, otherwise null.
     */
    public static String graphToRdfJsonPreordered(Graph graph) 
    {
//    	int outputCounter = 0;
        JSONObject result = new JSONObject();
        try 
        {
        	Resource lastSubject = null;
    		URI lastPredicate = null;
    		Value lastObject = null;
        	Resource lastContext = null;
        	
        	JSONObject predicateArray = new JSONObject();
        	JSONArray objectArray = new JSONArray();
        	JSONArray contextArray = new JSONArray();
        	
        	for(Statement nextStatement : graph)
        	{
    			Resource nextSubject = nextStatement.getSubject();
        		
				// Dump everything if the subject changes after the first iteration
				if(lastSubject != null && !nextSubject.equals(lastSubject))
        		{
					//==================================================
					// Dump the last variables starting from the context
	    			if(lastContext != null || contextArray.size() == 0)
	    			{
	    				contextArray.add(lastContext);
	    			}
    				addObjectToArray(lastObject, objectArray, contextArray);
                    predicateArray.put(lastPredicate, objectArray);
                    result.put(lastSubject, predicateArray);
					//==================================================
                    
					//==================================================
					// Recreate the relevant temporary objects, now that 
                    // they have been stored with the results
                    predicateArray = new JSONObject();
        			objectArray = new JSONArray();
        			contextArray = new JSONArray();
        			//==================================================

        			//==================================================
        			// Change all of the pointers for the last objects over
        			lastSubject = nextStatement.getSubject();
        			lastPredicate = nextStatement.getPredicate();
        			lastObject = nextStatement.getObject();
        			lastContext = nextStatement.getContext();
        			//==================================================
        			
        		}
				else
				{
	
					lastSubject = nextSubject;
					
	    			// Add the lastPredicate when it changes, as we know we have all of the objects and their related contexts for the last predicate now
					if(lastPredicate != null && !nextStatement.getPredicate().equals(lastPredicate))
	        		{
						//==================================================
						// Dump the last variables starting from the context
		    			if(lastContext != null || contextArray.size() == 0)
		    			{
		    				contextArray.add(lastContext);
		    			}
	    				addObjectToArray(lastObject, objectArray, contextArray);
	                    predicateArray.put(lastPredicate, objectArray);
						//==================================================
	                    
						//==================================================
						// Recreate the relevant temporary objects, now that 
	                    // they have been stored with the last predicate
	                    objectArray = new JSONArray();
	                    contextArray = new JSONArray();
						//==================================================
	                    
						//==================================================
	        			// Change the relevant pointers for the last objects over
		    			lastPredicate = nextStatement.getPredicate();
		    			lastObject = nextStatement.getObject();
		    			lastContext = nextStatement.getContext();
						//==================================================
	        		}
					else
					{
		    			lastPredicate = nextStatement.getPredicate();
		    			
		
		    			// Add the lastObject to objectArray when it changes, as we know we have all of the contexts for the object then
		    			if(lastObject != null && !nextStatement.getObject().equals(lastObject))
		    			{
							//==================================================
							// Dump the last variables starting from the context
			    			if(lastContext != null || contextArray.size() == 0)
			    			{
								// Add the lastContext to contextArray
				    			contextArray.add(lastContext);
			    			}
			    			addObjectToArray(lastObject, objectArray, contextArray);
							//==================================================
		
							//==================================================
							// Recreate the temporary context array, now that 
		                    // they have been stored with the last object
		                    contextArray = new JSONArray();
							//==================================================

							//==================================================
		        			// Change the relevant pointers for the last objects over
			    			lastObject = nextStatement.getObject();
			    			lastContext = nextStatement.getContext();
							//==================================================
		    			}
		    			else
		    			{
			    			lastObject = nextStatement.getObject();
			    			
			    			// add the next context for the current object
			    			contextArray.add(lastContext);
			    			
			    			lastContext = nextStatement.getContext();
		    			}
		    			
					}
				}
        	}
        	

			// the last subject/predicate/object/context will never get pushed inside the loop above, so push it here if we went into the loop
    		if(graph.size() > 0)
    		{
    			if(lastContext != null || contextArray.size() == 0)
    			{
    				contextArray.add(lastContext);
    			}
    			addObjectToArray(lastObject, objectArray, contextArray);
                predicateArray.put(lastPredicate, objectArray);
        		result.put(lastSubject.stringValue(), predicateArray);
    		}
    		
            return result.toString(2);
	    } 
        catch (JSONException e) 
        {
	        log.error(e.getMessage(), e);
	    }
	    return null;
	}
	/**
	 * @param contexts
	 * @param nonDefaultContext
	 */
	private static void addObjectToArray(Value object, JSONArray valueArray, JSONArray contexts)
	{
		JSONObject valueObj = new JSONObject();
		
		valueObj.put(RDFJSON.STRING_VALUE, object.stringValue());
		
		if (object instanceof Literal) 
		{
		    valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_LITERAL);
		    Literal l = (Literal) object;
		    
		    if (l.getLanguage() != null) 
		    {
		        valueObj.put(RDFJSON.STRING_LANG, l.getLanguage());
		    } 
		    else if (l.getDatatype() != null) 
		    {
		        valueObj.put(RDFJSON.STRING_DATATYPE, l.getDatatype().stringValue());
		    }
		} 
		else if (object instanceof BNode) 
		{
		    valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_BNODE);
		} 
		else if (object instanceof URI) 
		{
		    valueObj.put(RDFJSON.STRING_TYPE, RDFJSON.STRING_URI);
		}
		
		if (contexts.size() > 0 && !(contexts.size() == 1 && contexts.contains(null)))
		{
				valueObj.put(RDFJSON.STRING_GRAPHS, contexts);
		}
		valueArray.add(valueObj);
    }

    /**
     * Implementation using the Streaming API of the Jackson framework.
     *
     * @param graph A Sesame Graph.
     * @return An RDF/JSON string if successful, otherwise null.
     */
    /*
    public static String graphToRdfJsonJackson(Graph graph) {
        JsonFactory f = new JsonFactory();
        StringWriter sw = new StringWriter();
        JsonGenerator g = null;
        try {
            g = f.createJsonGenerator(sw);
            g.useDefaultPrettyPrinter();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            return null;
        }

        try {
            g.writeStartObject(); // root object
            Set<Resource> subjects = new HashSet<Resource>();
            for (Statement s1 : graph) {
                subjects.add(s1.getSubject());
            }
            for (Resource subject : subjects) {
                g.writeObjectFieldStart(subject.stringValue()); // subject
                Set<URI> predicates = new HashSet<URI>();
                Iterator<Statement> s2 = graph.match(subject, null, null);
                while (s2.hasNext()) {
                    predicates.add(s2.next().getPredicate());
                }
                for (URI predicate : predicates) {
                    g.writeArrayFieldStart(predicate.stringValue()); // predicate
                    Iterator<Statement> stmnts = graph.match(subject, predicate, null);
                    while (stmnts.hasNext()) {
                        Value v = stmnts.next().getObject();
                        g.writeStartObject(); // value
                        g.writeStringField("value", v.stringValue());
                        if (v instanceof Literal) {
                            g.writeStringField("type", "literal");
                            Literal l = (Literal) v;
                            if (l.getLanguage() != null) {
                                g.writeStringField("lang", l.getLanguage());
                            } else if (l.getDatatype() != null) {
                                g.writeStringField("datatype", l.getDatatype().stringValue());
                            }
                        } else if (v instanceof BNode) {
                            g.writeStringField("type", "bnode");
                        } else if (v instanceof URI) {
                            g.writeStringField("type", "uri");
                        }
                        g.writeEndObject(); // value
                    }
                    g.writeEndArray(); // predicate
                }
                g.writeEndObject(); // subject
            }
            g.writeEndObject(); // root object
            g.close();
            return sw.toString();
        } catch (JsonGenerationException e) {
            log.error(e.getMessage(), e);
        } catch (IOException ioe) {
            log.error(ioe.getMessage(), ioe);
        }
        return null;
    }*/

}