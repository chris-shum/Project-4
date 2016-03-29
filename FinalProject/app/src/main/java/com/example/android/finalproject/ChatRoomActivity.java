package com.example.android.finalproject;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.finalproject.RecyclerViewTest.Chat;
import com.example.android.finalproject.RecyclerViewTest.ChatHolder;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.Query;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseRecyclerAdapter;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

public class ChatRoomActivity extends ListActivity {

    private static final String FIREBASE_URL = "https://blazing-inferno-2663.firebaseio.com/";
    private Firebase mFirebaseRef;
    private ValueEventListener mConnectedListener;
    private String mUsername;
    public String mNativeLanguage;
    String mLanguage;
    EditText mInputText;
    public static Language translateToLanguage = Language.ENGLISH;
    public static boolean myText = true;
    Toolbar chatRoomToolbar;

    private RecyclerView mMessages;
    private FirebaseRecyclerAdapter<Chat, ChatHolder> mRecycleViewAdapter;
    private Query mChatRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room);

        mFirebaseRef.setAndroidContext(this);
        mInputText = (EditText) findViewById(R.id.messageInput);

        Intent intent = getIntent();
        mUsername = intent.getStringExtra("Name");
        mNativeLanguage = intent.getStringExtra("NativeLanguage");
        mLanguage = intent.getStringExtra("Language");
        final Translator translator = Translator.getInstance();
        translator.setUsername(mUsername);
        translator.setNativeLanguage(mNativeLanguage);
        translator.setLanguage(mLanguage);

        chatRoomToolbar = (Toolbar) findViewById(R.id.chatRoomToolbar);
        chatRoomToolbar.setTitle("Chatting as " + mUsername + " in the " + mLanguage + " room");

        // Setup our Firebase mFirebaseRef
        if (mLanguage == null) {
            mFirebaseRef = new Firebase(FIREBASE_URL).child("default");
        } else {
            mFirebaseRef = new Firebase(FIREBASE_URL).child(mLanguage);
        }
        mChatRef = mFirebaseRef.limitToLast(50);


        // Setup our input methods. Enter key on the keyboard or pushing the send button
        final EditText inputText = (EditText) findViewById(R.id.messageInput);
        inputText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_NULL && keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                    sendMessage();
                }
                return true;
            }
        });
        findViewById(R.id.sendButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        findViewById(R.id.sendButton).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                try {
                    String inputText = mInputText.getText().toString();
                    translator.translatedText(inputText, mInputText ,mLanguage);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Setup our view and list adapter. Ensure it scrolls to the bottom as data changes
//        final ListView listView = getListView();
//        // Tell our list adapter that we only want 50 messages at a time
//        mChatListAdapter = new ChatListAdapter(mFirebaseRef.limit(50), this, R.layout.chat_message, mUsername);
//        listView.setAdapter(mChatListAdapter);
//        mChatListAdapter.registerDataSetObserver(new DataSetObserver() {
//            @Override
//            public void onChanged() {
//                super.onChanged();
//                listView.setSelection(mChatListAdapter.getCount() - 1);
//            }
//        });


        mMessages = (RecyclerView) findViewById(R.id.listRecyclerView);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setReverseLayout(false);

        mMessages.setHasFixedSize(false);
        mMessages.setLayoutManager(manager);

        mRecycleViewAdapter = new FirebaseRecyclerAdapter<Chat, ChatHolder>(Chat.class, R.layout.message, ChatHolder.class, mChatRef) {
            @Override
            public void populateViewHolder(ChatHolder chatView, Chat chat, int position) {
                chatView.setName(chat.getAuthor());
                chatView.setText(chat.getMessage());
                String author = chat.getAuthor();
                if (author != null && author.equals(mUsername)) {
                    chatView.setIsSender(true);
                } else {
                    chatView.setIsSender(false);
                }
            }
        };
        mMessages.setAdapter(mRecycleViewAdapter);


//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//                TextView clickedText = (TextView) view.findViewById(R.id.message);
//                try {
//                    myText = false;
//                    translatedText(clickedText.getText().toString(), mNativeLanguage);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });

        // Finally, a little indication of connection status
        mConnectedListener = mFirebaseRef.getRoot().child(".info/connected").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                boolean connected = (Boolean) dataSnapshot.getValue();
                if (connected) {
                    Toast.makeText(ChatRoomActivity.this, "Connected to Firebase", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ChatRoomActivity.this, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // No-op
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
//        mChatListAdapter.cleanup();
        mRecycleViewAdapter.cleanup();
    }


    private void sendMessage() {
        mInputText = (EditText) findViewById(R.id.messageInput);
        String input = mInputText.getText().toString();
        if (!input.equals("")) {
            // Create our 'model', a Chat object
//            String timeStamp = new SimpleDateFormat("MM.dd.HH.mm").format(new Date());
            Chat chat = new Chat(input, mUsername);
            // Create a new, auto-generated child of that chat location, and save our chat data there
            mFirebaseRef.push().setValue(chat);
            mInputText.setText("");
        }
    }






    //translation portion below
    public void translatedText(String clickedText, String translatedToLanguage) throws Exception {
        //Replace client_id and client_secret with your own.
        Translate.setClientId("Project4Shum");
        Translate.setClientSecret("AXhYWTlsSQuWjQ21EnuuzmR64ymaAONk/Oe1wnfU0AI=");
        String clickedString = clickedText;
        BackgroundTranslation backgroundTranslation = new BackgroundTranslation();
        setTranslateToLanguage(translatedToLanguage);
        backgroundTranslation.execute(clickedString);
    }

    public static void setTranslateToLanguage(String language) {
        switch (language) {
            case "English":
                translateToLanguage = Language.ENGLISH;
                break;
            case "Spanish":
                translateToLanguage = Language.SPANISH;
                break;
            case "French":
                translateToLanguage = Language.FRENCH;
                break;
            default:
                translateToLanguage = Language.ENGLISH;
        }
    }

    class BackgroundTranslation extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... inputString) {
            String translatedString = null;
            try {
                translatedString = Translate.execute(inputString[0], translateToLanguage);
                Log.d("Translate", "It worked");
            } catch (Exception e) {
                e.printStackTrace();
                Log.d("Translate", "It didn't work");
            }
            Log.d("Translate", "Original: " + inputString[0]);
            return translatedString;
        }

        @Override
        protected void onPostExecute(String translatedString) {
            Log.d("Translate", "Translated: " + translatedString);
            if (myText) {
                EditText inputText = (EditText) findViewById(R.id.messageInput);
                inputText.setText(translatedString);
            } else {
                Toast.makeText(ChatRoomActivity.this, translatedString, Toast.LENGTH_SHORT).show();
            }
            super.onPostExecute(translatedString);
        }
    }
}
