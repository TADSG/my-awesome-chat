package com.example.myawesomechat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends BaseActivity {

    public static final String PUBLIC_ROOM_ID = "public_room_id";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //新增這段
        findViewById(R.id.public_room).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openChatRoom(PUBLIC_ROOM_ID);
            }
        });
    }

    private void openChatRoom(String roomId) {
        Intent openChatroomForUser = new Intent(MainActivity.this, ChatRoomActivity.class)
                .putExtra(ChatRoomActivity.EXTRA_ROOM_ID, roomId);
        startActivity(openChatroomForUser);
    }
}
