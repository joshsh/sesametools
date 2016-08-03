package net.fortytwo.sesametools;

import org.eclipse.rdf4j.model.BNode;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.XMLSchema;

import java.util.Random;
import java.util.UUID;

/**
 * A utility for generating random values and statements
 *
 * @author Joshua Shinavier (http://fortytwo.net)
 */
public class RandomValueFactory {
    private static final int MAXINT = 65535;

    private static final int MIN_LABEL_LENGTH = 1;
    private static final int MAX_LABEL_LENGTH = 100;

    private static final String[] languages = {"en", "fr", "de"};

    private enum ValueType {
        URI, /*Bnode,*/ LITERAL
    }

    private enum LiteralKind {
        PLAIN, WITH_LANGUAGE, TYPED
    }

    private enum LiteralType {
        STRING, INT, DOUBLE
    }

    private ValueFactory valueFactory;
    private Random rand = new Random();

    public RandomValueFactory(final ValueFactory vf) {
        this.valueFactory = vf;
    }

    public Statement randomStatement() {
        return randomStatement(randomResource());
    }

    public Statement randomStatement(final Resource context) {
        Resource subj = randomResource();
        IRI pred = randomIRI();
        Value obj = randomValue();
        return valueFactory.createStatement(subj, pred, obj, context);
    }

    public Resource randomResource() {
        ValueType type;
        do {
            type = randomValueType();
        } while (ValueType.LITERAL == type);
        return (Resource) randomValue(type);
    }

    public Value randomValue() {
        ValueType type = randomValueType();
        return randomValue(type);
    }

    public IRI randomIRI() {
        return valueFactory.createIRI("urn:uuid:" + UUID.randomUUID().toString().replace("-", ""));
    }

    public BNode randomBNode() {
        return valueFactory.createBNode();
    }

    public Literal randomLiteral() {
        LiteralKind kind = LiteralKind.values()[rand.nextInt(MAXINT) % LiteralKind.values().length];
        Literal l = null;

        switch (kind) {
            case PLAIN:
                l = valueFactory.createLiteral(randomStringLabel());
                break;
            case WITH_LANGUAGE:
                l = valueFactory.createLiteral(randomStringLabel(), randomLanguage());
                break;
            case TYPED:
                l = randomTypedLiteral();
                break;
        }

        return l;
    }

    public Literal randomTypedLiteral() {
        LiteralType type = LiteralType.values()[rand.nextInt(MAXINT) % LiteralType.values().length];
        Literal l = null;

        switch (type) {
            case INT:
                l = valueFactory.createLiteral(rand.nextInt(MAXINT));
                break;
            case DOUBLE:
                l = valueFactory.createLiteral(rand.nextDouble());
                break;
            case STRING:
                l = valueFactory.createLiteral(randomStringLabel(), XMLSchema.STRING);
                break;
        }

        return l;
    }

    private ValueType randomValueType() {
        return ValueType.values()[rand.nextInt(MAXINT) % ValueType.values().length];
    }

    private Value randomValue(final ValueType type) {
        Value v = null;
        switch (type) {
            case URI:
                v = randomIRI();
                break;
            //case Bnode:
            //    v = randomBNode();
            //    break;
            case LITERAL:
                v = randomLiteral();
                break;
        }
        return v;
    }

    private String randomLanguage() {
        return languages[rand.nextInt(MAXINT) % languages.length];
    }

    private String randomLabel(final int minlen, final int maxlen) {
        int n = minlen + rand.nextInt(maxlen - minlen);
        byte[] b = new byte[n];
        int range = '~' - ' ';

        // For now, use only "safe" characters.
        for (int i = 0; i < n; i++) {
            b[i] = (byte) (' ' + rand.nextInt(range));
        }

        return new String(b);
    }

    private String randomStringLabel() {
        return randomLabel(MIN_LABEL_LENGTH, MAX_LABEL_LENGTH);
    }

}
