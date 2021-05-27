package com.example.ptsupport;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;


public class SettingsFragment extends Fragment {

    public static boolean is_touch;
    private SharedPreferences sp;
    SharedPreferences.Editor ed;
    LinearLayout walkway;

    Button mode_select;
    Button walk_check;
    Button button_x;


    public SettingsFragment() {

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }


    public void onModeClick() {

        sp = getActivity().getSharedPreferences("SharedPrefFile", Context.MODE_PRIVATE);
        ed = sp.edit();

        if(is_touch == true)
        {
            ed.putBoolean("mode", true);
            ed.commit();
            mode_select.setText("EASY");
//            Toast.makeText(getActivity(), "음성 코칭이 진행되는 EASY 모드입니다", Toast.LENGTH_SHORT).show();
        }
        else
        {
            ed.putBoolean("mode", false);
            ed.commit();
            mode_select.setText("DIET");
//            Toast.makeText(getActivity(), "1분 간격으로 알림음이 들리는 DIET 모드입니다", Toast.LENGTH_SHORT).show();
        }
    }


    //앱 다시 실행할 때
    @Override
    public void onResume(){
        super.onResume();

        SharedPreferences sp =
                getActivity().getSharedPreferences("SharedPrefFile", Context.MODE_PRIVATE);

        is_touch = sp.getBoolean("mode", true);
        onModeClick();
    }


    //앱 나갔을 떄
    @Override
    public void onPause(){
        super.onPause();

        SharedPreferences sp = getActivity().getSharedPreferences("SharedPrefFile", Context.MODE_PRIVATE);
        is_touch = sp.getBoolean("mode", true);
    }


    @Override
    public void onStart(){
        super.onStart();

        walkway = (LinearLayout) getActivity().findViewById(R.id.include_layout);
        button_x = (Button) getActivity().findViewById(R.id.btn_x);
        walk_check = (Button) getActivity().findViewById(R.id.button_check);

        mode_select = (Button) getView().findViewById(R.id.button_mode);

        //모드 선택
        mode_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                is_touch = !is_touch;
                onModeClick();
            }

        });
         //걷는 방법
            walk_check.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                walkway.setVisibility(View.VISIBLE); //스크롤뷰
                button_x.setVisibility(View.VISIBLE); //X버튼
                mode_select.setVisibility(View.INVISIBLE);
                walk_check.setVisibility(View.INVISIBLE);

                button_x.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        walkway.setVisibility(View.INVISIBLE);
                        button_x.setVisibility(View.INVISIBLE);
                        mode_select.setVisibility(View.VISIBLE);
                        walk_check.setVisibility(View.VISIBLE);
                    }
                });
            }
        });

    }


}