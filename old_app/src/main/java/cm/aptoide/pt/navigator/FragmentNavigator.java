package cm.aptoide.pt.navigator;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import rx.Observable;

public interface FragmentNavigator {

  String REQUEST_CODE_EXTRA = "cm.aptoide.pt.view.navigator.extra.REQUEST_CODE";

  void navigateForResult(Fragment fragment, int requestCode, boolean replace);

  void navigateToWithoutBackSave(Fragment fragment, boolean replace);

  void navigateToCleaningBackStack(Fragment fragment, boolean replace);

  String navigateTo(Fragment fragment, boolean replace);

  Observable<Result> results(int requestCode);

  void popWithResult(Result result);

  boolean popBackStack();

  void cleanBackStack();

  Fragment peekLast();

  Fragment getFragment();

  Fragment getFragment(String tag);

  void navigateToDialogFragment(DialogFragment fragment);

  void navigateToDialogForResult(DialogFragment fragment, int requestCode);

  void popDialogWithResult(Result result);

  void popBackStackUntil(String foundFragmentTag);

  int getBackStackEntryCount();

  String getTagByBackStackEntry(int backstackEntry);
}
