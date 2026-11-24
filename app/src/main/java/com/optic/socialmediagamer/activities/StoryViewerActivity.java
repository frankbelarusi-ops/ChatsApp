package com.optic.socialmediagamer.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.optic.socialmediagamer.R;
import com.optic.socialmediagamer.utils.RelativeTime;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class StoryViewerActivity extends AppCompatActivity {

    private ProgressBar mProgressBar;
    private CircleImageView mCircleAuthor;
    private TextView mTextViewAuthor;
    private TextView mTextViewTime;
    private ImageView mImageClose;
    private ImageView mImageContent;
    private TextView mTextViewCaption;

    private Handler mHandler = new Handler();
    private int mProgressStatus = 0;
    private static final int STORY_DURATION_MS = 5000;
    private static final int UPDATE_INTERVAL_MS = 50;

    private Runnable mProgressRunnable = new Runnable() {
        @Override
        public void run() {
            mProgressStatus += (UPDATE_INTERVAL_MS * 100) / STORY_DURATION_MS;
            mProgressBar.setProgress(mProgressStatus);
            if (mProgressStatus < 100) {
                mHandler.postDelayed(this, UPDATE_INTERVAL_MS);
            } else {
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_viewer);

        mProgressBar = findViewById(R.id.progressBarStory);
        mCircleAuthor = findViewById(R.id.circleImageViewStoryAuthor);
        mTextViewAuthor = findViewById(R.id.textViewStoryAuthor);
        mTextViewTime = findViewById(R.id.textViewStoryTime);
        mImageClose = findViewById(R.id.imageViewCloseStory);
        mImageContent = findViewById(R.id.imageViewStoryContent);
        mTextViewCaption = findViewById(R.id.textViewStoryCaption);

        String username = getIntent().getStringExtra("username");
        String userPhoto = getIntent().getStringExtra("userPhoto");
        String imageUrl = getIntent().getStringExtra("imageUrl");
        String caption = getIntent().getStringExtra("caption");
        long timestamp = getIntent().getLongExtra("timestamp", 0);

        if (username != null) {
            mTextViewAuthor.setText(username);
        }
        if (timestamp > 0) {
            mTextViewTime.setText(RelativeTime.getTimeAgo(timestamp, this));
        }
        if (userPhoto != null && !userPhoto.isEmpty()) {
            Picasso.with(this).load(userPhoto).into(mCircleAuthor);
        }
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Picasso.with(this).load(imageUrl).into(mImageContent);
        }
        if (caption != null && !caption.isEmpty()) {
            mTextViewCaption.setVisibility(View.VISIBLE);
            mTextViewCaption.setText(caption);
        }

        mImageClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mImageContent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mHandler.post(mProgressRunnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mProgressRunnable);
    }
}
