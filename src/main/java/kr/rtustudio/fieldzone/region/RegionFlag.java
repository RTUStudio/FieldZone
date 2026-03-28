package kr.rtustudio.fieldzone.region;

import org.bukkit.plugin.Plugin;

import java.util.Collection;

/**
 * Represents a region flag with a namespace (owner) and key.
 * External plugins can create and register custom flags via {@link RegionFlagRegistry}.
 * <p>
 * Format: {@code namespace:key} (e.g., {@code fieldzone:warning}, {@code myplugin:no_lightning})
 */
public record RegionFlag(String namespace, String key) {

    public static final RegionFlag WARNING = RegionFlagRegistry.WARNING;

    public RegionFlag {
        namespace = namespace.toLowerCase();
        key = key.toLowerCase();
    }

    /**
     * Creates a new RegionFlag using the plugin's name as the namespace.
     *
     * @param plugin the owner plugin
     * @param key    the flag identifier
     * @return a new RegionFlag instance
     */
    public static RegionFlag create(Plugin plugin, String key) {
        return new RegionFlag(plugin.getName(), key);
    }

    /**
     * Returns the full key in {@code namespace:key} format.
     */
    public String getKey() {
        return namespace + ":" + key;
    }

    /**
     * Returns all registered flags from the global registry.
     */
    public static Collection<RegionFlag> values() {
        return RegionFlagRegistry.values();
    }

    @Override
    public String toString() {
        return getKey();
    }
}
