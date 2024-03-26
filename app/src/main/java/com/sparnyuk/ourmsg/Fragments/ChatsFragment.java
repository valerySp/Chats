package com.sparnyuk.ourmsg.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.sparnyuk.ourmsg.Adapter.UserAdapter;
import com.sparnyuk.ourmsg.Model.Chat;
import com.sparnyuk.ourmsg.Model.User;
import com.sparnyuk.ourmsg.Notification.Token;
import com.sparnyuk.ourmsg.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<User> mUsers;

    FirebaseUser fuser;
    DatabaseReference reference;

    private List<String> userList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView=view.findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        userList=new ArrayList<>();
        reference=FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    //if (!users.getUserId().equals(FirebaseAuth.getInstance().getUid()))
                    if(chat.getSender().equals(fuser.getUid())){
                        userList.add(chat.getReceiver());
                    }
                    if (chat.getReceiver().equals(fuser.getUid())){
                        userList.add(chat.getSender());
                    }
                }


                chatList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                if(task.isComplete()){
                    updateToken(task.getResult());
                    }
                }
        });

        return view;
    }

    public void updateToken(String token){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference("Tokens");
        Token token1=new Token(token);
        reference.child(fuser.getUid()).setValue(token1);
    }

    private void chatList(){
        mUsers=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Users");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    User user=snapshot1.getValue(User.class);

                    for (String id:userList){
                        if (user.getId().equals(id)) {
                            if (userList.size() != 0) {
                                boolean exists = false;
                                // If not exists then add
                                for (User user1 : mUsers) {
                                    if (user.getId().equals(user1.getId())) {
                                        exists = true;
                                    }
                                }
                                if (!exists) {
                                    mUsers.add(user);
                                }
                            } else {
                                mUsers.add(user);
                            }
                        }
                    }
                    Collections.sort(mUsers, new Comparator<User>() {
                        @Override
                        public int compare(User o1, User o2) {
                            return (int) (o2.getTime()-o1.getTime());
                        }
                    });
                }


                userAdapter =new UserAdapter(getContext(),mUsers);

                recyclerView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }



   /*private void chatList() {

    }*
    //
    mUsers=new ArrayList<>();
        reference= FirebaseDatabase.getInstance().getReference("Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mUsers.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    User user=snapshot1.getValue(User.class);
                    user.setId(snapshot1.getKey());
                    if(!user.getId().equals(FirebaseAuth.getInstance().getUid())){
                        mUsers.add(user);
                    }

                }

                userAdapter =new UserAdapter(getContext(),mUsers);


                recyclerView.setAdapter(userAdapter);
                userAdapter.notifyDataSetChanged();

    /
    */
}