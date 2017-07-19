//
//  FirebaseManager.java
//  Waitr
//
//  Created by Adam Murnane on 12/24/15.
//  Copyright (c) 2015 Waitr Inc. All rights reserved.
//

package com.waitr.thelazydev.tabletop;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class FirebaseManager {
    public static final String TAG = "FirebaseManager";

    private volatile boolean mObservingAuthData;
    private String mCustomToken;
    private boolean mIsUserAuthorized = false;
    private Context mContext;
    private static FirebaseManager sInstance;

    private DatabaseReference mFirebase;
    private List<ValueEventListener> mFirebaseEventListeners = new ArrayList<>();


    private Handler mTimeoutHandler;


    private static FirebaseAuth sFireBaseAuth = FirebaseAuth.getInstance();

    private FirebaseManager(Context context) {
        this.mContext = context;
        mTimeoutHandler = new Handler();

    }

    public static FirebaseManager getSharedManager(Context context) {
        synchronized (FirebaseManager.class) {
            if (sInstance == null) {
                sInstance = new FirebaseManager(context);
            }
            return sInstance;
        }
    }

    public interface AuthCompletionHandler {
        void onAuthSuccess(FirebaseAuth authData);

        void onAuthFailure(Exception error);
    }

    public interface SnapshotCompletionHandler {
        void onSuccess(String url, DataSnapshot snapshot);

        void onFailure(Exception error);
    }

    public interface BooleanValueCompletionHandler {
        void onSuccess(boolean value);

        void onFailure(Exception error);
    }


    public interface ReserveGroupCodeCompletionHandler {
        void onSuccess(String groupCode, String orderId);

        void onFaliure(Exception error);
    }

    public interface FindActiveOrderCompletionHandler {
        void onSuccess(DataSnapshot snapshot);

        void onFailure(Exception error);
    }

    public boolean isObservingAuthData() {
        return mObservingAuthData;
    }

    public String getCustomToken() {
        return mCustomToken;
    }

    public void setCustomToken(String customToken) {
        mCustomToken = customToken;
    }


    public void authenticate(final DatabaseReference firebase, final AuthCompletionHandler authCompletionHandler, boolean needUserCreds) {
        if (sFireBaseAuth.getCurrentUser() == null) {

            sFireBaseAuth.signInAnonymously()
                    .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                        @Override
                        public void onSuccess(AuthResult authResult) {
                            if (!isObservingAuthData()) {
                                mObservingAuthData = true;
                                sFireBaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                                    @Override
                                    public void onAuthStateChanged(FirebaseAuth authData) {
                                        if (authData == null) {
                                            renewCustomToken(firebase);
                                        }
                                    }
                                });
                            }
                            if (authCompletionHandler != null) {
                                authCompletionHandler.onAuthSuccess(sFireBaseAuth);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e(TAG, "Unable to authenticate with Firebase: " + e.getMessage());
                            if (authCompletionHandler != null) {
                                authCompletionHandler.onAuthFailure(e);
                            }
                        }
                    });


        } else {
            // Already authenticated
            if (!isObservingAuthData()) {
                mObservingAuthData = true;
                sFireBaseAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
                    @Override
                    public void onAuthStateChanged(FirebaseAuth authData) {
                        if (authData == null) {
                            renewCustomToken(firebase);
                        }
                    }
                });
            }

            if (authCompletionHandler != null) {
                authCompletionHandler.onAuthSuccess(sFireBaseAuth);
            }
        }

    }

    public void clearListeners() {
        // ResetDatabaseReference instances
        if (mFirebase != null) {
            for (ValueEventListener listener : mFirebaseEventListeners) {
                mFirebase.removeEventListener(listener);
            }
            mFirebase = null;
        }
        mFirebaseEventListeners.clear();
    }


    public void findActiveOrder(String userId, final FindActiveOrderCompletionHandler handler) {
        String baseUrl = getFirebaseBaseUrl();
        final String fullUrl = baseUrl + "lookups/users_to_orders/" + userId;
        final DatabaseReference userActiveOrder = FirebaseDatabase.getInstance().getReferenceFromUrl(fullUrl);

        Log.e("fullUrl", fullUrl);

        authenticate(userActiveOrder, new AuthCompletionHandler() {
            @Override
            public void onAuthSuccess(FirebaseAuth authData) {
                userActiveOrder.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.v(TAG, "User Active Order:\n" + dataSnapshot.getValue());
                        if (handler != null) {
                            handler.onSuccess(dataSnapshot);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError firebaseError) {
                        Log.e(TAG, firebaseError.getMessage());
                        if (handler != null) {
                            handler.onFailure(firebaseError.toException());
                        }
                    }
                });
            }

            @Override
            public void onAuthFailure(Exception error) {
                Log.e(TAG, error.getMessage());
                if (handler != null) {
                    handler.onFailure(error);
                }
            }
        }, true);
    }

    public void deleteActiveOrder(String userId) {
        String baseUrl = getFirebaseBaseUrl();
        final String fullUrl = baseUrl + "user_active_order/" + userId;
        final DatabaseReference userActiveOrder = FirebaseDatabase.getInstance().getReferenceFromUrl(fullUrl);

        authenticate(userActiveOrder, new AuthCompletionHandler() {
            @Override
            public void onAuthSuccess(FirebaseAuth authData) {
                userActiveOrder.setValue(null);
            }

            @Override
            public void onAuthFailure(Exception error) {
                Log.e(TAG, error.getMessage());
            }
        }, true);
    }

    private void renewCustomToken(DatabaseReference firebase) {
        authenticate(firebase, null, true);
    }

    public String getFirebaseBaseUrl() {
        return "https://retaurant-reservations.firebaseio.com/lookups/code_to_table";
    }
}
