package io.github.toberocat.improvedfactions.papi

import io.github.toberocat.improvedfactions.ImprovedFactionsPlugin
import io.github.toberocat.improvedfactions.config.ImprovedFactionsConfig
import io.github.toberocat.improvedfactions.user.factionUser
import io.github.toberocat.improvedfactions.utils.toOfflinePlayer
import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap


/**
 * Created: 20.07.2023
 * @author Tobias Madlberger (Tobias)
 */

class PapiExpansion(private val pluginConfig: ImprovedFactionsConfig) : PlaceholderExpansion() {
    private data class CachedValue(val value: String?, val expiresAt: Long)

    private val placeholders = HashMap<String, (player: OfflinePlayer) -> String?>()
    private val cache = ConcurrentHashMap<UUID, MutableMap<String, CachedValue>>()

    companion object {
        private const val CACHE_DURATION_MS = 5_000L
    }

    init {
        placeholders["owner"] = { it.factionUser().faction()?.owner?.toOfflinePlayer()?.name }
        placeholders["name"] = { it.factionUser().faction()?.name }
        placeholders["rank"] = { it.factionUser().rank().name }
        placeholders["members_total"] = {
            it.factionUser().faction()?.members()?.count()?.toString()
        }
        placeholders["members_online"] = {
            it.factionUser().faction()?.members()?.count { user ->
                user.offlinePlayer().isOnline
            }?.toString()
        }

        ImprovedFactionsPlugin.instance.moduleManager.loadPapiPlaceholders(placeholders)
    }

    override fun getAuthor(): String = "Tobero"

    override fun getIdentifier(): String = "faction"

    override fun getVersion(): String = "1.0.0"

    override fun persist(): Boolean = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        if (player == null) return pluginConfig.defaultPlaceholders[params]

        val playerCache = cache.computeIfAbsent(player.uniqueId) { ConcurrentHashMap() }
        val now = System.currentTimeMillis()
        val cached = playerCache[params]
        if (cached != null && cached.expiresAt > now) {
            return cached.value
        }

        val value = loggedTransaction { placeholders[params]?.invoke(player) }
        playerCache[params] = CachedValue(value, now + CACHE_DURATION_MS)

        return value ?: pluginConfig.defaultPlaceholders[params]
    }
}
