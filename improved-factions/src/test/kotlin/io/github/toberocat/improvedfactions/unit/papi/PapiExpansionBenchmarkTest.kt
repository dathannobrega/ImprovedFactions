package io.github.toberocat.improvedfactions.unit.papi

import io.github.toberocat.improvedfactions.database.DatabaseManager.loggedTransaction
import io.github.toberocat.improvedfactions.papi.PapiExpansion
import io.github.toberocat.improvedfactions.unit.ImprovedFactionsTest
import org.bukkit.OfflinePlayer
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class PapiExpansionBenchmarkTest : ImprovedFactionsTest() {
    @Test
    fun `benchmark caching performance`() {
        val player = createTestPlayer()
        val expansion = PapiExpansion(plugin.improvedFactionsConfig)

        // Inject a heavy placeholder for benchmarking
        val field = PapiExpansion::class.java.getDeclaredField("placeholders")
        field.isAccessible = true
        val map = field.get(expansion) as MutableMap<String, (OfflinePlayer) -> String?>
        map["sleep"] = {
            Thread.sleep(5)
            "ok"
        }

        val placeholder = map["sleep"]!!

        val baselineStart = System.currentTimeMillis()
        repeat(50) { loggedTransaction { placeholder(player) } }
        val baselineDuration = System.currentTimeMillis() - baselineStart

        val cachedStart = System.currentTimeMillis()
        repeat(50) { expansion.onRequest(player, "sleep") }
        val cachedDuration = System.currentTimeMillis() - cachedStart

        println("Baseline: ${baselineDuration}ms, Cached: ${cachedDuration}ms")
        assertTrue(cachedDuration < baselineDuration)
    }
}
