package co.edu.udea.comunicaciones2.spacerace;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.google.android.gms.games.GamesCallbackStatusCodes;
import com.google.android.gms.games.GamesClient;
import com.google.android.gms.games.GamesClientStatusCodes;
import com.google.android.gms.games.InvitationsClient;
import com.google.android.gms.games.Player;
import com.google.android.gms.games.PlayersClient;
import com.google.android.gms.games.RealTimeMultiplayerClient;
import com.google.android.gms.games.multiplayer.Invitation;
import com.google.android.gms.games.multiplayer.InvitationCallback;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.Participant;
import com.google.android.gms.games.multiplayer.realtime.OnRealTimeMessageReceivedListener;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.android.gms.games.multiplayer.realtime.Room;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.realtime.RoomStatusUpdateCallback;
import com.google.android.gms.games.multiplayer.realtime.RoomUpdateCallback;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import Models.Shot;
import Models.Spacecraft;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    /*
     * API INTEGRATION SECTION. This section contains the code that integrates
     * the game with the Google Play game services API.
     */

    final static String TAG = "SpaceRace";

    // Request codes for the UIs that we show with startActivityForResult:
    final static int RC_SELECT_PLAYERS = 10000;
    final static int RC_INVITATION_INBOX = 10001;
    final static int RC_WAITING_ROOM = 10002;

    // Request code used to invoke sign in user interactions.
    private static final int RC_SIGN_IN = 9001;

    // Client used to sign in with Google APIs
    private GoogleSignInClient mGoogleSignInClient = null;

    // Client used to interact with the real time multiplayer system.
    private RealTimeMultiplayerClient mRealTimeMultiplayerClient = null;

    // Client used to interact with the Invitation system.
    private InvitationsClient mInvitationsClient = null;

    // Room ID where the currently active game is taking place; null if we're
    // not playing.
    String mRoomId = null;

    // Holds the configuration of the current room.
    RoomConfig mRoomConfig;

    // Are we playing in multiplayer mode?
    boolean mMultiplayer = false;

    // The participants in the currently active game
    ArrayList<Participant> mParticipants = null;

    // My participant ID in the currently active game
    String mMyId = null;

    // If non-null, this is the id of the invitation we received via the
    // invitation listener
    String mIncomingInvitationId = null;

    // Message buffer for sending messages
    byte[] mMsgBuf = new byte[1024];

    //Game
    SensorManager sm;
    Sensor sensor;
    Vibrator vibrator;
    MediaPlayer mp;
    MediaPlayer mpFondo;
    MediaPlayer mpChoque;
    Drawable imgFacebook, imgNaveRival, imagenesNavesInv[], imagenesNaves[];
    Drawable imgNave, imgMeteorito, imgMisil, imgFondo, imgFondo2, imgGameOver, imgMisilRival;
    Drawable imgVida1, imgVida2, imgVida3, imPause, imPlay;
    PaperView papel;
    int naves[], meteoritos[], tamImgMet[][];
    int x_nave, y_nave,x,y, tamNaveX = 100, tamNaveY = 150, x_bala, y_bala, tamBalaX = 40, tamBalaY = 60, tamNavePeqX=60, tamNavePeqY=110;
    int tamPantallaX,tamPantallaY;
    float x_sensor,y_sensor,z_sensor;
    int tiempo, puntos, speed, vel, vidas, vidas_Rival, puntos_Extra, tiempo700=700, tiempo500=500, tiempo300=300, tiempo100=100;;
    int y_fondo,y_fondo2, y_inferior;
    boolean terminado, yaDiagonal, generaMeteoritos = false;
    long monedas;
    ArrayList<Shot> balas;
    ArrayList<Shot> balas_Rival;
    ArrayList<Spacecraft> misNaves, navesRival;
    boolean pauseActivado;
    Spacecraft naveNueva;
    DisplayMetrics metrics;
    int x_nave_Rival=0, y_nave_Rival=0, posIDNaveRival=0;
    Canvas c;// = new Canvas();
    Rect b = new Rect(0,0,0,0), n = b;
    private String var="";
    private boolean ganaste = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Create the client used to sign in.
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_GAMES_SIGN_IN);

        mContentView = findViewById(R.id.activity_challenge);

        //Hide task bar
        hide();

        switchToMainScreen();
        checkPlaceholderIds();

        //Game
        metrics = new DisplayMetrics();
        terminado = false;
        pauseActivado = false;
        yaDiagonal = false;

        papel = new PaperView(this);
        papel.setDrawingCacheEnabled(true);

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        mp = MediaPlayer.create(this,R.raw.laser21);
        mpFondo = MediaPlayer.create(this,R.raw.fondo2);
        mpChoque = MediaPlayer.create(this,R.raw.explo1);
        //mpFondo.start();

        balas = new ArrayList<>();
        balas_Rival = new ArrayList<>();
        navesRival = new ArrayList<>();
        misNaves = new ArrayList<>();

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        tamPantallaX = size.x;
        tamPantallaY = getWallpaper().getIntrinsicHeight();

        x_nave = tamPantallaX/2;
        y_nave = tamPantallaY-tamNaveY;

        x=1;
        y=1;
        x_sensor = 0;
        y_sensor = 0;
        z_sensor = 9;
        speed = 1;
        tiempo = 0;
        puntos = 0;
        puntos_Extra = 0;
        vel=5;
        vidas = 3;
        vidas_Rival = 3;
        y_fondo = -2;
        y_fondo2 = tamPantallaY;

        sm = (SensorManager)getSystemService(SENSOR_SERVICE);
        sensor = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    // Check the sample to ensure all placeholder ids are are updated with real-world values.
    // This is strictly for the purpose of the samples; you don't need this in a production
    // application.
    private void checkPlaceholderIds() {
        StringBuilder problems = new StringBuilder();

        if (getPackageName().startsWith("com.google.")) {
            problems.append("- Package name start with com.google.*\n");
        }

        for (Integer id : new Integer[]{R.string.app_id}) {

            String value = getString(id);

            if (value.startsWith("YOUR_")) {
                // needs replacing
                problems.append("- Placeholders(YOUR_*) in ids.xml need updating\n");
                break;
            }
        }

        if (problems.length() > 0) {
            problems.insert(0, "The following problems were found:\n\n");

            problems.append("\nThese problems may prevent the app from working properly.");
            problems.append("\n\nSee the TODO window in Android Studio for more information");
            (new android.app.AlertDialog.Builder(this)).setMessage(problems.toString())
                    .setNeutralButton(android.R.string.ok, null).create().show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");

        // Since the state of the signed in user can change when the activity is not active
        // it is recommended to try and sign in silently from when the app resumes.
        signInSilently();
        hide();
        //mpFondo.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // unregister our listeners.  They will be re-registered via onResume->signInSilently->onConnected.
        if (mInvitationsClient != null) {
            mInvitationsClient.unregisterInvitationCallback(mInvitationCallback);
        }
        //mpFondo.pause();
        vibrator.cancel();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_sign_in:
                // start the sign-in flow
                Log.d(TAG, "Sign-in button clicked");
                startSignInIntent();
                break;
            case R.id.button_sign_out:
                // user wants to sign out
                // sign out.
                Log.d(TAG, "Sign-out button clicked");
                signOut();
                switchToScreen(R.id.screen_sign_in);
                break;
            case R.id.button_invite_players:
                switchToScreen(R.id.screen_wait);

                // show list of invitable players
                mRealTimeMultiplayerClient.getSelectOpponentsIntent(1, 3).addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_SELECT_PLAYERS);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem selecting opponents."));
                break;
            case R.id.button_see_invitations:
                switchToScreen(R.id.screen_wait);

                // show list of pending invitations
                mInvitationsClient.getInvitationInboxIntent().addOnSuccessListener(
                        new OnSuccessListener<Intent>() {
                            @Override
                            public void onSuccess(Intent intent) {
                                startActivityForResult(intent, RC_INVITATION_INBOX);
                            }
                        }
                ).addOnFailureListener(createFailureListener("There was a problem getting the inbox."));
                break;
            case R.id.button_accept_popup_invitation:
                // user wants to accept the invitation shown on the invitation popup
                // (the one we got through the OnInvitationReceivedListener).
                acceptInviteToRoom(mIncomingInvitationId);
                mIncomingInvitationId = null;
                break;
            case R.id.button_quick_game:
                // user wants to play against a random opponent right now
                startQuickGame();
                break;
            /*case R.id.button_click_me:
                // (gameplay) user clicked the "click me" button
                scoreOnePoint();
                break;*/
        }
    }

    void startQuickGame() {
        // quick-start a game with 1 randomly selected opponent
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 1;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        //switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria)
                .build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
    }

    /**
     * Start a sign in activity.  To properly handle the result, call tryHandleSignInResult from
     * your Activity's onActivityResult function
     */
    public void startSignInIntent() {
        startActivityForResult(mGoogleSignInClient.getSignInIntent(), RC_SIGN_IN);
    }

    /**
     * Try to sign in without displaying dialogs to the user.
     * <p>
     * If the user has already signed in previously, it will not show dialog.
     */
    public void signInSilently() {
        Log.d(TAG, "signInSilently()");

        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this,
                new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInSilently(): success");
                            onConnected(task.getResult());
                        } else {
                            Log.d(TAG, "signInSilently(): failure", task.getException());
                            onDisconnected();
                        }
                    }
                });
    }

    public void signOut() {
        Log.d(TAG, "signOut()");

        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                        if (task.isSuccessful()) {
                            Log.d(TAG, "signOut(): success");
                        } else {
                            handleException(task.getException(), "signOut() failed!");
                        }

                        onDisconnected();
                    }
                });
    }

    /**
     * Since a lot of the operations use tasks, we can use a common handler for whenever one fails.
     *
     * @param exception The exception to evaluate.  Will try to display a more descriptive reason for the exception.
     * @param details   Will display alongside the exception if you wish to provide more details for why the exception
     *                  happened
     */
    private void handleException(Exception exception, String details) {
        int status = 0;

        if (exception instanceof ApiException) {
            ApiException apiException = (ApiException) exception;
            status = apiException.getStatusCode();
        }

        String errorString = null;
        switch (status) {
            case GamesCallbackStatusCodes.OK:
                break;
            case GamesClientStatusCodes.MULTIPLAYER_ERROR_NOT_TRUSTED_TESTER:
                errorString = getString(R.string.status_multiplayer_error_not_trusted_tester);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_ALREADY_REMATCHED:
                errorString = getString(R.string.match_error_already_rematched);
                break;
            case GamesClientStatusCodes.NETWORK_ERROR_OPERATION_FAILED:
                errorString = getString(R.string.network_error_operation_failed);
                break;
            case GamesClientStatusCodes.INTERNAL_ERROR:
                errorString = getString(R.string.internal_error);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_INACTIVE_MATCH:
                errorString = getString(R.string.match_error_inactive_match);
                break;
            case GamesClientStatusCodes.MATCH_ERROR_LOCALLY_MODIFIED:
                errorString = getString(R.string.match_error_locally_modified);
                break;
            default:
                errorString = getString(R.string.unexpected_status, GamesClientStatusCodes.getStatusCodeString(status));
                break;
        }

        if (errorString == null) {
            return;
        }

        String message = getString(R.string.status_exception_error, details, status, exception);

        new android.app.AlertDialog.Builder(MainActivity.this)
                .setTitle("Error")
                .setMessage(message + "\n" + errorString)
                .setNeutralButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (requestCode == RC_SIGN_IN) {

            Task<GoogleSignInAccount> task =
                    GoogleSignIn.getSignedInAccountFromIntent(intent);

            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                onConnected(account);
            } catch (ApiException apiException) {
                String message = apiException.getMessage();
                if (message == null || message.isEmpty()) {
                    message = getString(R.string.signin_other_error);
                }

                onDisconnected();

                new android.app.AlertDialog.Builder(this)
                        .setMessage(message)
                        .setNeutralButton(android.R.string.ok, null)
                        .show();
            }
        } else if (requestCode == RC_SELECT_PLAYERS) {
            // we got the result from the "select players" UI -- ready to create the room
            handleSelectPlayersResult(resultCode, intent);

        } else if (requestCode == RC_INVITATION_INBOX) {
            // we got the result from the "select invitation" UI (invitation inbox). We're
            // ready to accept the selected invitation:
            handleInvitationInboxResult(resultCode, intent);

        } else if (requestCode == RC_WAITING_ROOM) {
            // we got the result from the "waiting room" UI.
            if (resultCode == Activity.RESULT_OK) {
                // ready to start playing
                Log.d(TAG, "Starting game (waiting room returned OK).");
                startGame(true);
            } else if (resultCode == GamesActivityResultCodes.RESULT_LEFT_ROOM) {
                // player indicated that they want to leave the room
                leaveRoom();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Dialog was cancelled (user pressed back key, for instance). In our game,
                // this means leaving the room too. In more elaborate games, this could mean
                // something else (like minimizing the waiting room UI).
                leaveRoom();
            }
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    // Handle the result of the "Select players UI" we launched when the user clicked the
    // "Invite friends" button. We react by creating a room with those players.

    private void handleSelectPlayersResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** select players UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Select players UI succeeded.");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
        Log.d(TAG, "Invitee count: " + invitees.size());

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            Log.d(TAG, "Automatch criteria: " + autoMatchCriteria);
        }

        // create the room
        Log.d(TAG, "Creating room...");
        //switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .addPlayersToInvite(invitees)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .setAutoMatchCriteria(autoMatchCriteria).build();
        mRealTimeMultiplayerClient.create(mRoomConfig);
        Log.d(TAG, "Room created, waiting for it to be ready...");
    }

    // Handle the result of the invitation inbox UI, where the player can pick an invitation
    // to accept. We react by accepting the selected invitation, if any.
    private void handleInvitationInboxResult(int response, Intent data) {
        if (response != Activity.RESULT_OK) {
            Log.w(TAG, "*** invitation inbox UI cancelled, " + response);
            switchToMainScreen();
            return;
        }

        Log.d(TAG, "Invitation inbox UI succeeded.");
        Invitation invitation = data.getExtras().getParcelable(Multiplayer.EXTRA_INVITATION);

        // accept invitation
        if (invitation != null) {
            acceptInviteToRoom(invitation.getInvitationId());
        }
    }

    // Accept the given invitation.
    void acceptInviteToRoom(String invitationId) {
        // accept the invitation
        Log.d(TAG, "Accepting invitation: " + invitationId);

        mRoomConfig = RoomConfig.builder(mRoomUpdateCallback)
                .setInvitationIdToAccept(invitationId)
                .setOnMessageReceivedListener(mOnRealTimeMessageReceivedListener)
                .setRoomStatusUpdateCallback(mRoomStatusUpdateCallback)
                .build();

        //switchToScreen(R.id.screen_wait);
        keepScreenOn();
        //resetGameVars();
        generaMeteoritos = true;
        mRealTimeMultiplayerClient.join(mRoomConfig)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Room Joined Successfully!");
                    }
                });
    }

    // Activity is going to the background. We have to leave the current room.
    @Override
    public void onStop() {
        Log.d(TAG, "**** got onStop");

        // if we're in a room, leave it.
        leaveRoom();

        // stop trying to keep the screen on
        stopKeepingScreenOn();

        switchToMainScreen();

        super.onStop();
    }

    // Handle back key to make sure we cleanly leave a game if we are in the middle of one
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent e) {
        if (keyCode == KeyEvent.KEYCODE_BACK /*&& mCurScreen == R.id.screen_game*/) {
            leaveRoom();
            papel = new PaperView(getApplicationContext());
            papel.setDrawingCacheEnabled(true);
            return true;
        }
        return super.onKeyDown(keyCode, e);
    }

    // Leave the room.
    void leaveRoom() {
        Log.d(TAG, "Leaving room.");
        stopKeepingScreenOn();
        if (mRoomId != null) {
            mRealTimeMultiplayerClient.leave(mRoomConfig, mRoomId)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            mRoomId = null;
                            mRoomConfig = null;
                        }
                    });
            setContentView(R.layout.activity_main);
            //switchToScreen(R.id.screen_wait);
        } else {
            switchToMainScreen();
        }
        //finish();
        sm.unregisterListener(this);
    }

    // Show the waiting room UI to track the progress of other players as they enter the
    // room and get connected.
    void showWaitingRoom(Room room) {
        // minimum number of players required for our game
        // For simplicity, we require everyone to join the game before we start it
        // (this is signaled by Integer.MAX_VALUE).
        final int MIN_PLAYERS = Integer.MAX_VALUE;
        mRealTimeMultiplayerClient.getWaitingRoomIntent(room, MIN_PLAYERS)
                .addOnSuccessListener(new OnSuccessListener<Intent>() {
                    @Override
                    public void onSuccess(Intent intent) {
                        // show waiting room UI
                        startActivityForResult(intent, RC_WAITING_ROOM);
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the waiting room!"));
    }

    private InvitationCallback mInvitationCallback = new InvitationCallback() {
        // Called when we get an invitation to play a game. We react by showing that to the user.
        @Override
        public void onInvitationReceived(@NonNull Invitation invitation) {
            // We got an invitation to play a game! So, store it in
            // mIncomingInvitationId
            // and show the popup on the screen.
            mIncomingInvitationId = invitation.getInvitationId();
            ((TextView) findViewById(R.id.incoming_invitation_text)).setText(
                    invitation.getInviter().getDisplayName() + " " +
                            getString(R.string.is_inviting_you));
            switchToScreen(mCurScreen); // This will show the invitation popup
            yaDiagonal = true;
        }

        @Override
        public void onInvitationRemoved(@NonNull String invitationId) {

            if (mIncomingInvitationId.equals(invitationId) && mIncomingInvitationId != null) {
                mIncomingInvitationId = null;
                findViewById(R.id.invitation_popup).setVisibility(View.INVISIBLE);
                //switchToScreen(mCurScreen); // This will hide the invitation popup
            }
        }
    };

    /*
     * CALLBACKS SECTION. This section shows how we implement the several games
     * API callbacks.
     */

    private String mPlayerId;

    // The currently signed in account, used to check the account has changed outside of this activity when resuming.
    GoogleSignInAccount mSignedInAccount = null;

    private void onConnected(GoogleSignInAccount googleSignInAccount) {
        Log.d(TAG, "onConnected(): connected to Google APIs");
        if (mSignedInAccount != googleSignInAccount) {

            mSignedInAccount = googleSignInAccount;

            // update the clients
            mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(this, googleSignInAccount);
            mInvitationsClient = Games.getInvitationsClient(MainActivity.this, googleSignInAccount);

            // get the playerId from the PlayersClient
            PlayersClient playersClient = Games.getPlayersClient(this, googleSignInAccount);
            playersClient.getCurrentPlayer()
                    .addOnSuccessListener(new OnSuccessListener<Player>() {
                        @Override
                        public void onSuccess(Player player) {
                            mPlayerId = player.getPlayerId();

                            switchToMainScreen();
                        }
                    })
                    .addOnFailureListener(createFailureListener("There was a problem getting the player id!"));
        }

        // register listener so we are notified if we receive an invitation to play
        // while we are in the game
        mInvitationsClient.registerInvitationCallback(mInvitationCallback);

        // get the invitation from the connection hint
        // Retrieve the TurnBasedMatch from the connectionHint
        GamesClient gamesClient = Games.getGamesClient(MainActivity.this, googleSignInAccount);
        gamesClient.getActivationHint()
                .addOnSuccessListener(new OnSuccessListener<Bundle>() {
                    @Override
                    public void onSuccess(Bundle hint) {
                        if (hint != null) {
                            Invitation invitation =
                                    hint.getParcelable(Multiplayer.EXTRA_INVITATION);

                            if (invitation != null && invitation.getInvitationId() != null) {
                                // retrieve and cache the invitation ID
                                Log.d(TAG, "onConnected: connection hint has a room invite!");
                                acceptInviteToRoom(invitation.getInvitationId());
                            }
                        }
                    }
                })
                .addOnFailureListener(createFailureListener("There was a problem getting the activation hint!"));
    }

    private OnFailureListener createFailureListener(final String string) {
        return new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                handleException(e, string);
            }
        };
    }

    public void onDisconnected() {
        Log.d(TAG, "onDisconnected()");

        mRealTimeMultiplayerClient = null;
        mInvitationsClient = null;

        switchToMainScreen();
    }

    private RoomStatusUpdateCallback mRoomStatusUpdateCallback = new RoomStatusUpdateCallback() {
        // Called when we are connected to the room. We're not ready to play yet! (maybe not everybody
        // is connected yet).
        @Override
        public void onConnectedToRoom(Room room) {
            Log.d(TAG, "onConnectedToRoom.");

            //get participants and my ID:
            mParticipants = room.getParticipants();
            mMyId = room.getParticipantId(mPlayerId);

            // save room ID if its not initialized in onRoomCreated() so we can leave cleanly before the game starts.
            if (mRoomId == null) {
                mRoomId = room.getRoomId();
            }

            // print out the list of participants (for debug purposes)
            Log.d(TAG, "Room ID: " + mRoomId);
            Log.d(TAG, "My ID " + mMyId);
            Log.d(TAG, "<< CONNECTED TO ROOM>>");
        }

        // Called when we get disconnected from the room. We return to the main screen.
        @Override
        public void onDisconnectedFromRoom(Room room) {
            mRoomId = null;
            mRoomConfig = null;
            showGameError();
        }


        // We treat most of the room update callbacks in the same way: we update our list of
        // participants and update the display. In a real game we would also have to check if that
        // change requires some action like removing the corresponding player avatar from the screen,
        // etc.
        @Override
        public void onPeerDeclined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerInvitedToRoom(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onP2PDisconnected(@NonNull String participant) {
        }

        @Override
        public void onP2PConnected(@NonNull String participant) {
        }

        @Override
        public void onPeerJoined(Room room, @NonNull List<String> arg1) {
            updateRoom(room);
        }

        @Override
        public void onPeerLeft(Room room, @NonNull List<String> peersWhoLeft) {
            updateRoom(room);
        }

        @Override
        public void onRoomAutoMatching(Room room) {
            updateRoom(room);
        }

        @Override
        public void onRoomConnecting(Room room) {
            updateRoom(room);
        }

        @Override
        public void onPeersConnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }

        @Override
        public void onPeersDisconnected(Room room, @NonNull List<String> peers) {
            updateRoom(room);
        }
    };

    // Show error message about game being cancelled and return to main screen.
    void showGameError() {
        new android.app.AlertDialog.Builder(this)
                .setMessage(getString(R.string.game_problem))
                .setNeutralButton(android.R.string.ok, null).create();
        mContentView = findViewById(R.id.activity_challenge);
        setContentView(R.layout.activity_main);
        switchToMainScreen();
    }

    private RoomUpdateCallback mRoomUpdateCallback = new RoomUpdateCallback() {

        // Called when room has been created
        @Override
        public void onRoomCreated(int statusCode, Room room) {
            Log.d(TAG, "onRoomCreated(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomCreated, status " + statusCode);
                showGameError();
                return;
            }

            // save room ID so we can leave cleanly before the game starts.
            mRoomId = room.getRoomId();

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when room is fully connected.
        @Override
        public void onRoomConnected(int statusCode, Room room) {
            Log.d(TAG, "onRoomConnected(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }
            updateRoom(room);
        }

        @Override
        public void onJoinedRoom(int statusCode, Room room) {
            Log.d(TAG, "onJoinedRoom(" + statusCode + ", " + room + ")");
            if (statusCode != GamesCallbackStatusCodes.OK) {
                Log.e(TAG, "*** Error: onRoomConnected, status " + statusCode);
                showGameError();
                return;
            }

            // show the waiting room UI
            showWaitingRoom(room);
        }

        // Called when we've successfully left the room (this happens a result of voluntarily leaving
        // via a call to leaveRoom(). If we get disconnected, we get onDisconnectedFromRoom()).
        @Override
        public void onLeftRoom(int statusCode, @NonNull String roomId) {
            // we have left the room; return to main screen.
            Log.d(TAG, "onLeftRoom, code " + statusCode);
            switchToMainScreen();
        }
    };

    void updateRoom(Room room) {
        if (room != null) {
            mParticipants = room.getParticipants();
        }
        if (mParticipants != null) {
            //updatePeerScoresDisplay();
            if (mParticipants.size() < 1) {
                leaveRoom();
            }
        }

    }

    /*
     * GAME LOGIC SECTION. Methods that implement the game's rules.
     */

    // Current state of the game:
    int mSecondsLeft = -1; // how long until the game ends (seconds)
    final static int GAME_DURATION = 20; // game duration, seconds.
    int mScore = 0; // user's current score

    // Reset game variables in preparation for a new game.
    void resetGameVars() {
        mSecondsLeft = GAME_DURATION;
        mScore = 0;
        mParticipantScore.clear();
        mFinishedParticipants.clear();
    }

    // Start the gameplay phase of the game.
    void startGame(boolean multiplayer) {
        mMultiplayer = multiplayer;
        mContentView = papel;
        vidas=3;
        tiempo=0;
        x_nave=tamPantallaX/2-50;
        y_nave=tamPantallaY-150;
        pauseActivado=false;
        papel.reiniciarListaMeteoritos();
        terminado=false;
        papel.reiniciar();
        setContentView(papel);
        sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /*
     * COMMUNICATIONS SECTION. Methods that implement the game's network
     * protocol.
     */

    // Score of other participants. We update this as we receive their scores
    // from the network.
    Map<String, Integer> mParticipantScore = new HashMap<>();

    // Participants who sent us their final score.
    Set<String> mFinishedParticipants = new HashSet<>();

    // Called when we receive a real-time message from the network.
    // Messages in our game are made up of 2 bytes: the first one is 'F' or 'U'
    // indicating
    // whether it's a final or interim score. The second byte is the score.
    // There is also the
    // 'S' message, which indicates that the game should start.
    OnRealTimeMessageReceivedListener mOnRealTimeMessageReceivedListener = new OnRealTimeMessageReceivedListener() {
        @Override
        public void onRealTimeMessageReceived(@NonNull RealTimeMessage realTimeMessage) {
            byte[] buf = realTimeMessage.getMessageData();
            String sender = realTimeMessage.getSenderParticipantId();
            Log.d(TAG, "Message received: " + (char) buf[0] + "/" + (int) buf[1]);

            if (buf[0] == 'F' || buf[0] == 'U') {
                // score update.
                int existingScore = mParticipantScore.containsKey(sender) ?
                        mParticipantScore.get(sender) : 0;
                int thisScore = (int) buf[1];
                if (thisScore > existingScore) {
                    // this check is necessary because packets may arrive out of
                    // order, so we
                    // should only ever consider the highest score we received, as
                    // we know in our
                    // game there is no way to lose points. If there was a way to
                    // lose points,
                    // we'd have to add a "serial number" to the packet.
                    mParticipantScore.put(sender, thisScore);
                }

                // update the scores on the screen
                //updatePeerScoresDisplay();

                // if it's a final score, mark this participant as having finished
                // the game
                if ((char) buf[0] == 'F') {
                    mFinishedParticipants.add(realTimeMessage.getSenderParticipantId());
                }
            }
        }
    };

    // Broadcast my score to everybody else.
    void broadcastScore(boolean finalScore, byte tag) {
        if (!mMultiplayer) {
            // playing single-player mode
            return;
        }

        // First byte in message indicates whether it's a final score or not
        mMsgBuf[0] = (byte) (finalScore ? 'F' : 'U');

        // MY GAME
        mMsgBuf[0] = tag;
        switch (tag){
            case (byte)'N':
                mMsgBuf[1] = (byte) (x_nave/100);
                mMsgBuf[2] = (byte) (x_nave%100);
                mMsgBuf[3] = (byte) (y_nave/100);
                mMsgBuf[4] = (byte) (y_nave%100);
                mMsgBuf[5] = (byte) (posIDNaveRival);
                mMsgBuf[6] = (byte) (tamPantallaY/100);
                mMsgBuf[7] = (byte) (tamPantallaY%100);
                mMsgBuf[8] = (byte) (tamPantallaX/100);
                mMsgBuf[9] = (byte) (tamPantallaX%100);
                //Enviar cada nave

                break;
            case (byte)'B':
                mMsgBuf[1] = (byte) (x_bala/100);
                mMsgBuf[2] = (byte) (x_bala%100);
                mMsgBuf[3] = (byte) (y_bala/100);
                mMsgBuf[4] = (byte) (y_bala%100);
                break;
            case (byte)'M':
                Log.d("M", "Imposible");
                mMsgBuf[1] = (byte) (naveNueva.getPosX()/100);
                mMsgBuf[2] = (byte) (naveNueva.getPosX()%100);
                mMsgBuf[3] = (byte) (naveNueva.getPosY()/100);
                mMsgBuf[4] = (byte) (naveNueva.getPosY()%100);
                mMsgBuf[5] = (byte) (naveNueva.getIntervaloTiempo()/100);
                mMsgBuf[6] = (byte) (naveNueva.getIntervaloTiempo()%100);
                mMsgBuf[7] = (byte) (naveNueva.isMovX() ? '1' : '0');
                mMsgBuf[8] = (byte) (naveNueva.getImagen());
                break;
            case (byte)'C':
                //mMsgBuf[1] = (byte)(pos_met);
                break;
            case (byte)'E':
                //mMsgBuf[1] = (byte)(pos_met);
                break;
            case (byte)'V':
                break;
        }

        // Second byte is the score.
        //mMsgBuf[1] = (byte) mScore;

        // Send to every other participant.
        for (Participant p : mParticipants) {
            if (p.getParticipantId().equals(mMyId)) {
                continue;
            }
            if (p.getStatus() != Participant.STATUS_JOINED) {
                continue;
            }
            if (finalScore) {
                // final score notification must be sent via reliable message
                mRealTimeMultiplayerClient.sendReliableMessage(mMsgBuf,
                        mRoomId, p.getParticipantId(), new RealTimeMultiplayerClient.ReliableMessageSentCallback() {
                            @Override
                            public void onRealTimeMessageSent(int statusCode, int tokenId, String recipientParticipantId) {
                                Log.d(TAG, "RealTime message sent");
                                Log.d(TAG, "  statusCode: " + statusCode);
                                Log.d(TAG, "  tokenId: " + tokenId);
                                Log.d(TAG, "  recipientParticipantId: " + recipientParticipantId);
                            }
                        })
                        .addOnSuccessListener(new OnSuccessListener<Integer>() {
                            @Override
                            public void onSuccess(Integer tokenId) {
                                Log.d(TAG, "Created a reliable message with tokenId: " + tokenId);
                            }
                        });
            } else {
                // it's an interim score notification, so we can use unreliable
                mRealTimeMultiplayerClient.sendUnreliableMessage(mMsgBuf, mRoomId,
                        p.getParticipantId());
            }
        }
    }

    /*
     * UI SECTION. Methods that implement the game's UI.
     */

    // This array lists everything that's clickable, so we can install click
    // event handlers.
    final static int[] CLICKABLES = {
            R.id.button_accept_popup_invitation, R.id.button_invite_players,
            R.id.button_quick_game, R.id.button_see_invitations, R.id.button_sign_in,
            R.id.button_sign_out
    };

    // This array lists all the individual screens our game has.
    final static int[] SCREENS = {
            R.id.screen_main, R.id.screen_sign_in,
            R.id.screen_wait
    };
    int mCurScreen = -1;

    void switchToScreen(int screenId) {
        //mContentView = findViewById(R.id.activity_challenge);
        //setContentView(R.layout.activity_main);
        // make the requested screen visible; hide all others.
        for (int id : SCREENS) {
            findViewById(id).setVisibility(screenId == id ? View.VISIBLE : View.GONE);
        }
        mCurScreen = screenId;


        // should we show the invitation popup?
        boolean showInvPopup;
        if (mIncomingInvitationId == null) {
            // no invitation, so no popup
            showInvPopup = false;
        } else /*if (mMultiplayer)*/ {
            // if in multiplayer, only show invitation on main screen
            showInvPopup = (mCurScreen == R.id.screen_main);
        } /*else {
            // single-player: show on main screen and gameplay screen
            //showInvPopup = (mCurScreen == R.id.screen_main || mCurScreen == R.id.screen_game);
        }*/
        findViewById(R.id.invitation_popup).setVisibility(showInvPopup ? View.VISIBLE : View.GONE);
    }

    void switchToMainScreen() {
        //setContentView(R.layout.activity_main);
        if (mRealTimeMultiplayerClient != null) {
            switchToScreen(R.id.screen_main);
        } else {
            switchToScreen(R.id.screen_sign_in);
        }
    }

    // formats a score as a three-digit number
    String formatScore(int i) {
        if (i < 0) {
            i = 0;
        }
        String s = String.valueOf(i);
        return s.length() == 1 ? "00" + s : s.length() == 2 ? "0" + s : s;
    }

    /*
     * MISC SECTION. Miscellaneous methods.
     */


    // Sets the flag to keep this screen on. It's recommended to do that during
    // the
    // handshake when setting up a game, because if the screen turns off, the
    // game will be
    // cancelled.
    void keepScreenOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    // Clears the flag that keeps the screen on.
    void stopKeepingScreenOn() {
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private static final int UI_ANIMATION_DELAY = 0;
    Bitmap img = null;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    public Drawable imagenVidas(int v, int x, int y){
        if (v == 3){
            imgVida3.setBounds(x,y,x+120,y+40);
            return imgVida3;
        }else if (v == 2){
            imgVida2.setBounds(x,y,x+80,y+40);
            return imgVida2;
        }else{
            imgVida1.setBounds(x,y,x+40,y+40);
            return imgVida1;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        x_sensor = sensorEvent.values[0];
        y_sensor = sensorEvent.values[1];
        z_sensor = sensorEvent.values[2];
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class PaperView extends View {
        Paint paint;
        public PaperView(Context context) {
            super(context);
            resetImgRefenrencias();
            reiniciar();
            paint = new Paint();
        }

        public void onDraw(Canvas canvas){
            super.onDraw(canvas);
            c = canvas;

            paint.setColor(Color.GREEN);
            paint.setTextSize(50);
            paint.setAntiAlias(true);
            canvas.drawPaint(paint);
            configurarPosNave_yFondo();
            dibujeTodo(canvas);
            tiempo++;
            puntos = tiempo/60;
            monedas = puntos/10 + puntos_Extra/10;
            int xBal, yBal;
            int cantNaves = misNaves.size()*5;
            mMsgBuf[11+cantNaves] = (byte) balas.size();
            for (int i=0; i<balas.size(); i++){
                xBal=balas.get(i).getPosX(); yBal = balas.get(i).getPosY();

                b = new Rect(xBal, yBal, xBal+tamBalaX, yBal+tamBalaY);
                n = new Rect(x_nave_Rival, y_nave_Rival, x_nave_Rival+tamNaveX, y_nave_Rival+tamNaveY);

                if(Rect.intersects(b,n)){ //colision bala-naveRival

                    mpChoque.seekTo(1000);
                    mpChoque.start();

                    balas.get(i).setPosY(-tamPantallaY);
                    //gam over
                    x_nave_Rival=tamPantallaX/2-tamNaveX/2;
                    y_nave_Rival=0;
                    break;
                }else if(colisionMisBalasNaves()){
                    balas.remove(i);
                    //pauseActivado = true;
                    xBal = -tamPantallaX;
                    yBal = -tamPantallaY;
                }else if (balas.get(i).getPosY()<0){
                    balas.remove(i);
                    xBal = -tamPantallaX;
                    yBal = -tamPantallaY;
                }else {
                    yBal-=8;
                    balas.get(i).setPosY(yBal);
                    imgMisil.setBounds(xBal,yBal,xBal+tamBalaX,yBal+tamBalaY);
                    imgMisil.draw(canvas);
                }
                mMsgBuf[12+cantNaves+4*i] = (byte) (xBal/100);
                mMsgBuf[13+cantNaves+4*i] = (byte) (xBal%100);
                mMsgBuf[14+cantNaves+4*i] = (byte) (yBal/100);
                mMsgBuf[15+cantNaves+4*i] = (byte) (yBal%100);


            }
            broadcastScore(false, (byte) 'N');
            for (int j=0; j<balas_Rival.size(); j++){
                yBal = balas_Rival.get(j).getPosY();
                xBal = balas_Rival.get(j).getPosX();
                imgMisilRival.setBounds(xBal,yBal,xBal+tamBalaX,yBal+tamBalaY);
                imgMisilRival.draw(canvas);

                b = new Rect(xBal, yBal, xBal+tamBalaX, yBal+tamBalaY);
                Log.d("Misil", xBal+" ---"+b.left+ "  +++  "+yBal+" /// " + b.top);
                n = new Rect(x_nave, y_nave, x_nave+tamNaveX, y_nave+tamNaveY);
                if(Rect.intersects(b,n)){ //colision bala-meteorito

                    mpChoque.seekTo(1000);
                    mpChoque.start();

                    balas_Rival.remove(j);
                    vidaPerdida(canvas);
                    x_nave=tamPantallaX/2-tamNaveX/2;
                    y_nave=tamPantallaY-tamNaveY/2;
                    continue;
                }
                if(!colisionBalaconBala(canvas))
                    colisionBalasNaves();
            }

            if(tiempo%1000 == 0){
                vel = vel + 1;
                if (tiempo100 > 20){
                    tiempo700 = tiempo700 - 20;
                    tiempo500 = tiempo500 - 20;
                    tiempo300 = tiempo300 - 20;
                    tiempo100 = tiempo100 - 20;
                }

            }
            nuevaNave();

            if(vidas>0 && !pauseActivado && !terminado){
                invalidate();
            }
        }

        private boolean colisionMisBalasNaves() {
            int xNave, yNave;
            for (int j = 0; j < navesRival.size(); j++) {
                xNave = navesRival.get(j).getPosX();
                yNave = navesRival.get(j).getPosY();
                n = new Rect(xNave, yNave, xNave + tamNavePeqX, yNave + tamNavePeqY);
                if (Rect.intersects(b, n)) {
                    //balas_Rival.remove(pos);
                    //pauseActivado = true;
                    return true;
                    //balas.remove(i);
                }
            }
            return false;
        }

        public boolean colisionBalaconBala(Canvas canvas){
            int xBal, yBal;
            for (int i=0; i<balas.size(); i++) {
                xBal = balas.get(i).getPosX();
                yBal = balas.get(i).getPosY();
                n = new Rect(xBal, yBal, xBal+tamBalaX, yBal+tamBalaY);
                Log.d("Misil Misil", xBal+" ---"+n.left+ "  +++  "+yBal+" /// " + n.top);
                //n.set(xBal, yBal, xBal+tamBalaX, yBal+tamBalaY);
                if( Rect.intersects(b, n) ){
                    balas.remove(i);
                    //canvas.drawCircle(xBal, yBal, 5, paint);
                    //pauseActivado = true;
                    Log.d("Rect Balas B", "X: "+b.left+" Y: "+b.top+" tX: "+b.right+" tY: "+b.bottom);
                    Log.d("Rect Balas N", "X: "+n.left+" Y: "+n.top+" tX: "+n.right+" tY: "+n.bottom);
                    //balas_Rival.remove(pos);
                    return true;
                }
            }
            return false;
        }
        public void vidaPerdida(Canvas canvas){
            vidas = vidas - 1;
            broadcastScore(false, (byte) 'V');
            if (vibrator != null) {
                vibrator.vibrate(500);
            }
            reiniciarListaMeteoritos();
            if (vidas == 0) {
                terminado = true;
                broadcastScore(false, (byte) 'T');
                dibujeTodo(canvas);
                vel = 5;
            }
        }
        public void configureNaves(Canvas canvas){
            imgNaveRival.setBounds(x_nave_Rival, y_nave_Rival, x_nave_Rival+tamNaveX, y_nave_Rival+tamNaveY);
            imgNaveRival.draw(canvas);
            imagenVidas(vidas_Rival, 10, 10).draw(canvas);
            Drawable imgNaveR;
            for(int i=0; i < navesRival.size(); i++) {
                imgNaveR = imagenesNavesInv[navesRival.get(i).getImagen()];//getApplicationContext().getResources().getDrawable(naves[img_NaveRival.get(i)]);
                int xPos = navesRival.get(i).getPosX();
                int yPos = navesRival.get(i).getPosY();
                int vidasRival = navesRival.get(i).getVidas();

                imgNaveR.setBounds(xPos, yPos, xPos + tamNavePeqX, yPos + tamNavePeqY);

                imgNaveR.draw(canvas);
            }
            Log.d("Naves", "Tengo: "+misNaves.size());
            mMsgBuf[10] = (byte) (misNaves.size()); //Cantidad de naves
            Spacecraft n;
            for(int i=0; i < misNaves.size(); i++) {
                imgNaveR = imagenesNaves[misNaves.get(i).getImagen()];//getApplicationContext().getResources().getDrawable(naves[img_NaveRival.get(i)]);
                n = misNaves.get(i);
                int xPos = n.getPosX();
                int yPos = n.getPosY();
                if (tiempo % n.getIntervaloTiempo() == 0) {
                    Shot shot = new Shot(xPos + tamNavePeqX / 2 - tamBalaX/2, yPos - tamBalaY);
                    balas.add(shot);
                    //y_balas_Rival.add(yPos+tamNaveY);
                }
                boolean mov = n.isMovX();
                if (mov) {
                    xPos += 2;
                    if (xPos >= tamPantallaX - tamNavePeqX / 2) {
                        xPos = tamPantallaX - tamNavePeqX / 2;
                        misNaves.get(i).setMovX(!mov);
                    }
                } else {
                    xPos -= 2;
                    if (xPos <= -tamNavePeqX / 2) {
                        xPos = -tamNavePeqX / 2;
                        misNaves.get(i).setMovX(!mov);
                    }
                }

                mov = n.isMovY();
                if (mov) {
                    yPos += 2;
                    if (yPos >= tamPantallaY - tamNavePeqY) {
                        yPos = tamPantallaY  - tamNavePeqY;
                        misNaves.get(i).setMovY(!mov);
                    }
                } else {
                    yPos -= 2;
                    if (yPos <= tamPantallaY / 2) {
                        yPos = tamPantallaY / 2;
                        misNaves.get(i).setMovY(!mov);
                    }
                }

                misNaves.get(i).setPosX(xPos);
                misNaves.get(i).setPosY(yPos);

                mMsgBuf[11+5*i] = (byte) (xPos/100);
                mMsgBuf[12+5*i] = (byte) (xPos%100);
                mMsgBuf[13+5*i] = (byte) (yPos/100);
                mMsgBuf[14+5*i] = (byte) (yPos%100);
                mMsgBuf[15+5*i] = (byte) (n.getImagen());

                imgNaveR.setBounds(xPos, yPos, xPos + tamNavePeqX, yPos + tamNavePeqY);
                imgNaveR.draw(canvas);
            }
        }

        public void resetImgRefenrencias(){
            Context context = getApplicationContext();
            imgNave = getNave(context);
            imgMeteorito = context.getResources().getDrawable(R.drawable.m);
            imgFondo = context.getResources().getDrawable(R.drawable.stars);
            imgFondo2 = context.getResources().getDrawable(R.drawable.stars);
            imgGameOver = context.getResources().getDrawable(R.drawable.game_over);
            imgMisil = context.getResources().getDrawable(R.drawable.misil);
            imgVida1 = context.getResources().getDrawable(R.drawable.unavida);
            imgVida2 = context.getResources().getDrawable(R.drawable.dosvidas);
            imgVida3 = context.getResources().getDrawable(R.drawable.tresvidas);
            imPause = context.getResources().getDrawable(android.R.drawable.ic_media_pause);
            imPlay = context.getResources().getDrawable(R.drawable.play);

            naves = new int[]{R.drawable.nave24,
                    R.drawable.nave9,
                    R.drawable.nave5,
                    R.drawable.nave3,
                    R.drawable.nave4,
                    R.drawable.nave12,
                    R.drawable.nave10,
                    R.drawable.nave1};

            imagenesNaves = new Drawable[naves.length];
            imagenesNavesInv = new Drawable[naves.length];
            Matrix matrix = new Matrix();
            Bitmap bm;// = BitmapFactory.decodeResource(getResources(), config.getIdNave());
            matrix.postRotate(180.0f);
            //Girando las naves rivales
            imgNaveRival = imgNave;
            for (int j =0; j<naves.length; j++){
                bm = BitmapFactory.decodeResource(getResources(), naves[j]);
                bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
                imagenesNavesInv[j] = new BitmapDrawable(getResources(), bm);
                imagenesNaves[j] = context.getResources().getDrawable(naves[j]);

                if (R.drawable.nave24 == naves[j]){
                    posIDNaveRival = j;
                    imgNaveRival = imagenesNavesInv[j];
                }

            }

            //Girando el misil rival
            bm = BitmapFactory.decodeResource(getResources(), R.drawable.misil2);
            bm = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), matrix, true);
            imgMisilRival = new BitmapDrawable(getResources(), bm);

            meteoritos = new int[]{R.drawable.m, R.drawable.m2, R.drawable.m3};
            tamImgMet = new int[meteoritos.length][2];
            for (int i=0; i<meteoritos.length; i++){
                bm = BitmapFactory.decodeResource(getResources(), naves[i]);
                tamImgMet[i][0] = bm.getWidth();
                tamImgMet[i][1] = bm.getHeight();
            }
        }

        public void dibujeTodo(Canvas canvas){
            imgNave.setBounds(x_nave, y_nave, x_nave + tamNaveX, y_nave + tamNaveY);
            imgFondo.setBounds(0,y_fondo,tamPantallaX,tamPantallaY+y_fondo+5);
            imgFondo2.setBounds(0,y_fondo2,tamPantallaX, y_inferior);
            imPause.setBounds(tamPantallaX-70, 0, tamPantallaX, 70);
            imgFondo.draw(canvas);
            imgFondo2.draw(canvas);
            if(terminado){
                dibujeFinJuego(canvas);
            }else{
                imgNave.draw(canvas);
                imPause.draw(canvas);
                canvas.drawText(mParticipants.size()+"",tamPantallaX-100,90,paint);
                //retorno
                canvas.drawText ("Coins: "+ monedas,0,150,paint);
                imagenVidas(vidas, 10, tamPantallaY-50).draw(canvas);
                configureNaves(canvas);
            }
            if (pauseActivado){
                imPlay.setBounds(tamPantallaX/2 -200, tamPantallaY/2 - 100, tamPantallaX/2 + 200, tamPantallaY/2 + 100);
                imPlay.draw(canvas);
            }
        }

        public void configurarPosNave_yFondo(){
            if (z_sensor<4){
                speed = 8;
            }else if (z_sensor<6){
                speed = 7;
            }else if (z_sensor<8){
                speed = 6;
            }else{
                speed = 4;
            }
            speed = (int) (Math.abs(x_sensor)*3);
            if (x_sensor>0&&y_sensor<0){//izquierda-arriba
                x = -1* speed;
                y = -1* speed;
            }else if (x_sensor<0&&y_sensor<0){//derecha-arriba
                x = 1* speed;
                y = -1* speed;
            }else if (x_sensor>0&&y_sensor>0){//izquierda-abajo
                x = -1* speed;
                y = 1* speed;
            }else if (x_sensor<0&&y_sensor>0){//derecha-abajo
                x = 1* speed;
                y = 1* speed;
            }
            x_nave = x_nave + x;
            y_nave = y_nave + y;

            if (x_nave <= 0){
                x_nave =+speed;
                x_nave = 0;
            }
            if (x_nave +  tamNaveX >= tamPantallaX){
                x_nave -= speed;
                x_nave = tamPantallaX-tamNaveX;
            }
            if (y_nave <= tamPantallaY/2){
                y_nave += speed;
                y_nave = tamPantallaY/2;
            }
            if (y_nave + tamNaveY >= tamPantallaY){
                y_nave -= speed;
                y_nave = tamPantallaY -tamNaveY;
            }

            y_fondo+=2;
            y_fondo2+=2;
            y_inferior+=2;
            if(y_fondo>tamPantallaY){
                y_fondo = -tamPantallaY;
            }
            if(y_fondo2>tamPantallaY){
                y_fondo2 = -tamPantallaY;
                y_inferior = 0;
            }
        }

        public void nuevaNave(){
            if (tiempo%tiempo500 == 0){
                int pos_nave = posIDNaveRival;//(int)(Math.random()*(naves.length));
                Boolean mov = new Random().nextBoolean();
                naveNueva = new Spacecraft(tamNavePeqX/2,tamPantallaY+tamNavePeqY,pos_nave,pos_nave,mov,false);
                Log.d("nave", "otra añadida " + pos_nave);
                pos_nave = (int)(Math.random()*(100) + 50);
                naveNueva.setIntervaloTiempo(pos_nave);
                misNaves.add(naveNueva);
            }
        }

        public void dibujeFinJuego(Canvas canvas){
            paint.setTextSize(50);
            String add[] = {getString(R.string.win), getString(R.string.lose)};
            int a=1;
            if(ganaste){
                a=0;
            }
            canvas.drawText(getString(R.string.versus),tamPantallaX/2-120, tamPantallaY/2-200, paint);
            for (Participant p : mParticipants) {
                if (p.getParticipantId().equals(mMyId)){
                    canvas.drawText(p.getDisplayName()+" - "+add[a],tamPantallaX/2-200, tamPantallaY/2-350, paint);
                }else{
                    canvas.drawText(p.getDisplayName()+" - "+add[1-a], tamPantallaX/2-200, tamPantallaY/2-100, paint);
                }
            }

            img = papel.getDrawingCache();

            imPlay.setBounds(tamPantallaX/2-225, tamPantallaY/2+30, tamPantallaX/2+225, tamPantallaY/2+230);
            imPlay.draw(canvas);

            imgFacebook.setBounds(tamPantallaX/2-50,tamPantallaY/2+300, tamPantallaX/2+50, tamPantallaY/2+400);
            imgFacebook.draw(canvas);
        }

        public void reiniciarListaMeteoritos(){
            balas = new ArrayList<>();
            balas_Rival = new ArrayList<>();
        }

        public boolean onTouchEvent(MotionEvent event){
            if (event.getAction()==MotionEvent.ACTION_DOWN){
                //Log.d("coor", "X: " + event.getX()+ " Y: " + event.getY() + " pX: "+tamPantallaX +  " py: " + tamPantallaY);
                if (pauseActivado && event.getX() >= tamPantallaX/2 -225 && event.getX() <= tamPantallaX/2 + 225 && event.getY() >= tamPantallaY/2 - 100 && event.getY() <= tamPantallaY/2 + 100){
                    pauseActivado = false;
                    broadcastScore(false, (byte)'R');
                    invalidate();
                }else if (!terminado  && !pauseActivado && event.getX() > tamPantallaX -70 && event.getY() < 70){
                    pauseActivado = true;
                    broadcastScore(false, (byte)'P');
                    invalidate();
                }else if (terminado && event.getX() >= tamPantallaX/2-150 && event.getX() <= tamPantallaX/2+150 && event.getY() >= tamPantallaY/2+30 && event.getY() <= tamPantallaY/2+230){
                    reiniciar();
                    broadcastScore(false, (byte)'O');
                    invalidate();
                }else if(!pauseActivado && !terminado){
                    mp.seekTo(200);
                    mp.start();
                    x_bala = x_nave+tamNaveX/2-tamBalaX/2;
                    y_bala = y_nave-tamBalaY;
                    Shot shot = new Shot(x_bala, y_bala);
                    balas.add(shot);
                }

            }
            return true;
        }

        private void reiniciar() {
            vel = 5;
            vidas_Rival = 3;
            vidas = 3;
            tiempo = 0;
            tiempo100 = 100;
            tiempo300 = 300;
            tiempo500 = 500;
            tiempo700 = 700;
            terminado = false;
            balas = new ArrayList<>();
            balas_Rival = new ArrayList<>();
            misNaves = new ArrayList<>();
            navesRival = new ArrayList<>();
        }

        public Drawable getNave(Context ctx){
            return ctx.getResources().getDrawable(R.drawable.nave24);
        }

        public void colisionBalasNaves(){
            int xNave, yNave;

            for (int j=0; j<misNaves.size(); j++){
                xNave = misNaves.get(j).getPosX();
                yNave = misNaves.get(j).getPosY();
                n = new Rect(xNave, yNave, xNave+tamNavePeqX, yNave+tamNavePeqY);
                if (Rect.intersects(b,n)){
                    misNaves.remove(j);
                    break;
                }
            }
        }
    }
}