package com.nomendi6.rsql.it.domain.compositekey;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * A three-column composite key that owns its textual form, the way a generated application's key class does:
 * the parts are joined with {@code ~}, and every byte outside {@code [A-Za-z0-9-_.]} is written as {@code !}
 * followed by two hex digits of its UTF-8 encoding. The library knows none of this - it only calls
 * {@link #valueOf(String)}.
 */
@Embeddable
public class DocumentKey implements Serializable {

    private static final long serialVersionUID = 1L;

    @Column(name = "company_code")
    private String companyCode;

    @Column(name = "doc_year")
    private Integer docYear;

    @Column(name = "doc_no")
    private Long docNo;

    public DocumentKey() {}

    public DocumentKey(String companyCode, Integer docYear, Long docNo) {
        this.companyCode = companyCode;
        this.docYear = docYear;
        this.docNo = docNo;
    }

    /**
     * Parse the encoded key.
     *
     * @throws IllegalArgumentException for a wrong number of parts, a bad escape or a part that is not a number
     */
    public static DocumentKey valueOf(String key) {
        List<String> parts = new ArrayList<>(3);
        ByteArrayOutputStream part = new ByteArrayOutputStream();
        for (int i = 0; i < key.length(); i++) {
            char c = key.charAt(i);
            if (c == '~') {
                parts.add(part.toString(StandardCharsets.UTF_8));
                part.reset();
            } else if (c == '!') {
                if (i + 3 > key.length()) {
                    throw new IllegalArgumentException("Invalid escape in key: " + key);
                }
                part.write(HexFormat.fromHexDigits(key, i + 1, i + 3));
                i += 2;
            } else {
                part.writeBytes(String.valueOf(c).getBytes(StandardCharsets.UTF_8));
            }
        }
        parts.add(part.toString(StandardCharsets.UTF_8));
        if (parts.size() != 3) {
            throw new IllegalArgumentException("Expected 3 key parts, got " + parts.size() + ": " + key);
        }
        return new DocumentKey(parts.get(0), Integer.valueOf(parts.get(1)), Long.valueOf(parts.get(2)));
    }

    /** The encoded form {@link #valueOf(String)} reads back. */
    @Override
    public String toString() {
        return encode(companyCode) + "~" + docYear + "~" + docNo;
    }

    private static String encode(String part) {
        StringBuilder out = new StringBuilder();
        for (byte b : part.getBytes(StandardCharsets.UTF_8)) {
            char c = (char) (b & 0xff);
            if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '-' || c == '_' || c == '.') {
                out.append(c);
            } else {
                out.append('!').append(HexFormat.of().withUpperCase().toHexDigits(b));
            }
        }
        return out.toString();
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public Integer getDocYear() {
        return docYear;
    }

    public Long getDocNo() {
        return docNo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentKey other)) return false;
        return Objects.equals(companyCode, other.companyCode) && Objects.equals(docYear, other.docYear) && Objects.equals(docNo, other.docNo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(companyCode, docYear, docNo);
    }
}
