package ch.cyberduck.core.cryptomator.features;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.Cache;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.cryptomator.CryptoPathCache;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Vault;

public class CryptoAttributesFeature implements AttributesFinder {

    private final Session<?> session;
    private final AttributesFinder delegate;
    private final Vault vault;

    public CryptoAttributesFeature(final Session<?> session, final AttributesFinder delegate, final Vault cryptomator) {
        this.session = session;
        this.delegate = delegate;
        this.vault = cryptomator;
    }

    @Override
    public PathAttributes find(final Path file) throws BackgroundException {
        final PathAttributes attributes = delegate.find(vault.encrypt(session, file));
        attributes.setSize(vault.toCleartextSize(attributes.getSize()));
        return attributes;
    }

    @Override
    public AttributesFinder withCache(final Cache<Path> cache) {
        delegate.withCache(new CryptoPathCache(cache));
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CryptoAttributesFeature{");
        sb.append("delegate=").append(delegate);
        sb.append('}');
        return sb.toString();
    }
}
