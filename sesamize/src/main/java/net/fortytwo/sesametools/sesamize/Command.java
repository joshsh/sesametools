package net.fortytwo.sesametools.sesamize;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static net.fortytwo.sesametools.sesamize.Sesamize.DEFAULT_BASEURI;

public abstract class Command {
    private final String name;
    private List<Parameter> anonymousParameters = new LinkedList<>();
    private Map<String, Parameter> namedParameters = new HashMap<>();

    public Command(final String name) {
        Preconditions.checkNotNull(name);
        this.name = name;
    }

    public abstract void execute(SesamizeArgs args) throws Exception;

    protected void addParameter(final Parameter param) {
        if (null == param.getName()) {
            anonymousParameters.add(param);
        } else {
            if (getNamedParameters().keySet().contains(param.getName())) {
                throw new ExceptionInInitializerError("duplicate parameter '" + param.getName() + "'");
            }

            getNamedParameters().put(param.getName(), param);
        }
    }

    public String getName() {
        return name;
    }

    public Map<String, Parameter> getNamedParameters() {
        return namedParameters;
    }

    public List<Parameter> getAnonymousParameters() {
        return anonymousParameters;
    }

    protected static String getBaseURI(final SesamizeArgs args) {
        return args.getOption(DEFAULT_BASEURI, "b", "baseuri");
    }

    public static class Parameter<T> {
        private final String name;
        private final String shortName;
        private final boolean required;
        private final Class<T> valueClass;
        private final T defaultValue;
        private final String description;

        public Parameter(String name,
                         String shortName,
                         boolean required,
                         Class<T> valueClass,
                         T defaultValue,
                         String description) {
            Preconditions.checkNotNull(valueClass);
            Preconditions.checkArgument(null == shortName || null != name);

            this.name = name;
            this.description = description;
            this.shortName = shortName;
            this.required = required;
            this.valueClass = valueClass;
            this.defaultValue = defaultValue;
        }

        public String getShortestName() {
            return null != getShortName() ? getShortName() : getName();
        }

        public String getName() {
            return name;
        }

        public String getShortName() {
            return shortName;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isRequiredAndHasNoDefaultValue() {
            return required && null == defaultValue;
        }

        public Class<T> getValueClass() {
            return valueClass;
        }

        public T getDefaultValue() {
            return defaultValue;
        }

        public String getDescription() {
            return description;
        }
    }
}
