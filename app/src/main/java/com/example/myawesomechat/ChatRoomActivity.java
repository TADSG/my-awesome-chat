package com.example.myawesomechat;

import android.os.Bundle;

public class ChatRoomActivity extends BaseActivity {

    private static final String TAG = ChatRoomActivity.class.getSimpleName();

    public final static String EXTRA_PREFIX = ChatRoomActivity.class.getName();
    public final static String EXTRA_ROOM_ID = EXTRA_PREFIX + ".ROOM_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
    }
}
