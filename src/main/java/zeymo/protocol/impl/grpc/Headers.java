package zeymo.protocol.impl.grpc;

import java.util.*;

/**
 * @author Zeymo
 */
public final class Headers {
    private static final long serialVersionUID = -1020286049567379459L;
    private final String[] namesAndValues;

    Headers(Builder builder) {
        this.namesAndValues = builder.namesAndValues.toArray(new String[builder.namesAndValues.size()]);
    }

    private Headers(String[] namesAndValues) {
        this.namesAndValues = namesAndValues;
    }

    public String get(String name) {
        return get(namesAndValues, name);
    }

    public int size() {
        return namesAndValues.length / 2;
    }

    public String name(int index) {
        return namesAndValues[index * 2];
    }

    public String value(int index) {
        return namesAndValues[index * 2 + 1];
    }

    public Set<String> names() {
        TreeSet<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            result.add(name(i));
        }
        return Collections.unmodifiableSet(result);
    }

    public List<String> values(String name) {
        List<String> result = null;
        for (int i = 0, size = size(); i < size; i++) {
            if (name.equalsIgnoreCase(name(i))) {
                if (result == null) { result = new ArrayList<>(2); }
                result.add(value(i));
            }
        }
        return result != null
                ? Collections.unmodifiableList(result)
                : Collections.emptyList();
    }

    public long byteCount() {
        // Each header name has 2 bytes of overhead for ': ' and every header value has 2 bytes of
        // overhead for '\r\n'.
        long result = namesAndValues.length * 2;

        for (int i = 0, size = namesAndValues.length; i < size; i++) {
            result += namesAndValues[i].length();
        }

        return result;
    }

    public Builder newBuilder() {
        Builder result = new Builder();
        Collections.addAll(result.namesAndValues, namesAndValues);
        return result;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Headers
                && Arrays.equals(((Headers)other).namesAndValues, namesAndValues);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(namesAndValues);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        for (int i = 0, size = size(); i < size; i++) {
            result.append(name(i)).append(": ").append(value(i)).append("\n");
        }
        return result.toString();
    }

    public String[] toArray() {
        return namesAndValues;
    }

    public Map<String, List<String>> toMultimap() {
        Map<String, List<String>> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = size(); i < size; i++) {
            String name = name(i).toLowerCase(Locale.US);
            List<String> values = result.get(name);
            if (values == null) {
                values = new ArrayList<>(2);
                result.put(name, values);
            }
            values.add(value(i));
        }
        return result;
    }

    public static Map<String, String> toMap(Headers headers) {
        Map<String, String> result = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        for (int i = 0, size = headers.size(); i < size; i++) {
            String name = headers.name(i).toLowerCase(Locale.US);
            result.put(name, headers.value(i));
        }
        return result;
    }

    private static String get(String[] namesAndValues, String name) {
        for (int i = namesAndValues.length - 2; i >= 0; i -= 2) {
            if (name.equalsIgnoreCase(namesAndValues[i])) {
                return namesAndValues[i + 1];
            }
        }
        return null;
    }

    public static Headers of(String... namesAndValues) {
        if (namesAndValues == null) { throw new NullPointerException("namesAndValues == null"); }
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Expected alternating header names and values");
        }

        // Make a defensive copy and clean it up.
        namesAndValues = namesAndValues.clone();
        for (int i = 0; i < namesAndValues.length; i++) {
            if (namesAndValues[i] == null) { throw new IllegalArgumentException("Headers cannot be null"); }
            namesAndValues[i] = namesAndValues[i].trim();
        }

        // Check for malformed headers.
        for (int i = 0; i < namesAndValues.length; i += 2) {
            String name = namesAndValues[i];
            String value = namesAndValues[i + 1];
            checkName(name);
            checkValue(value, name);
        }

        return new Headers(namesAndValues);
    }

    public static Headers of(Map<String, String> headers) {
        if (headers == null) { throw new NullPointerException("headers == null"); }

        // Make a defensive copy and clean it up.
        String[] namesAndValues = new String[headers.size() * 2];
        int i = 0;
        for (Map.Entry<String, String> header : headers.entrySet()) {
            if (header.getKey() == null || header.getValue() == null) {
                throw new IllegalArgumentException("Headers cannot be null");
            }
            String name = header.getKey().trim();
            String value = header.getValue().trim();
            checkName(name);
            checkValue(value, name);
            namesAndValues[i] = name;
            namesAndValues[i + 1] = value;
            i += 2;
        }

        return new Headers(namesAndValues);
    }

    static void checkName(String name) {
        if (name == null) { throw new NullPointerException("name == null"); }
        if (name.isEmpty()) { throw new IllegalArgumentException("name is empty"); }
        for (int i = 0, length = name.length(); i < length; i++) {
            char c = name.charAt(i);
            if (c <= '\u0020' || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(
                        "Unexpected char %#04x at %d in header name: %s", (int)c, i, name));
            }
        }
    }

    static void checkValue(String value, String name) {
        if (value == null) { throw new NullPointerException("value for name " + name + " == null"); }
        for (int i = 0, length = value.length(); i < length; i++) {
            char c = value.charAt(i);
            if ((c <= '\u001f' && c != '\t') || c >= '\u007f') {
                throw new IllegalArgumentException(String.format(
                        "Unexpected char %#04x at %d in %s value: %s", (int)c, i, name, value));
            }
        }
    }

    public static final class Builder {
        final List<String> namesAndValues = new ArrayList<>(20);

        /**
         * Add a header with the specified name and value. Does validation of header names and values.
         */
        public Builder add(String name, String value) {
            checkName(name);
            checkValue(value, name);
            return addLenient(name, value);
        }

        /**
         * Add a header with the specified name and value. Does validation of header names, allowing
         * non-ASCII values.
         */
        public Builder addUnsafeNonAscii(String name, String value) {
            checkName(name);
            return addLenient(name, value);
        }

        public Builder addAll(Headers headers) {
            for (int i = 0, size = headers.size(); i < size; i++) {
                addLenient(headers.name(i), headers.value(i));
            }

            return this;
        }

        /**
         * Add a field with the specified value without any validation. Only appropriate for headers
         * from the remote peer or cache.
         */
        Builder addLenient(String name, String value) {
            namesAndValues.add(name);
            namesAndValues.add(value.trim());
            return this;
        }

        public Builder removeAll(String name) {
            for (int i = 0; i < namesAndValues.size(); i += 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    namesAndValues.remove(i); // name
                    namesAndValues.remove(i); // value
                    i -= 2;
                }
            }
            return this;
        }

        public Builder set(String name, String value) {
            checkName(name);
            checkValue(value, name);
            removeAll(name);
            addLenient(name, value);
            return this;
        }

        public String get(String name) {
            for (int i = namesAndValues.size() - 2; i >= 0; i -= 2) {
                if (name.equalsIgnoreCase(namesAndValues.get(i))) {
                    return namesAndValues.get(i + 1);
                }
            }
            return null;
        }

        public Headers build() {
            return new Headers(this);
        }
    }
}
