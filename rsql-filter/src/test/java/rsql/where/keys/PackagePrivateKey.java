package rsql.where.keys;

/** A key class the library cannot reach: its valueOf is public, the class is not. */
class PackagePrivateKey {

    private final String text;

    private PackagePrivateKey(String text) {
        this.text = text;
    }

    public static PackagePrivateKey valueOf(String text) {
        return new PackagePrivateKey(text);
    }

    @Override
    public String toString() {
        return text;
    }
}
