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
    private boolean consentCheckCompleted=false;
    private boolean rewardedLoading=false;
    private boolean interstitialLoading=false;
    private boolean rewardShowing=false;
    private boolean pendingRewardRequest=false;
    private boolean pendingGrant24HourPass=true;

    private Runnable pendingUnlock;
    private Runnable pendingUnavailable;

    private int naturalBreaks=0;
    private int nextInterstitialAt=5+(int)(Math.abs(System.nanoTime())%4L);

    KeyKiiAds(Activity activity,SharedPreferences prefs){
        this.activity=activity;
        this.prefs=prefs;
    }

    void start(){
        if(activity.isFinishing())return;
        if(isTestMode()){
            maybeInitializeAds();
        }
        try{
            consentInformation=UserMessagingPlatform.getConsentInformation(activity);
            ConsentRequestParameters params=
                    new ConsentRequestParameters.Builder().build();

            consentInformation.requestConsentInfoUpdate(
                    activity,
                    params,
                    () -> UserMessagingPlatform.loadAndShowConsentFormIfRequired(
                            activity,
                            formError -> {
                                consentCheckCompleted=true;
                                maybeInitializeAds();
                            }
                    ),
                    formError -> {
                        consentCheckCompleted=true;
                        maybeInitializeAds();
                    }
            );

            if(consentInformation.canRequestAds())maybeInitializeAds();
        }catch(Throwable ignored){
            consentCheckCompleted=true;
            maybeInitializeAds();
        }
    }

    private synchronized void maybeInitializeAds(){
        if(activity.isFinishing())return;
        if(!isTestMode()&&consentInformation!=null&&!consentInformation.canRequestAds()&&!consentCheckCompleted){
            return;
        }
        if(initialized){
            if(rewardedAd==null&&!rewardedLoading){
                activity.runOnUiThread(this::loadRewarded);
            }
            return;
        }
        initialized=true;

        MobileAds.initialize(
                activity,
                initializationStatus -> activity.runOnUiThread(() -> {
                    loadRewarded();
                    loadInterstitial();
                })
        );
    }

    boolean isTestMode(){
        return BuildConfig.ADMOB_TEST_MODE;
    }

    boolean isRewardedReady(){
        return rewardedAd!=null;
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
        requestRewardedAd(onUnlocked,onUnavailable,true);
    }

    /**
     * Loads and shows an AdMob RewardedAd for applying any free or ad-gated theme.
     * Only fires {@code onRewardEarned} when {@code onUserEarnedReward} runs;
     * early dismissal or ad load/show failure runs {@code onNotEarned}.
     */
    void showRewardedForFreeTheme(Runnable onRewardEarned,Runnable onNotEarned){
        requestRewardedAd(onRewardEarned,onNotEarned,false);
    }

    /**
     * Loads and shows an AdMob RewardedAd before applying a theme pack.
     * When {@code grant24HourPass} is true, earning the reward also grants 24h PRO access.
     */
    void showRewardedForThemeApply(
            boolean grant24HourPass,
            Runnable onRewardEarned,
            Runnable onNotEarned
    ){
        requestRewardedAd(onRewardEarned,onNotEarned,grant24HourPass);
    }

    private void requestRewardedAd(
            Runnable onRewardEarned,
            Runnable onNotEarned,
            boolean grant24HourPass
    ){
        if(activity.isFinishing()||rewardShowing)return;

        pendingUnlock=onRewardEarned;
        pendingUnavailable=onNotEarned;
        pendingGrant24HourPass=grant24HourPass;
        pendingRewardRequest=true;

        if(!initialized){
            maybeInitializeAds();
            return;
        }

        if(rewardedAd!=null){
            showLoadedRewarded();
        }else{
            loadRewarded();
        }
    }

    private void notifyPendingRewardUnavailable(){
        pendingRewardRequest=false;
        final Runnable unavailable=pendingUnavailable;
        pendingUnlock=null;
        pendingUnavailable=null;
        if(unavailable!=null){
            activity.runOnUiThread(() -> {
                if(!activity.isFinishing())unavailable.run();
            });
        }
    }

    private void loadRewarded(){
        if(rewardedLoading||rewardedAd!=null||activity.isFinishing())return;
        if(BuildConfig.ADMOB_REWARDED_ID==null||BuildConfig.ADMOB_REWARDED_ID.isEmpty()){
            if(pendingRewardRequest)notifyPendingRewardUnavailable();
            return;
        }
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
                        if(pendingRewardRequest)notifyPendingRewardUnavailable();
                    }
                }
        );
    }

    private void showLoadedRewarded(){
        if(rewardedAd==null||rewardShowing||activity.isFinishing())return;

        final RewardedAd ad=rewardedAd;
        final boolean grantPass=pendingGrant24HourPass;
        rewardedAd=null;
        rewardShowing=true;
        pendingRewardRequest=false;
        final boolean[] earned={false};

        ad.setFullScreenContentCallback(new FullScreenContentCallback(){
            @Override public void onAdDismissedFullScreenContent(){
                rewardShowing=false;
                if(!earned[0]){
                    final Runnable unavailable=pendingUnavailable;
                    pendingUnlock=null;
                    pendingUnavailable=null;
                    if(unavailable!=null){
                        activity.runOnUiThread(() -> {
                            if(!activity.isFinishing())unavailable.run();
                        });
                    }
                }
                loadRewarded();
            }

            @Override public void onAdFailedToShowFullScreenContent(AdError error){
                rewardShowing=false;
                if(!earned[0]){
                    final Runnable unavailable=pendingUnavailable;
                    pendingUnlock=null;
                    pendingUnavailable=null;
                    if(unavailable!=null){
                        activity.runOnUiThread(() -> {
                            if(!activity.isFinishing())unavailable.run();
                        });
                    }
                }
                loadRewarded();
            }
        });

        ad.show(activity,rewardItem -> {
            earned[0]=true;
            if(grantPass)KeyKiiAccess.grant24Hours(activity);
            final Runnable unlock=pendingUnlock;
            pendingUnlock=null;
            pendingUnavailable=null;
            if(unlock!=null){
                activity.runOnUiThread(() -> {
                    if(!activity.isFinishing())unlock.run();
                });
            }
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
