package urlshortener.fixtures;


import common.domain.ShortURL;

import java.net.URI;

public class ShortURLFixture {

    public static ShortURL url1() {
        return new ShortURL("1", "http://www.unizar.es/", null, null, null, null, null, false, false,
                null, null, null);
    }

    public static ShortURL url1modified() {
        return new ShortURL("1", "http://www.unizar.org/", null, null, null, null, null, false, false,
                null, null, null);
    }

    public static ShortURL url2() {
        return new ShortURL("2", "http://www.unizar.es/", null, null, null, null, null, false, false,
                null, null, null);
    }

    public static ShortURL url3() {
        return new ShortURL("3", "http://www.google.es/", null, null, null, null, null, false, false,
                null, null, null);
    }

    public static ShortURL badUrl() {
        return new ShortURL(null, null, null, null, null, null, null, false, false,
                null, null, null);
    }

    public static ShortURL urlSponsor() {
        return new ShortURL("3", null, null, "sponsor", null, null, null, false,
                false, null, null, null);
    }

    public static ShortURL urlSafe() {
        return new ShortURL("4", null, null, "sponsor", null, null, null, true, true,
                null, null, null);
    }

    public static ShortURL someUrl() {
        return new ShortURL("someKey", "http://example.com/", null, null, null,
                null, 307, true, true, null, null, null);
    }

    public static ShortURL exampleOrgUrl() {
        return new ShortURL("16a3e3e5", "http://example.org/", URI.create("http://localhost/16a3e3e5"),
                null, null, null, 0, false, true, null, null, null);
    }
}
