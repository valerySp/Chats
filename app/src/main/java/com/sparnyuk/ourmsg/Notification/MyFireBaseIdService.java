package com.sparnyuk.ourmsg.Notification;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFireBaseIdService extends FirebaseMessagingService {


    public void onNewToken(String s) {
        super.onNewToken(s);

       FirebaseUser firebaseUser= FirebaseAuth.getInstance().getCurrentUser();

        if (firebaseUser!=null) {
            //final String[] token = {""};
            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if (task.isComplete()) {
                        updateToken(task.getResult());
                        Log.d("MyLog2","task"+task.getResult());
                        //token[0] = task.getResult();
                    }
                }
            });

        }
    }

    private void updateToken(String s) {

            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Tokens");
            Token token = new Token(s);
            reference.child(firebaseUser.getUid()).setValue(token);
    }
}
