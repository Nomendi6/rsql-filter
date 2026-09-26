package rsql.where.keys;

/** A base the library cannot reach, declaring the valueOf its public subclass inherits. */
class HiddenBase {

    public static PublicSubKey valueOf(String text) {
        return new PublicSubKey(text);
    }
}
