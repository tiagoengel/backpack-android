package net.skyscanner.backpack.util

import android.content.ContextWrapper
import android.graphics.Color
import android.util.TypedValue
import android.view.ContextThemeWrapper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import net.skyscanner.backpack.test.R
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BpkThemeTest {
  private lateinit var activity: AppCompatActivity

  @get:Rule
  internal var activityRule: ActivityTestRule<TestActivity> =
    ActivityTestRule(TestActivity::class.java)

  private val blue500 by unsafeLazy { ContextCompat.getColor(activity, R.color.bpkBlue500) }
  private val customBlue = Color.parseColor("#9B104A")

  @Before
  fun setUp() {
    activity = activityRule.activity
  }

  @Test
  fun test_primary_color() {
    Assert.assertEquals("default", blue500, BpkTheme.getPrimaryColor(activity))

    Assert.assertEquals(
      "Themed",
      customBlue,
      BpkTheme.getPrimaryColor(ContextThemeWrapper(activity, R.style.TestThemeUtilsCustomColors)))
  }

  @Test
  fun test_wrapContextWithDefaults() {
    val t = TypedValue()

    Assert.assertFalse(activity.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true))

    val newContext = BpkTheme.wrapContextWithDefaults(activity)
    newContext.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true)
    Assert.assertEquals(blue500, t.data)

    val withTheme = ContextThemeWrapper(activity, R.style.TestThemeUtilsWrapWithDefaults)
    val withThemeAndDefault = BpkTheme.wrapContextWithDefaults(withTheme)
    withThemeAndDefault.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true)

    Assert.assertEquals(customBlue, t.data)
  }

  @Test
  fun test_applyDefaultsToContext() {
    var testContext = ContextWrapper(activity)
    val t = TypedValue()

    Assert.assertFalse(activity.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true))

    BpkTheme.applyDefaultsToContext(testContext)
    testContext.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true)
    Assert.assertEquals(blue500, t.data)

    val withTheme = ContextThemeWrapper(activity, R.style.TestThemeUtilsWrapWithDefaults)
    BpkTheme.applyDefaultsToContext(withTheme)
    withTheme.theme.resolveAttribute(R.attr.bpkPrimaryColor, t, true)

    Assert.assertEquals(customBlue, t.data)
  }
}
