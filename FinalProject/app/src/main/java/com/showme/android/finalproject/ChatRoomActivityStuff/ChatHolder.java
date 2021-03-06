package com.showme.android.finalproject.ChatRoomActivityStuff;

import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.showme.android.finalproject.R;
import com.showme.android.finalproject.Singletons.TranslatorSingleton;

/**
 * Created by ShowMe on 3/28/16.
 */
public class ChatHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    View mView;
    RelativeLayout relativeLayout;

    public ChatHolder(View itemView) {
        super(itemView);
        mView = itemView;
        relativeLayout = (RelativeLayout) itemView.findViewById(R.id.message_container);
    }

    public void setIsSender(Boolean isSender) {
        FrameLayout left_arrow = (FrameLayout) mView.findViewById(R.id.left_arrow);
        FrameLayout right_arrow = (FrameLayout) mView.findViewById(R.id.right_arrow);
        RelativeLayout messageContainer = (RelativeLayout) mView.findViewById(R.id.message_container);

        if (isSender) {
            left_arrow.setVisibility(View.GONE);
            right_arrow.setVisibility(View.VISIBLE);
            messageContainer.setGravity(Gravity.RIGHT);
        } else {
            left_arrow.setVisibility(View.VISIBLE);
            right_arrow.setVisibility(View.GONE);
            messageContainer.setGravity(Gravity.LEFT);
        }
    }

    public void setName(String name) {
        TextView field = (TextView) mView.findViewById(R.id.name_text);
        field.setText(name);
    }

    public void setText(String text) {
        TextView field = (TextView) mView.findViewById(R.id.message_text);
        field.setText(text);

        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView clickedText = (TextView) mView.findViewById(R.id.message_text);
                try {
                    TranslatorSingleton translator = TranslatorSingleton.getInstance();
                    translator.translatedText(clickedText, translator.getNativeLanguage());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
    }
}
