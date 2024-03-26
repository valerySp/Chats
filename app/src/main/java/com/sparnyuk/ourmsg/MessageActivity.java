package com.sparnyuk.ourmsg;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.sparnyuk.ourmsg.Adapter.MessageAdapter;
import com.sparnyuk.ourmsg.Fragments.APIService;
import com.sparnyuk.ourmsg.Model.Chat;
import com.sparnyuk.ourmsg.Model.User;
import com.sparnyuk.ourmsg.Notification.Client;
import com.sparnyuk.ourmsg.Notification.Data;
import com.sparnyuk.ourmsg.Notification.MyResponse;
import com.sparnyuk.ourmsg.Notification.Sender;
import com.sparnyuk.ourmsg.Notification.Token;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    CircleImageView profile_image;
    TextView username,time;
    FirebaseUser fuser;
    DatabaseReference reference;
    Intent intent;
    MessageAdapter messageAdapter;
    List<Chat> mChat;
    RecyclerView recyclerView;
    ImageView send;
    EditText text_send;
    APIService apiService;
    String userid;
    boolean notify =false;
    ValueEventListener seenListener;

    //import image start
    ImageButton sendPic;
    private StorageTask uploadTask;

    Uri fileUri=null;
    String myUrl;
    ProgressDialog loadbar;
//import image end




    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Toolbar toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        apiService= Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

        recyclerView=findViewById(R.id.recycler_view);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);


        profile_image=findViewById(R.id.profile_image);
        username=findViewById(R.id.userName);

        send=findViewById(R.id.btn_send);
        text_send=findViewById(R.id.text_send);
        time=findViewById(R.id.receiverTime);


        intent =getIntent();
        userid=intent.getStringExtra("userid");

        //import image start
        sendPic=findViewById(R.id.sendPic);
        loadbar=new ProgressDialog(this);
       // sendImag=findViewById(R.id.imageFileSend);
        //import image end

        /*
        Этот код не работает - нужно скорректировать ChatFragment!
        добавлять эти строчки в метод sendMessage и readMessage*/

        /*final String senderID=FirebaseAuth.getInstance().getUid();//sm1
        String recieveId=getIntent().getStringExtra("userid");//sm1
        final String senderRoom=senderID+recieveId;//sm1
        final String receiverRoom=recieveId+senderID;//sm1*/

        //import image start

        sendPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                notify=true;
                Intent intent=new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent,438);
            }
        });


        //import image end


        send.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.N)

            @Override
            public void onClick(View view) {
                notify=true;
                String msg=text_send.getText().toString();

                if (!msg.equals("")){
                    sendMessage(fuser.getUid(),userid,msg);
                    FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid()).child("time").setValue(new Date().getTime());

                } else {
                    Toast.makeText(MessageActivity.this, "Вы не можете отправить пустое сообщение.", Toast.LENGTH_SHORT).show();
                }

                text_send.setText("");
            }
        });

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        reference= FirebaseDatabase.getInstance().getReference("Users").child(userid);



        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                username.setText(user.getUsername());
                if(user.getImageURL().equals("default")){
                    profile_image.setImageResource(R.mipmap.ic_launcher_round);

                } else{
                    Glide.with(getApplicationContext()).load(user.getImageURL()).into(profile_image);
                }
                readMessages(fuser.getUid(),userid,user.getImageURL());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        seenMessage(userid);

    }


    private void seenMessage(String userid){
        reference=FirebaseDatabase.getInstance().getReference("Chats");
        seenListener=reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(fuser.getUid())&&chat.getSender().equals(userid)){
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("isseen",true);
                        snapshot1.getRef().updateChildren(hashMap);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void sendMessage (String sender,String receiver,String message){
        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
        HashMap<String,Object> hashMap=new HashMap<>();
        hashMap.put("sender",sender);
        hashMap.put("receiver",receiver);
        hashMap.put("message",message);
        hashMap.put("timestamp",new Date().getTime());
        hashMap.put("isseen",false);
        hashMap.put("type", "text");


        /*final String senderID=FirebaseAuth.getInstance().getUid();//sm1
        String recieveId=getIntent().getStringExtra("userId");//sm1
        final String senderRoom=senderID+recieveId;//sm1
        final String receiverRoom=recieveId+senderID;//sm1*/

        reference.child("Chats").push().setValue(hashMap);
        //Chatlist - part 16

        //Chatlist - part 16 - end

        //create Not
        final String msg=message;
        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user=snapshot.getValue(User.class);
                if(notify){
                sendNotification(receiver,user.getUsername(),msg);
                }
            notify=false;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    private void sendNotification(String receiver,final String username,final String message){
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query query=tokens.orderByKey().equalTo(receiver);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                Token token=snapshot1.getValue(Token.class);
                User user=snapshot.getValue(User.class);
                Data data=new Data(fuser.getUid(),R.mipmap.ic_launcher,username+": "+message,"New Message",userid);
                Sender sender =new Sender(data,token.getToken());

                apiService.sendNotification(sender)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                                if (response.code()==200){
                                    //Log.d("MyLog","size body"+response.body());
                                    if (response.body().success!=1){
                                        Toast.makeText(MessageActivity.this, "НЕ РАБОТАЕТ!!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                            }
                        });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
    //create NOt end

    private void readMessages(String myid,String userid,String imageurl){

        mChat=new ArrayList<>();
        /*final String senderID=FirebaseAuth.getInstance().getUid();//sm1
        String recieveId=getIntent().getStringExtra("userId");//sm1
        final String senderRoom=senderID+recieveId;//sm1
        final String receiverRoom=recieveId+senderID;//sm1*/

        reference=FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mChat.clear();
                for (DataSnapshot snapshot1:snapshot.getChildren()){
                    Chat chat=snapshot1.getValue(Chat.class);
                    if (chat.getReceiver().equals(myid)&&chat.getSender().equals(userid)||
                    chat.getReceiver().equals(userid)&&chat.getSender().equals(myid)){
                        mChat.add(chat);
                    }
                    messageAdapter=new MessageAdapter(MessageActivity.this,mChat,imageurl);
                    recyclerView.setAdapter(messageAdapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    protected void onPause() {
        super.onPause();
        reference.removeEventListener(seenListener);
        currentUser("none");
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser(userid);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode==438 && data.getData()!=null && resultCode==RESULT_OK&&data!=null){

            loadbar.setTitle("Отправка изображения");
            loadbar.setMessage("Пожалуйста подождите..");
            loadbar.setCanceledOnTouchOutside(false);
            loadbar.show();

            fileUri=data.getData();
            StorageReference storageReference= FirebaseStorage.getInstance().getReference().child("ImageFiles");


            StorageReference filePath =storageReference.child(String.valueOf(System.currentTimeMillis()));

            uploadTask=filePath.putFile(fileUri);
            uploadTask.continueWithTask(new Continuation() {
                @Override
                public Object then(@NonNull Task task) throws Exception {
                    if (!task.isSuccessful()){
                        throw task.getException();
                    }
                    return filePath.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task <Uri> task) {
                    if (task.isSuccessful()){
                     Uri downloadUrl=task.getResult();
                     myUrl=downloadUrl.toString();

                        DatabaseReference reference=FirebaseDatabase.getInstance().getReference();
                        HashMap<String,Object> hashMap=new HashMap<>();
                        hashMap.put("sender", fuser.getUid());
                        hashMap.put("receiver",userid);
                        hashMap.put("message",myUrl);
                        hashMap.put("timestamp",new Date().getTime());
                        hashMap.put("isseen",false);
                        hashMap.put("type", "image");

                        //Chat chat=new Chat(sender,receiver,message,new Date().getTime());
                        //chat.setTimestamp(new Date().getTime());
        /*final String senderID=FirebaseAuth.getInstance().getUid();//sm1
        String recieveId=getIntent().getStringExtra("userid");//sm1
        final String senderRoom=senderID+recieveId;//sm1
        final String receiverRoom=recieveId+senderID;//sm1*/

                        reference.child("Chats").push().setValue(hashMap);
                        loadbar.dismiss();

                        reference=FirebaseDatabase.getInstance().getReference("Users").child(fuser.getUid());
                        reference.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User user=snapshot.getValue(User.class);
                                if(notify){
                                    sendNotification(userid,user.getUsername(),"Новое изображение");
                                }
                                notify=false;
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                    loadbar.dismiss();
                }
            });
        }
    }

    private void currentUser(String userid){
        SharedPreferences.Editor editor=getSharedPreferences("PREFS",MODE_PRIVATE).edit();
        editor.putString("currentuser", userid);
        editor.apply();
    }
}

   /* @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case CAMERA_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean cameraAccepted=grantResults[0]== PackageManager.PERMISSION_GRANTED;
                    boolean storageAccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if (cameraAccepted && storageAccepted){
                        pickFromCamera();
                    }
                    else {
                        Toast.makeText(this, "Camera & Storage both permis", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;
            case STORAGE_REQUEST_CODE:{
                if (grantResults.length>0){
                    boolean storageAccepted=grantResults[1]== PackageManager.PERMISSION_GRANTED;
                    if (storageAccepted){
                        pickFromGallery();
                    }
                    else {
                        Toast.makeText(this, "Storage permis", Toast.LENGTH_SHORT).show();
                    }
                }
                else {

                }
            }
            break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        
        if (resultCode==RESULT_OK){
            if (requestCode==IMAGE_PICK_GALLERY_CODE)
            {
                image_uri=data.getData();
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

            } else if (requestCode==IMAGE_PICK_CAMERA_CODE) {
                try {
                    sendImageMessage(image_uri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void sendImageMessage(Uri image_uri) throws IOException {
        //notify=true;
        ProgressDialog progressDialog=new ProgressDialog(this);
        progressDialog.setMessage("Sending image...");
        progressDialog.show();

        String timeStamp=""+System.currentTimeMillis();

        String fileNameAndPath="ChatImages/"+"post_"+timeStamp;

        Bitmap bitmap= MediaStore.Images.Media.getBitmap(this.getContentResolver(),image_uri);
        ByteArrayOutputStream baos=new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,baos);
        byte[] data = baos.toByteArray();
        StorageReference ref= FirebaseStorage.getInstance().getReference().child(fileNameAndPath);
        ref.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();

                        Task<Uri> uriTask=taskSnapshot.getStorage().getDownloadUrl();
                        while (!uriTask.isSuccessful());
                        String downloadUri=uriTask.getResult().toString();
                        if(uriTask.isSuccessful()){
                            DatabaseReference databaseReference=FirebaseDatabase.getInstance().getReference();
                            HashMap<String,Object> hashMap=new HashMap<>();
                            hashMap.put("type","image");
                            databaseReference.child("Chats").push().updateChildren(hashMap);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });

    }

    private void showImagePickDialog() {
        String[] options={"Camera", "Gallery"};

        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("Choose Image");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.create().show();
    }*/
