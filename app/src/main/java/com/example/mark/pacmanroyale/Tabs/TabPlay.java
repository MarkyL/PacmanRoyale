package com.example.mark.pacmanroyale.Tabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
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

    private static final String GAME_MODE = "GAME_MODE";

    private ISearchMatchInterface iViewInterface;

    private FrameLayout mLoadingLayout;
    private LinearLayout mButtonsLayout;
    private boolean mFirstLoad = true;
    private GameMode mGameMode;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab_play, container, false);

        rootView.findViewById(R.id.playBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsPacmanBtn).setOnClickListener(this);
        rootView.findViewById(R.id.playAsGhostBtn).setOnClickListener(this);

        mLoadingLayout = rootView.findViewById(R.id.loading_layout);
        mButtonsLayout = rootView.findViewById(R.id.buttons_layout);

        Button mCancelMatchMakingBtn = rootView.findViewById(R.id.cancelMatchMakingBtn);
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
            }
            break;
            case (R.id.playAsPacmanBtn): {
                loadWaitingRoomLoader();
                mGameMode = GameMode.PACMAN;
                WaitingRoomUtils.getWaitingRoom().beginMatchMaking(mGameMode);
            }
            break;
            case (R.id.playAsGhostBtn): {
                loadWaitingRoomLoader();
                mGameMode = GameMode.GHOST;
                WaitingRoomUtils.getWaitingRoom().beginMatchMaking(mGameMode);
            }
            break;
            case (R.id.cancelMatchMakingBtn): {
                if (mGameMode == GameMode.PACMAN) {
                    WaitingRoomUtils.getWaitingRoom().cancelPacmanGame();
                } else if (mGameMode == GameMode.GHOST) {
                    WaitingRoomUtils.getWaitingRoom().cancelGhostGame();
                }
                toggleLayoutVisibilities(false); // I saw loader , need to toggle now.
                iViewInterface.toggleSearchingForMatch(false);
            }
            break;
        }
    }

    private void loadWaitingRoomLoader() {
        toggleLayoutVisibilities(true);
        iViewInterface.toggleSearchingForMatch(true);
    }

    public void toggleLayoutVisibilities(boolean isLoadingLayoutVisible) {
        if (isLoadingLayoutVisible) {
            mButtonsLayout.setVisibility(View.INVISIBLE);
            mLoadingLayout.setVisibility(View.VISIBLE);
        } else {
            mButtonsLayout.setVisibility(View.VISIBLE);
            mLoadingLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!mFirstLoad) {
            toggleLayoutVisibilities(false);
            iViewInterface.toggleSearchingForMatch(false);
        }
        mFirstLoad = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public interface ISearchMatchInterface {
        void toggleSearchingForMatch(boolean isSearching);
    }
}

