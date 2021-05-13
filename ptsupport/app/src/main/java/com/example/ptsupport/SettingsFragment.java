package com.example.ptsupport;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class SettingsFragment extends Fragment {

    boolean is_touch;

    LinearLayout walkway;
    private Button mode_select;
    private Button walk_check;
    private Button button_x;

//    //프래그먼트 매니저 선언, 프래그먼트 트랜잭션 시작
//    FragmentManager fragmentManager = getFragmentManager();
//    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//    SettingsFragment setting = new SettingsFragment();

    public SettingsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onStart(){
        super.onStart();

        walkway = (LinearLayout) getActivity().findViewById(R.id.include_layout);
        Button button_x = (Button) getActivity().findViewById(R.id.btn_x);
        Button walk_check = (Button) getActivity().findViewById(R.id.button_check);
        final Button mode_select = (Button) getView().findViewById(R.id.button_mode);

        //모드 선택
         mode_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(is_touch == true){
                    mode_select.setText("EASY");
                    Toast.makeText(getActivity(), "EASY 모드로 바뀌었습니다", Toast.LENGTH_SHORT).show();
                    is_touch = false;

                    //EASY 모드는 아직 구현하지 않았습니다
                }
                else {
                    mode_select.setText("DIET");
                    Toast.makeText(getActivity(), "DIET 모드로 바뀌었습니다", Toast.LENGTH_SHORT).show();
                    is_touch = true;

                    //DIET 모드는 아직 구현하지 않았습니다
                }
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