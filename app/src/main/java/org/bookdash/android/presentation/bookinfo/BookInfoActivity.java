package org.bookdash.android.presentation.bookinfo;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.transition.Transition;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.bookdash.android.BR;
import org.bookdash.android.Injection;
import org.bookdash.android.R;
import org.bookdash.android.domain.pojo.BookContributor;
import org.bookdash.android.domain.pojo.BookDetail;
import org.bookdash.android.domain.pojo.gson.BookPages;
import org.bookdash.android.presentation.activity.BaseAppCompatActivity;
import org.bookdash.android.presentation.readbook.BookDetailActivity;

import java.util.List;

import mbanje.kurt.fabbutton.FabButton;


public class BookInfoActivity extends BaseAppCompatActivity implements BookInfoContract.View {
    private static final String TAG = "BookInfoActivity";

    public static final String BOOK_PARCEL = "book_parcel";
    /**
     * Presenter object
     */
    private BookInfoContract.UserActionsListener actionsListener;

    /**
     * Views
     */
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FabButton floatingActionButton;
    private View gradientBackground;
    private LinearLayout contributorLinearLayout;
    private View scrollView;
    private ImageView imageViewBook;
    private AppBarLayout appBarLayout;
    private CoordinatorLayout coordinatorLayout;
    private View errorLayout;
    private TextView errorText;
    private ViewDataBinding binding;
    private ProgressBar loadingProgressBar;
    private CardView contributorCard, mainBookCard;
    private Toolbar toolbar;
    private ActionBar actionBar;
    private BookDetail bookInfo;
    private Action viewAction;
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;
    private Button errorRetryButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_book_information);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getSharedElementReturnTransition().addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {
                    floatingActionButton.animate().scaleY(0).scaleX(0).setInterpolator(new AccelerateInterpolator()).setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime)).start();

                }

                @Override
                public void onTransitionEnd(Transition transition) {
                    enterAnimation();
                }

                @Override
                public void onTransitionCancel(Transition transition) {

                }

                @Override
                public void onTransitionPause(Transition transition) {

                }

                @Override
                public void onTransitionResume(Transition transition) {

                }
            });
            postponeEnterTransition();
        }
        contributorLinearLayout = (LinearLayout) findViewById(R.id.linear_layout_contributors);
        contributorCard = (CardView) findViewById(R.id.contributor_card);
        imageViewBook = (ImageView) findViewById(R.id.image_view_book_cover);
        appBarLayout = (AppBarLayout) findViewById(R.id.app_bar_layout);
        mainBookCard = (CardView) findViewById(R.id.card_view_main_book_info);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator_layout_content);
        errorLayout = findViewById(R.id.linear_layout_error);
        errorText = (TextView) findViewById(R.id.text_view_error_screen);

        errorRetryButton = (Button) findViewById(R.id.button_retry);
        loadingProgressBar = (ProgressBar) findViewById(R.id.activity_loading_book_info);
        setSupportActionBar(toolbar);
        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        scrollView = findViewById(R.id.scrollViewBookInfo);
        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        gradientBackground = findViewById(R.id.toolbar_background_gradient);
        floatingActionButton = (FabButton) findViewById(R.id.fab_download);
        floatingActionButton.setScaleX(0);
        floatingActionButton.setScaleY(0);
        binding.setVariable(BR.download_click, new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bookInfo == null) {
                    showSnackBarMessage(R.string.book_not_available);
                    return;
                }
                if (!bookInfo.isDownloadedAlready()){
                    floatingActionButton.resetIcon();
                    floatingActionButton.showProgress(true);
                    floatingActionButton.setProgress(0, true);
                }
                actionsListener.downloadBook(bookInfo);
            }
        });
        actionsListener = new BookInfoPresenter(this.getApplicationContext(), this, Injection.provideBookRepo());
        calculateLayoutHeight();
        imageViewBook.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver
                .OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                imageViewBook.getViewTreeObserver().removeOnPreDrawListener(this);

                enterAnimation();
                return true;
            }
        });
        final BookDetail bookDetailParcelable = getIntent().getParcelableExtra(BOOK_PARCEL);

        if (bookDetailParcelable != null) {
            String bookDetailId = bookDetailParcelable.getKey();
            startLoadingBook(bookDetailId);
            actionsListener.loadImage(bookDetailParcelable.getBook_cover_page_url());
        } else {
            onNewIntent(getIntent());
        }
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_book_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_share_book) {
            actionsListener.shareBookClicked(bookInfo);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void startLoadingBook(final String bookDetailId) {
        errorRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionsListener.loadBookInformation(bookDetailId);
            }
        });

        actionsListener.loadBookInformation(bookDetailId);
        showBookDetailView();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        String data = intent.getDataString();
        Log.d(TAG, "onNewIntent() called: action" + action);

        if (Intent.ACTION_VIEW.equals(action) && data != null) {
            Uri uri =  Uri.parse(data);
            String bookId = uri.getLastPathSegment();
            String invitationId = uri.getQueryParameter("invitation_id");
            Log.d(TAG, "Action View: book id:" + bookId + ". Full URL:" + uri.toString() + ". InvitationId:" + invitationId);
            startLoadingBook(bookId);
        }
    }

    private void enterAnimation() {

        floatingActionButton.setScaleX(0);
        floatingActionButton.setScaleY(0);
        floatingActionButton.animate().setStartDelay(500).scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator()).setDuration(getResources().getInteger(android.R.integer.config_mediumAnimTime)).start();


    }


    @SuppressWarnings("SuspiciousNameCombination")
    private void calculateLayoutHeight() {
        if (getResources().getConfiguration().orientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT) {
            Log.d(TAG, "Setting image height");
            DisplayMetrics dMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dMetrics);
            CollapsingToolbarLayout.LayoutParams lp = (CollapsingToolbarLayout.LayoutParams) imageViewBook.getLayoutParams();
            lp.height = dMetrics.widthPixels;
            imageViewBook.setLayoutParams(lp);

            CoordinatorLayout.LayoutParams lp2 = (CoordinatorLayout.LayoutParams) appBarLayout.getLayoutParams();
            lp2.height = dMetrics.widthPixels;
            appBarLayout.setLayoutParams(lp2);
        }
    }


    @Override
    protected String getScreenName() {
        return "BookInfoActivity";
    }

    @Override
    public void showProgress(boolean visible) {
        // errorLayout.setVisibility(View.GONE);
        //  loadingProgressBar.setVisibility(visible ? View.VISIBLE : View.GONE);

    }

    @Override
    public void showBookDetailView() {
        mainBookCard.setVisibility(View.VISIBLE);
        loadingProgressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.GONE);
        coordinatorLayout.setVisibility(View.VISIBLE);
        scrollView.setVisibility(View.VISIBLE);
        floatingActionButton.setVisibility(View.VISIBLE);
    }

    @Override
    public void showError(String errorMessage) {
        loadingProgressBar.setVisibility(View.GONE);
        errorLayout.setVisibility(View.VISIBLE);
        errorText.setText(errorMessage);
    }

    @Override
    public void showSnackBarMessage(int message) {
        Snackbar.make(scrollView, message, Snackbar.LENGTH_LONG).show();

    }
    private int progress= 0;
    @Override
    public void showDownloadProgress(final int downloadProgress) {
        if (progress == downloadProgress){
            return;
        }
        progress = downloadProgress;
        Log.d(TAG, "Download progress:" + downloadProgress);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                floatingActionButton.setProgress(downloadProgress, true);
            }
        });

    }

    @Override
    public void showDownloadFinished() {
        Log.d(TAG, "Download finished");
        floatingActionButton.resetIcon();
        floatingActionButton.setProgress(100, false);
    }

    @Override
    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(title);
        }
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setTitle(title);
        }
    }

    @Override
    public void setBookInfoBinding(BookDetail bookInfo) {
        if (client!=null) {
            client.connect();
        }
        if (bookInfo == null){
            return;
        }

        this.bookInfo = bookInfo;
        binding.setVariable(BR.book_info, bookInfo);
        actionsListener.loadImage(bookInfo.getBook_cover_page_url());
        viewAction = Action.newAction(
                Action.TYPE_VIEW,
                bookInfo.getBook_title(),
                bookInfo.getWebUrl() == null ? null : Uri.parse(bookInfo.getWebUrl()),
                Uri.parse("android-app://org.bookdash.android/http/bookdash.org/books/" + bookInfo.getKey())
        );
        if (viewAction != null && client!=null) {
            AppIndex.AppIndexApi.start(client, viewAction);
        }
    }

    @Override
    public void openBook(BookDetail bookDetail, BookPages bookPages, String location) {
        if (isFinishing()) {
            return;
        }
        Intent intent = new Intent(BookInfoActivity.this, BookDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(BookDetailActivity.BOOK_PAGES, bookPages);
        intent.putExtra(BookDetailActivity.BOOK_ARG, bookDetail.getBook_title());
        intent.putExtra(BookDetailActivity.LOCATION_BOOK, location);
        startActivity(intent);
    }

    @Override
    public void showContributors(List<BookContributor> list) {
        if (list == null || list.size() == 0) {
            contributorCard.setVisibility(View.GONE);
            return;
        }
        contributorCard.setVisibility(View.VISIBLE);
        contributorLinearLayout.removeAllViews();
        for (BookContributor b : list) {
            Log.d(TAG, "Book contributor:" + b.getContributor().getName());
            View v = LayoutInflater.from(BookInfoActivity.this).inflate(R.layout.list_item_contributor, contributorLinearLayout, false);
            setContributorInfo(v, b);
            contributorLinearLayout.addView(v);
        }
    }

    @Override
    public void onImageLoaded(Bitmap bitmap) {
        imageViewBook.setImageBitmap(bitmap);

    }

    @Override
    public void setStatusBarColor(int color) {
        if (isFinishing()) {
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(color);
        }
    }

    @Override
    public void setAccentColor(int accentColor) {
        floatingActionButton.setColor(accentColor);

    }

    @Override
    public void setToolbarColor(int color) {
        if (collapsingToolbarLayout != null) {
            collapsingToolbarLayout.setStatusBarScrimColor(color);
            collapsingToolbarLayout.setContentScrimColor(color);
        } else {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
        floatingActionButton.setRingProgressColor(color);

        if (gradientBackground != null) {
            GradientDrawable gradientDrawable = new GradientDrawable();
            gradientDrawable.setColor(color);
            gradientDrawable.setAlpha(140);
            gradientBackground.setBackground(gradientDrawable);
        }
    }

    @Override
    public void sendShareEvent(String bookTitle) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.sharing_book_title, bookTitle));
        sendIntent.setType("text/plain");
        startActivity(sendIntent);
    }

    private void setContributorInfo(@NonNull View v, @NonNull BookContributor bookContributor) {
        TextView textViewContributor = (TextView) v.findViewById(R.id.textViewContributorName);
        TextView textViewRole = (TextView) v.findViewById(R.id.textViewRole);
        textViewContributor.setText(bookContributor.getContributor().getName());
        textViewRole.setText(bookContributor.getContributor().getRole());

        final ImageView imageView = (ImageView) v.findViewById(R.id.imageViewContributorAvatar);
        String pfAvatar = bookContributor.getContributor().getAvatarUrl();
        if (pfAvatar == null) {
            return;
        }

      //TODO
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public void onStop() {
        super.onStop();

        if (viewAction != null) {
            AppIndex.AppIndexApi.end(client, viewAction);

        }
        client.disconnect();
    }
}
