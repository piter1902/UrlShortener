package urlshortener.fixtures;

import common.domain.ShortURL;
import urlshortener.domain.Click;

public class ClickFixture {

    public static Click click(ShortURL su) {
        return new Click(null, su.getHash(), null, null, null, null, null, null);
    }
}
