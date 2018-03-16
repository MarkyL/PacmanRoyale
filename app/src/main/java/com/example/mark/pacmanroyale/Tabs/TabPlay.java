package com.example.mark.pacmanroyale.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.example.mark.pacmanroyale.Activities.PlayActivity;
import com.example.mark.pacmanroyale.Enums.GameMode;
import com.example.mark.pacmanroyale.R;
import com.example.mark.pacmanroyale.Utilities.UserInformationUtils;
import com.example.mark.pacmanroyale.Utilities.WaitingRoomUtils;

/**
 * Created by Mark on 17/02/2018.
 */

public class TabPlay extends Fragment implements View.OnClickListener {

    private static final String TAG = "TabPlay";
    private ImageView imageView;
    private static final String GAME_MODE = "GAME_MODE";

    private ISearchMatchInterface iViewInterface;

    private FrameLayout loadingLayout;
    private LinearLayout buttonsLayout;
    private Button mCancelMatchMakingBtn;

    private GameMode mGameMode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_play, container, false);

        rootView.findViewById(R.id.playBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsPacmanBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsGhostBtn).setOnClickListener(this);

        loadingLayout = rootView.findViewById(R.id.loading_layout);
        buttonsLayout = rootView.findViewById(R.id.buttons_layout);

        mCancelMatchMakingBtn = rootView.findViewById(R.id.cancelMatchMakingBtn);
        mCancelMatchMakingBtn.setOnClickListener(this);

        iViewInterface = (ISearchMatchInterface) getContext();
        return rootView;
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case (R.id.playBtn): {
                mGameMode = GameMode.VS_PC;
                UserInformationUtils.setUserPresencePlaying(getContext());
                Intent playIntent = new Intent(getContext(), PlayActivity.class);
                playIntent.putExtra(GAME_MODE, GameMode.VS_PC);
                startActivity(playIntent);
            } break;
            case (R.id.playAsPacmanBtn): {
                loadWaitingRoomLoader();
                mGameMode = GameMode.PACMAN;
                WaitingRoomUtils.getWaitingRoom().beginMatchMaking(mGameMode);
            } break;
            case (R.id.playAsGhostBtn): {
                loadWaitingRoomLoader();
                mGameMode = GameMode.GHOST;
                WaitingRoomUtils.getWaitingRoom().beginMatchMaking(mGameMode);
            } break;
            case (R.id.cancelMatchMakingBtn): {
                if (mGameMode == GameMode.PACMAN) {
                    WaitingRoomUtils.getWaitingRoom().cancelPacmanGame();
                } else if (mGameMode == GameMode.GHOST) {
                    WaitingRoomUtils.getWaitingRoom().cancelGhostGame();
                }
                toggleLayoutVisibilities(false); // I saw loader , need to toggle now.
                iViewInterface.toggleSearchingForMatch(false);
//                iViewInterface.toggleTabsVisibility(true); // return the tabs of the main activity.
//                iViewInterface.toggleBackPressAvailability(true);
            } break;
        }
    }

    private void loadWaitingRoomLoader() {
        toggleLayoutVisibilities(true);
        iViewInterface.toggleSearchingForMatch(true);
    }

    public void toggleLayoutVisibilities(boolean isLoadingLayoutVisible) {
        if (isLoadingLayoutVisible) {
            buttonsLayout.setVisibility(View.INVISIBLE);
            loadingLayout.setVisibility(View.VISIBLE);
        } else {
            buttonsLayout.setVisibility(View.VISIBLE);
            loadingLayout.setVisibility(View.INVISIBLE);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface ISearchMatchInterface {
        void toggleSearchingForMatch(boolean isSearching);
    }

}

