package com.example.mark.pacmanroyale.Fragments;

import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.GridLayout;

import com.example.mark.pacmanroyale.MiscDesign.GridButton;
import com.example.mark.pacmanroyale.R;
/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link PlayGround.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link PlayGround#newInstance} factory method to
 * create an instance of this fragment.
 */
public class PlayGround extends Fragment implements View.OnClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static final String TAG = PlayGround.class.getSimpleName();
    private static final int MAP_WIDTH_BUTTONS = 28;
    private static final int MAP_HEIGHT_BUTTONS = 36;

    private GridLayout gridLayout;
    private int gridLayoutHeight;

    private LayoutInflater inflater;
    private ViewGroup container;

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    public PlayGround() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment PlayGround.
     */
    // TODO: Rename and change types and number of parameters
    public static PlayGround newInstance(String param1, String param2) {
        PlayGround fragment = new PlayGround();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        this.inflater=inflater;
        this.container=container;

        View mapView = inflateMap(MAP_WIDTH_BUTTONS, MAP_HEIGHT_BUTTONS);
        return mapView;
    }

    public View inflateMap(int widthButtons, int heightButtons) {
        Log.d(TAG, "inflateMap");
        View mapView = inflater.inflate(R.layout.fragment_play_ground, container, false);
        initGridLayout(widthButtons, heightButtons, mapView);
        return mapView;
    }

    public void initGridLayout(int widthButtons, int heightButtons, View mapView) {
        gridLayout = mapView.findViewById(R.id.playGround);

        Log.d(TAG, "initGridLayout: of width= "+widthButtons+", height= "+heightButtons);
        gridLayout.setColumnCount(widthButtons);
        gridLayout.setRowCount(heightButtons);
        initGridLayoutButtons();
        gridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                gridLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                gridLayoutHeight = gridLayout.getHeight(); //width is ready
                int cellSize = gridLayoutHeight / gridLayout.getColumnCount();
                //cellSize -= 1;
                for (int i = 0; i < gridLayout.getChildCount(); i++) {
                    GridButton btn = (GridButton) gridLayout.getChildAt(i);
                    btn.setPositionX(i % gridLayout.getColumnCount());
                    btn.setPositionY(i / gridLayout.getColumnCount());
                    btn.setBackgroundResource(R.drawable.cell_border);
                    btn.getLayoutParams().height = cellSize;
                    btn.getLayoutParams().width = cellSize;
                }
                gridLayout.invalidate();
                gridLayout.requestLayout();
            }
        });
    }
    private void initGridLayoutButtons() {
        Log.d(TAG, "initGridLayoutButtons: starting");
        int squaresCount = gridLayout.getColumnCount() * gridLayout.getRowCount();
        for (int i = 0; i < squaresCount; i++) {
            GridButton gridButton = new GridButton(getContext());
            gridButton.setOnClickListener(this);
            gridLayout.addView(gridButton);
        }
        Log.d(TAG, "initGridLayoutButtons: finishing");
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

//    @Override
//    public void onAttach(Context context) {
//        super.onAttach(context);
//        if (context instanceof OnFragmentInteractionListener) {
//            mListener = (OnFragmentInteractionListener) context;
//        } else {
//            throw new RuntimeException(context.toString()
//                    + " must implement OnFragmentInteractionListener");
//        }
//    }
//
//    @Override
//    public void onDetach() {
//        super.onDetach();
//        mListener = null;
//    }

    @Override
    public void onClick(View view) {

    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
