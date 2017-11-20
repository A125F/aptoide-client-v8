package cm.aptoide.pt;

import android.support.test.espresso.AmbiguousViewMatcherException;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import cm.aptoide.pt.view.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static cm.aptoide.pt.UITests.NUMBER_OF_RETRIES;
import static cm.aptoide.pt.UITests.skipWizard;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;

/**
 * Created by jose_messejana on 24-10-2017.
 */

@RunWith(AndroidJUnit4.class)
public class SettingsUITests {
  private final String MATURE_APP = "Roullete Sex (Roleta do Sexo)";
  private final String MATURE_SEARCH = "kaoiproduct.roulletesexfree";
  @Rule public ActivityTestRule<MainActivity> mActivityRule =
      new ActivityTestRule<>(MainActivity.class);
  @Rule public RetryTestRule retry = new RetryTestRule(NUMBER_OF_RETRIES);

  @Before public void setUp() {
    if (UITests.isFirstTime()) {
      skipWizard();
    }
  }

  @Test public void matureTest() {
    goToSettings();
    onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(11));
    onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(11, click()));
    try {
      onView(withText(R.string.yes)).perform(click());
    } catch (NoMatchingViewException e) {
      onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(11));
      onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(11, click()));
      onView(withText(R.string.yes)).perform(click());
    }
    pressBackButton();
    barOnlySearchApp(MATURE_SEARCH);
    //
    onView(withId(R.id.search_src_text)).perform(pressImeActionButton());
    try {
      onView(withText(MATURE_APP)).check(matches(isDisplayed()));
    } catch (AmbiguousViewMatcherException e) {
    }
    pressBackButton();
    goToSettings();
    onView(withId(R.id.list)).perform(RecyclerViewActions.scrollToPosition(11));
    onView(withId(R.id.list)).perform(RecyclerViewActions.actionOnItemAtPosition(11, click()));
    pressBackButton();
    barOnlySearchApp(MATURE_SEARCH);
    onView(withId(R.id.search_src_text)).perform(pressImeActionButton());
    try {
      onView(withText(MATURE_APP)).check(matches(not(isDisplayed())));
    } catch (NoMatchingViewException e1) {
    }
  }

  private void barOnlySearchApp(String app){
    onView(withId(R.id.action_search)).perform(click());
    onView(withId(R.id.search_src_text)).perform(replaceText(app));
  }

  private void goToSettings(){
    onView(withId(R.id.drawer_layout)).perform(DrawerActions.open());
    onView(withText(R.string.drawer_title_settings)).perform(click());
  }

  private void pressBackButton() {
    ViewInteraction appCompatImageButton2 = onView(
        allOf(withContentDescription("Navigate up"), withParent(withId(R.id.toolbar)),
            isDisplayed()));
    appCompatImageButton2.perform(click());
  }
}
