package rsql.where.keys;

/** A public key class whose valueOf is inherited from a base that is not public - callable as PublicSubKey.valueOf(s). */
public class PublicSubKey extends HiddenBase {

    private final String text;

    PublicSubKey(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return text;
    }
}
