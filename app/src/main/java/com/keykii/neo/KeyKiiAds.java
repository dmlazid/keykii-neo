package com.keykii.neo;

import android.app.Activity;
import android.content.SharedPreferences;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.ump.ConsentInformation;
import com.google.android.ump.ConsentRequestParameters;
import com.google.android.ump.UserMessagingPlatform;

/**
 * KeyKii ad controller.
 *
 * Rewarded: one completed reward grants all ad-gated themes/fonts for 24 hours.
 * Interstitial: occasional natural-break ad only; never shown from the IME,
 * at app launch, while typing, or after every action.
 */
final class KeyKiiAds {
    private static final String PREF_LAST_INTERSTITIAL="ad_last_interstitial_at";
    private static final long INTERSTITIAL_MIN_INTERVAL=4L*60L*1000L;

    private final Activity activity;
    private final SharedPreferences prefs;

    private ConsentInformation consentInformation;
    private RewardedAd rewardedAd;
    private InterstitialAd interstitialAd;

    private boolean initialized=false;
    private boolean rewardedLoading=false;
    private boolean interstitialLoading=false;
    private boolean rewardShowing=false;
    private boolean pendingRewardRequest=false;

    private Runnable pendingUnlock;
    private Runnable pendingUnavailable;

    private int naturalBreaks=0;
    private int nextInterstitialAt=5+(int)(Math.abs(System.nanoTime())%4L);

    KeyKiiAds(Activity activity,SharedPreferences prefs){
        this.activity=activity;
        this.prefs=prefs;
    }

    void start(){
        if(activity.isFinishing()||KeyKiiAccess.isOwner(activity))return;
        consentInformation=UserMessagingPlatform.getConsentInformation(activity);
        ConsentRequestParameters params=
                new ConsentRequestParameters.Builder().build();

        consentInformation.requestConsentInfoUpdate(
                activity,
                params,
                () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                        activity,
                        formError -> maybeInitializeAds()
                ),
                formError -> maybeInitializeAds()
        );

        if(consentInformation.canRequestAds())maybeInitializeAds();
    }

    private synchronized void maybeInitializeAds(){
        if(initialized||activity.isFinishing()||KeyKiiAccess.isOwner(activity))return;
        if(consentInformation!=null&&!consentInformation.canRequestAds())return;
        initialized=true;

        new Thread(() ->
                MobileAds.initialize(
                        activity,
                        initializationStatus -> activity.runOnUiThread(() -> {
                            loadRewarded();
                            loadInterstitial();
                        })
                ),
                "KeyKii-MobileAds"
        ).start();
    }

    boolean isTestMode(){
        return BuildConfig.ADMOB_TEST_MODE;
    }

    boolean privacyOptionsRequired(){
        return consentInformation!=null&&
                consentInformation.getPrivacyOptionsRequirementStatus()==
                        ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED;
    }

    void showPrivacyOptions(Runnable done){
        UserMessagingPlatform.showPrivacyOptionsForm(
                activity,
                formError -> {
                    if(done!=null)done.run();
                    maybeInitializeAds();
                }
        );
    }

    boolean has24HourPass(){
        return KeyKiiAccess.canUsePremium(activity);
    }

    String passRemaining(){
        return KeyKiiAccess.remainingLabel(activity);
    }

    void showRewardedFor24Hours(Runnable onUnlocked,Runnable onUnavailable){
        if(KeyKiiAccess.canUsePremium(activity)){
            if(onUnlocked!=null)onUnlocked.run();
            return;
        }

        pendingUnlock=onUnlocked;
        pendingUnavailable=onUnavailable;
        pendingRewardRequest=true;

        if(!initialized){
            start();
            return;
        }

        if(rewardedAd!=null){
            showLoadedRewarded();
        }else{
            loadRewarded();
        }
    }

    private void loadRewarded(){
        if(KeyKiiAccess.isOwner(activity))return;
        if(rewardedLoading||rewardedAd!=null||activity.isFinishing())return;
        if(BuildConfig.ADMOB_REWARDED_ID==null||BuildConfig.ADMOB_REWARDED_ID.isEmpty())return;
        rewardedLoading=true;

        RewardedAd.load(
                activity,
                BuildConfig.ADMOB_REWARDED_ID,
                new AdRequest.Builder().build(),
                new RewardedAdLoadCallback(){
                    @Override public void onAdLoaded(RewardedAd ad){
                        rewardedLoading=false;
                        rewardedAd=ad;
                        if(pendingRewardRequest)showLoadedRewarded();
                    }

                    @Override public void onAdFailedToLoad(LoadAdError error){
                        rewardedLoading=false;
                        rewardedAd=null;
                        if(pendingRewardRequest){
                            pendingRewardRequest=false;
                            Runnable unavailable=pendingUnavailable;
                            pendingUnlock=null;
                            pendingUnavailable=null;
                            if(unavailable!=null)unavailable.run();
                        }
                    }
                }
        );
    }

    private void showLoadedRewarded(){
        if(rewardedAd==null||rewardShowing||activity.isFinishing())return;

        final RewardedAd ad=rewardedAd;
        rewardedAd=null;
        rewardShowing=true;
        pendingRewardRequest=false;
        final boolean[] earned={false};

        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){
                rewardShowing=false;
                if(!earned[0]){
                    Runnable unavailable=pendingUnavailable;
                    pendingUnlock=null;
                    pendingUnavailable=null;
                    if(unavailable!=null)unavailable.run();
                }
                loadRewarded();
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error){
                rewardShowing=false;
                Runnable unavailable=pendingUnavailable;
                pendingUnlock=null;
                pendingUnavailable=null;
                if(unavailable!=null)unavailable.run();
                loadRewarded();
            }
        });

        ad.show(activity,rewardItem -> {
            earned[0]=true;
            KeyKiiAccess.grant24Hours(activity);
            Runnable unlock=pendingUnlock;
            pendingUnlock=null;
            pendingUnavailable=null;
            if(unlock!=null)unlock.run();
        });
    }

    /**
     * Called only after a user completes a natural task (for example applying
     * a theme/font). Threshold is 5-8 completed tasks and at least 4 minutes.
     */
    void recordNaturalBreak(){
        if(KeyKiiAccess.isOwner(activity))return;
        if(activity.isFinishing()||rewardShowing)return;
        naturalBreaks++;

        long now=System.currentTimeMillis();
        long last=prefs.getLong(PREF_LAST_INTERSTITIAL,0L);
        if(naturalBreaks<nextInterstitialAt||now-last<INTERSTITIAL_MIN_INTERVAL)return;

        if(interstitialAd==null){
            loadInterstitial();
            return;
        }

        final InterstitialAd ad=interstitialAd;
        interstitialAd=null;
        naturalBreaks=0;
        nextInterstitialAt=5+(int)(Math.abs(System.nanoTime())%4L);
        prefs.edit().putLong(PREF_LAST_INTERSTITIAL,now).apply();

        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){
                loadInterstitial();
            }
            @Override public void onAdFailedToShowFullScreenContent(AdError error){
                loadInterstitial();
            }
        });
        ad.show(activity);
    }

    private void loadInterstitial(){
        if(KeyKiiAccess.isOwner(activity))return;
        if(interstitialLoading||interstitialAd!=null||activity.isFinishing())return;
        if(BuildConfig.ADMOB_INTERSTITIAL_ID==null||BuildConfig.ADMOB_INTERSTITIAL_ID.isEmpty())return;
        interstitialLoading=true;

        InterstitialAd.load(
                activity,
                BuildConfig.ADMOB_INTERSTITIAL_ID,
                new AdRequest.Builder().build(),
                new InterstitialAdLoadCallback(){
                    @Override public void onAdLoaded(InterstitialAd ad){
                        interstitialLoading=false;
                        interstitialAd=ad;
                    }
                    @Override public void onAdFailedToLoad(LoadAdError error){
                        interstitialLoading=false;
                        interstitialAd=null;
                    }
                }
        );
    }
}
