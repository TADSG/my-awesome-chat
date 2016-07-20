package com.example.myawesomechat;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.GoogleAuthProvider;

import java.util.Arrays;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends BaseActivity {

    public static final String PUBLIC_ROOM_ID = "public_room_id";
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQ_SIGN_IN = 1001;

    private SignInButton signInButton;

    private RecyclerView userRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FirebaseRecyclerAdapter<User, UserViewHolder> mFirebaseAdapter;

    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.public_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatRoom(PUBLIC_ROOM_ID);
            }
        });

        //新增這段
        initView();
    }

    @Override
    public void onStart() {
        super.onStart();
        setupView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (mFirebaseUser != null) {
            inflater.inflate(R.menu.sign_out_menu, menu);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                setupView();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == REQ_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            Log.d(TAG, "onActivityResult: sign in success.");
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
            } else {
                // Google Sign In failed
                Log.e(TAG, "Google Sign In failed.");
            }
        }
    }

    private void setupView() {
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, show the Sign In button
            signInButton.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(ProgressBar.GONE);
            userRecyclerView.setAdapter(null);
        } else {
            signInButton.setVisibility(View.GONE);
            mFirebaseDatabaseReference
                    .child(User.CHILD_NAME).child(mFirebaseUser.getUid())
                    .setValue(User.fromFirebaseUser(mFirebaseUser));

            if (null == userRecyclerView.getAdapter()) {
                //RealtimeDatabase not support notEqual query
                mFirebaseAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                        User.class,
                        UserViewHolder.layoutResId,
                        UserViewHolder.class,
                        mFirebaseDatabaseReference.child(User.CHILD_NAME)) {

                    @Override
                    protected void populateViewHolder(UserViewHolder viewHolder, final User user, int position) {
                        mProgressBar.setVisibility(ProgressBar.GONE);
                        viewHolder.displayNameTextView.setText(user.getDisplayName());
                        if (user.getPhotoUrl() == null) {
                            viewHolder.thumbImageView.setImageDrawable(
                                    ContextCompat.getDrawable(MainActivity.this, R.drawable.ic_account_circle_black_36dp));
                        } else {
                            Glide.with(MainActivity.this)
                                    .load(user.getPhotoUrl())
                                    .into(viewHolder.thumbImageView);
                        }

                        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //使用兩個使用者的 uid 排序後作為 Room ID，無論 A 去找 B 或是 B 去找 A 都會得到相同聊天室 ID
                                String[] uids = new String[]{user.getUid(), mFirebaseUser.getUid()};
                                Arrays.sort(uids);
                                String roomId = uids[0] + uids[1];

                                openChatRoom(roomId);
                            }
                        });
                    }
                };

                mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
                    @Override
                    public void onItemRangeInserted(int positionStart, int itemCount) {
                        super.onItemRangeInserted(positionStart, itemCount);
                        int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                        int lastVisiblePosition =
                                mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                        // If the recycler view is initially being loaded or the
                        // user is at the bottom of the list, scroll to the bottom
                        // of the list to show the newly added message.
                        if (lastVisiblePosition == -1 ||
                                (positionStart >= (friendlyMessageCount - 1) &&
                                        lastVisiblePosition == (positionStart - 1))) {
                            userRecyclerView.scrollToPosition(positionStart);
                        }
                    }
                });

                userRecyclerView.setAdapter(mFirebaseAdapter);
            }
        }
        invalidateOptionsMenu();
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGooogle:" + acct.getId());
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mFirebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "signInWithCredential:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Log.w(TAG, "signInWithCredential", task.getException());
                            Toast.makeText(MainActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        } else {
                            setupView();
                        }
                    }
                });
    }

    private void openChatRoom(String roomId) {
        Intent openChatroomForUser = new Intent(MainActivity.this, ChatRoomActivity.class)
                .putExtra(ChatRoomActivity.EXTRA_ROOM_ID, roomId);
        startActivity(openChatroomForUser);
    }

    private void initView() {
        signInButton = (SignInButton) findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
                startActivityForResult(signInIntent, REQ_SIGN_IN);
            }
        });

        userRecyclerView = (RecyclerView) findViewById(R.id.user_recycler_view);
        // Initialize ProgressBar and RecyclerView.
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mLinearLayoutManager = new LinearLayoutManager(this);
        userRecyclerView.setLayoutManager(mLinearLayoutManager);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public final static int layoutResId = R.layout.item_user;

        public TextView displayNameTextView;
        public CircleImageView thumbImageView;

        public UserViewHolder(View v) {
            super(v);
            displayNameTextView = (TextView) itemView.findViewById(R.id.display_name);
            thumbImageView = (CircleImageView) itemView.findViewById(R.id.thumb);
        }
    }
}
