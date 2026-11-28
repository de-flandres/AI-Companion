package dev.aicompanion.gametest;

import dev.aicompanion.AICompanionMod;
import dev.aicompanion.client.CompanionSwitchLayout;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * The two distance settings of the screens, as arithmetic: the pickup radius turns through the whole 4..32 the setting allows and
 * reaches 32 (the old step of eight went 4, 12, 20, 28 and never came to it), and the search distance is a number the player
 * types into the settings screen's field, taken only as a whole number in the setting's own 16..128 — 40 and 100 included, which
 * no cycle of 16/32/64/128 could give (NUMIN, #105). No client is needed: the geometry and the parse are pure numbers.
 */
@GameTestHolder(AICompanionMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class CompanionBehaviorScreenTests {
    @GameTest(template = "empty")
    public static void thePickUpRadiusCycleCoversTheWholeRangeAndReachesThirtyTwo(GameTestHelper h) {
        List<Integer> seen = new ArrayList<>();
        int value = CompanionSwitchLayout.PICK_UP_RADIUS_MIN;
        for (int press = 0; press < 16; press++) {
            seen.add(value);
            value = CompanionSwitchLayout.nextPickUpRadius(value);
            if (value == CompanionSwitchLayout.PICK_UP_RADIUS_MIN) break;
        }
        h.assertTrue(seen.equals(List.of(4, 8, 12, 16, 20, 24, 28, 32)),
                "The radius cycle skips a value or never reaches 32: " + seen);
        h.assertTrue(CompanionSwitchLayout.nextPickUpRadius(32) == 4, "32 does not turn back to 4");
        h.assertTrue(CompanionSwitchLayout.nextPickUpRadius(30) == 4,
                "A value off the cycle (30) does not come back into it: " + CompanionSwitchLayout.nextPickUpRadius(30));
        h.succeed();
    }

    /**
     * The search distance is typed: every whole number in 16..128 is taken as itself — 40 and 100, the numbers a cycle of
     * 16/32/64/128 never gave — and nothing else is: empty, spaces, letters, a decimal point, a number under or over the range.
     */
    @GameTest(template = "empty")
    public static void theSearchRadiusIsTypedAsAWholeNumberInItsOwnRange(GameTestHelper h) {
        h.assertTrue(CompanionSwitchLayout.SEARCH_RADIUS_MIN == 16 && CompanionSwitchLayout.SEARCH_RADIUS_MAX == 128,
                "The setting's own range is not 16..128");
        for (int radius = CompanionSwitchLayout.SEARCH_RADIUS_MIN; radius <= CompanionSwitchLayout.SEARCH_RADIUS_MAX; radius++)
            h.assertTrue(CompanionSwitchLayout.typedSearchRadius(Integer.toString(radius)) == radius,
                    "The typed number " + radius + " is refused or read as another");
        h.assertTrue(CompanionSwitchLayout.typedSearchRadius("40") == 40, "40, typed, is not taken as 40");
        h.assertTrue(CompanionSwitchLayout.typedSearchRadius("100") == 100, "100, typed, is not taken as 100");
        h.assertTrue(CompanionSwitchLayout.typedSearchRadius(" 64 ") == 64, "A typed number with spaces around it is refused");
        for (String refused : List.of("", "   ", "15", "129", "0", "-32", "abc", "64.0", "4 0", "1e2", "1000"))
            h.assertTrue(CompanionSwitchLayout.typedSearchRadius(refused) < 0,
                    "«" + refused + "» is taken as " + CompanionSwitchLayout.typedSearchRadius(refused) + ", not refused");
        h.succeed();
    }
}
