package kr.rtustudio.fieldzone.region;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * Global registry for {@link RegionFlag} instances.
 * <p>
 * External plugins can register custom flags via {@link #register(RegionFlag)}
 * and query them via {@link #get(String)}.
 * <p>
 * Built-in flags (e.g., {@link #WARNING}) are registered automatically.
 */
public class RegionFlagRegistry {

    private static final Map<String, RegionFlag> MAP = new Object2ObjectOpenHashMap<>();

    /** Built-in: shows red particles when close to the region boundary */
    public static final RegionFlag WARNING = new RegionFlag("fieldzone", "warning");

    static {
        register(WARNING);
    }

    /**
     * Registers a flag to the global registry.
     * External plugins should call this during {@code onEnable()}.
     *
     * @param flag the flag to register
     */
    public static void register(RegionFlag flag) {
        MAP.put(flag.getKey(), flag);
    }

    /**
     * Unregisters a flag from the global registry.
     *
     * @param flag the flag to unregister
     */
    public static void unregister(RegionFlag flag) {
        MAP.remove(flag.getKey());
    }

    /**
     * Looks up a flag by its full key ({@code namespace:key}) or short key.
     * If the key does not contain {@code ":"}, it defaults to the {@code fieldzone} namespace.
     *
     * @param key the full or short key (case-insensitive)
     * @return the registered flag, or {@code null} if not found
     */
    @Nullable
    public static RegionFlag get(String key) {
        if (key == null) return null;
        if (!key.contains(":")) {
            key = "fieldzone:" + key;
        }
        return MAP.get(key.toLowerCase());
    }

    /**
     * Returns an unmodifiable view of all registered flags.
     */
    public static Collection<RegionFlag> values() {
        return Collections.unmodifiableCollection(MAP.values());
    }

    /**
     * Checks whether a flag with the given full key is currently registered.
     */
    public static boolean isRegistered(String key) {
        return get(key) != null;
    }
}
