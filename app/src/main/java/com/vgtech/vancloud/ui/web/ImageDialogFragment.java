package com.vgtech.vancloud.ui.web;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.vgtech.vancloud.R;

import java.io.File;

/**
 * Data:  2019/6/21
 * Auther: xcd
 * Description:
 */
public class ImageDialogFragment extends DialogFragment implements View.OnClickListener{

    private ImageView imageView;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.dialog_fragment_image, container);
            imageView = view.findViewById(R.id.iv);
            imageView.setOnClickListener(this);
            // 接收关联Activity传来的数据 -----
            Bundle bundle = getArguments();
            if (bundle != null) {
                String imagePath = bundle.getString("url");
                File file = new File(imagePath);
                if(file.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                    imageView.setImageBitmap(bitmap);
                }

            }
            return view;
        }
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.iv:
                dismiss();
                break;
        }
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        BitmapDrawable b= imageView.getDrawable() instanceof BitmapDrawable ? ((BitmapDrawable) imageView.getDrawable()) : null;
        if (b != null) {
            b.getBitmap().recycle();
            imageView.setImageDrawable(null);
        }
    }
}
