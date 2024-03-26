package com.sparnyuk.ourmsg.Adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.sparnyuk.ourmsg.Model.Chat;
import com.sparnyuk.ourmsg.R;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter {

    public static final int MSG_TYPE_LEFT=0;
    public static final int MSG_TYPE_RIGHT=1;
    private List<Chat> mChat;
    private Context context;
    private String imageurl;
    FirebaseUser fuser;
    Bitmap bitmap;
    BitmapDrawable bitmapDrawable;


    public MessageAdapter(Context context, List<Chat> mChat, String imageurl) {

        this.context = context;
        this.mChat = mChat;
        this.imageurl=imageurl;
    }


    @NonNull
    @Override
    public  RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType==MSG_TYPE_RIGHT){
            View view=LayoutInflater.from(context).inflate(R.layout.sampe_sender,parent,false);

            return new SenderViewHolder(view);
        }
        else {
            View view = LayoutInflater.from(context).inflate(R.layout.sampe_reciver, parent,false);
            return new RecieverViewHolder(view);
        }


    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Chat messageModel=mChat.get(position);
        long msgTimeStamp= mChat.get(position).getTimestamp();
        String type=mChat.get(position).getType();

        //delete msg start
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(context)
                        .setTitle("Delete")
                        .setMessage("Вы хотите удалить сообщение?")
                        .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Меняем функицию делете!!!

                                DatabaseReference reference= FirebaseDatabase.getInstance().getReference("Chats");

                                Query query=reference.orderByChild("timestamp").equalTo(msgTimeStamp);
                                query.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        for (DataSnapshot dataSnapshot:snapshot.getChildren()){
                                            if (dataSnapshot.child("sender").getValue().equals(FirebaseAuth.getInstance().getUid())){
                                            //dataSnapshot.getRef().removeValue();
                                            HashMap<String,Object> hashMap=new HashMap<>();
                                            hashMap.put("message","This message was deleted");
                                            hashMap.put("type","text");
                                            dataSnapshot.getRef().updateChildren(hashMap);
                                            }
                                            else  Toast.makeText(context, "Вы не можете удалить это сообщение", Toast.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });


                            }
                        }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).show();
            }


        });
        //delete end


        //download pict start

        if(type.equals("image")) {

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    new AlertDialog.Builder(context)
                            .setTitle("Сохранить")
                            .setMessage("Вы хотите сохранить?")
                            .setPositiveButton("Да", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (holder.getItemViewType() == MSG_TYPE_RIGHT) {

                                        bitmapDrawable = (BitmapDrawable) ((SenderViewHolder) holder).imageSend.getDrawable();
                                        bitmap = bitmapDrawable.getBitmap();

                                        FileOutputStream fos = null;
                                        File sdCard = Environment.getExternalStorageDirectory();
                                        File Directory = new File(sdCard.getAbsolutePath() + "/Download");
                                        Directory.mkdir();

                                        String filename = String.format("%d.jpg", System.currentTimeMillis());
                                        File outFile = new File(Directory, filename);
                                        Toast.makeText(context, "Загружено", Toast.LENGTH_SHORT).show();

                                        try {
                                            fos = new FileOutputStream(outFile);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                            fos.flush();
                                            fos.close();

                                            Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            intent1.setData(Uri.fromFile(outFile));


                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }

                                    } else {
                                        bitmapDrawable = (BitmapDrawable) ((RecieverViewHolder) holder).imageRec.getDrawable();
                                        bitmap = bitmapDrawable.getBitmap();
                                        FileOutputStream fos = null;
                                        File sdCard = Environment.getExternalStorageDirectory();
                                        File Directory = new File(sdCard.getAbsolutePath() + "/Download");
                                        Directory.mkdir();

                                        String filename = String.format("%d.jpg", System.currentTimeMillis());
                                        File outFile = new File(Directory, filename);
                                        Toast.makeText(context, "Загружено", Toast.LENGTH_SHORT).show();

                                        try {
                                            fos = new FileOutputStream(outFile);
                                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                                            fos.flush();
                                            fos.close();

                                            Intent intent1 = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                            intent1.setData(Uri.fromFile(outFile));


                                        } catch (FileNotFoundException e) {
                                            e.printStackTrace();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }).setNegativeButton("Нет", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            }).show();


                    return false;
                }
            });
        }

        //down pict end



        if(holder.getClass()==SenderViewHolder.class){

                if (messageModel.isIsseen()){
                    ((SenderViewHolder) holder).txt_isseen.setText("Просмотрено");

                }
                else {
                    ((SenderViewHolder) holder).txt_isseen.setText("Доставлено");

                }

                if (type.equals("image")){
                    ((SenderViewHolder) holder).imageSend.setVisibility(View.VISIBLE);
                    Date date =new Date(messageModel.getTimestamp());
                    ((SenderViewHolder) holder).senderMsg.setVisibility(View.GONE);

                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm\nyyyy-MM-dd ");
                    String strDate=simpleDateFormat.format(date);
                    ((SenderViewHolder) holder).senderTime.setText(strDate.toString());
                    Glide.with(context).load(imageurl).into(((SenderViewHolder) holder).profile_image);
                    Picasso.get().load(messageModel.getMessage()).into(((SenderViewHolder) holder).imageSend);
                } else {
                    ((SenderViewHolder) holder).imageSend.setVisibility(View.GONE);
                    ((SenderViewHolder) holder).senderMsg.setVisibility(View.VISIBLE);
                    ((SenderViewHolder) holder).senderMsg.setText(messageModel.getMessage());
                    Date date =new Date(messageModel.getTimestamp());
                    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm\nyyyy-MM-dd ");
                    String strDate=simpleDateFormat.format(date);
                    ((SenderViewHolder) holder).senderTime.setText(strDate.toString());
                    Glide.with(context).load(imageurl).into(((SenderViewHolder) holder).profile_image);
                }

        }
        else {
            if (type.equals("image")){
                ((RecieverViewHolder) holder).imageRec.setVisibility(View.VISIBLE);
                ((RecieverViewHolder) holder).txt_isseen.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).recieverMsg.setVisibility(View.GONE);

                Date date =new Date(messageModel.getTimestamp());
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm\nyyyy-MM-dd ");
                String strDate=simpleDateFormat.format(date);
                ((RecieverViewHolder) holder).recieverTime.setText(strDate.toString());
                Glide.with(context).load(imageurl).into(((RecieverViewHolder) holder).profile_image);
                Picasso.get().load(messageModel.getMessage()).into(((RecieverViewHolder) holder).imageRec);
            } else {
                ((RecieverViewHolder) holder).imageRec.setVisibility(View.GONE);
                ((RecieverViewHolder) holder).recieverMsg.setVisibility(View.VISIBLE);
                ((RecieverViewHolder) holder).recieverMsg.setText(messageModel.getMessage());
                ((RecieverViewHolder) holder).txt_isseen.setVisibility(View.GONE);

                Date date =new Date(messageModel.getTimestamp());
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("HH:mm\nyyyy-MM-dd ");
                String strDate=simpleDateFormat.format(date);
                ((RecieverViewHolder) holder).recieverTime.setText(strDate.toString());
                Glide.with(context).load(imageurl).into(((RecieverViewHolder) holder).profile_image);
            }
        }


    }

    @Override
    public int getItemCount() {
        return mChat.size();
    }

    public class RecieverViewHolder extends RecyclerView.ViewHolder{
        TextView recieverMsg, recieverTime;
        public ImageView profile_image;
        TextView txt_isseen;
        ImageView imageRec;

        public RecieverViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            recieverMsg=itemView.findViewById(R.id.receiverText);
            recieverTime=itemView.findViewById(R.id.receiverTime);
            txt_isseen=itemView.findViewById(R.id.txt_seen_rec);
            imageRec=itemView.findViewById(R.id.imageFileRec);
        }
    }
    public class SenderViewHolder extends RecyclerView.ViewHolder{
        TextView senderMsg, senderTime;
        public ImageView profile_image;
        TextView txt_isseen;
        ImageView imageSend;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            profile_image = itemView.findViewById(R.id.profile_image);
            senderMsg=itemView.findViewById(R.id.senderText);
            senderTime=itemView.findViewById(R.id.senderTime);
            txt_isseen=itemView.findViewById(R.id.txt_seen_send);
            imageSend=itemView.findViewById(R.id.imageFileSend);

        }
    }

    //end

    @Override
    public int getItemViewType(int position) {

        fuser= FirebaseAuth.getInstance().getCurrentUser();
        if(mChat.get(position).getSender().equals(fuser.getUid())){
            return MSG_TYPE_RIGHT;
        }
        else return MSG_TYPE_LEFT;
    }
}
