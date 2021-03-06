/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.api.internal.changedetection.state;

import org.gradle.api.invocation.Gradle;
import org.gradle.cache.CacheRepository;
import org.gradle.cache.PersistentCache;
import org.gradle.cache.PersistentIndexedCache;
import org.gradle.cache.PersistentIndexedCacheParameters;
import org.gradle.cache.internal.CacheDecorator;
import org.gradle.cache.internal.FileLockManager;
import org.gradle.internal.Factory;
import org.gradle.internal.serialize.Serializer;

import java.io.Closeable;

import static org.gradle.cache.internal.filelock.LockOptionsBuilder.mode;

public class DefaultTaskArtifactStateCacheAccess implements TaskArtifactStateCacheAccess, Closeable {
    private final CacheDecorator cacheDecorator;
    private final PersistentCache cache;

    public DefaultTaskArtifactStateCacheAccess(Gradle gradle, CacheRepository cacheRepository, CacheDecorator decorator) {
        this.cacheDecorator = decorator;
        cache = cacheRepository
                .cache(gradle, "taskArtifacts")
                .withDisplayName("task history cache")
                .withLockOptions(mode(FileLockManager.LockMode.None)) // Lock on demand
                .open();
    }

    public void close() {
        cache.close();
    }

    public <K, V> PersistentIndexedCache<K, V> createCache(final String cacheName, final Class<K> keyType, final Serializer<V> valueSerializer) {
        PersistentIndexedCacheParameters<K, V> parameters = new PersistentIndexedCacheParameters<K, V>(cacheName, keyType, valueSerializer)
                .cacheDecorator(cacheDecorator);
        return cache.createCache(parameters);
    }

    @Override
    public void flush() {
        cache.flush();
    }

    public <T> T useCache(String operationDisplayName, Factory<? extends T> action) {
        return action.create();
    }

    public void useCache(String operationDisplayName, Runnable action) {
        action.run();
    }

    public <T> T longRunningOperation(String operationDisplayName, Factory<? extends T> action) {
        return action.create();
    }

    public void longRunningOperation(String operationDisplayName, Runnable action) {
        action.run();
    }
}