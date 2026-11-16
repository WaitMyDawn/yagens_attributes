package yagen.waitmydawn.yagens_attributes.core;

import java.util.*;

/**
 * The closest explicitly registered superclass wins; names and unrelated interfaces are not evidence.
 */
public final class ClassDefaults<T> {
    private final Map<Class<?>, T> rules;
    private final ClassValue<Optional<T>> cache = new ClassValue<>() {
        @Override
        protected Optional<T> computeValue(Class<?> type) {
            for (Class<?> ancestor = type; ancestor != null; ancestor = ancestor.getSuperclass()) {
                T value = rules.get(ancestor);
                if (value != null) return Optional.of(value);
            }
            return Optional.empty();
        }
    };

    public ClassDefaults(Map<Class<?>, T> rules) {
        this.rules = Map.copyOf(rules);
    }

    public Optional<T> match(Class<?> type) {
        return cache.get(type);
    }
}
