package paymentcloud.creaj.sv.com.asignartag;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieComposition;
import com.airbnb.lottie.OnCompositionLoadedListener;

public class Lottie extends AppCompatActivity {
    LottieAnimationView mLottieAnimationView;
    String[] mAnimFiles = new String[]{"empty_status.json", "letter_b_monster.json", "permission.json", "ice_cream_animation.json"};
    int mCurrentAnim = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.paymentlottie);
        mLottieAnimationView = findViewById(R.id.lottie_animation_view);

        LottieComposition.Factory.fromAssetFileName(this, "recharge.json", new OnCompositionLoadedListener() {
            @Override
            public void onCompositionLoaded(@Nullable LottieComposition composition) {
                mLottieAnimationView.setComposition(composition);
                mLottieAnimationView.playAnimation();
            }
        });


        new Handler().postDelayed(new running(), 4000);
    }

    class running implements Runnable {
        running() {
        }

        public void run() {
            Intent i =  new Intent(getApplicationContext(), MainActivity.class);
            startActivity(i);
        }

    }
    @Override
    protected void onDestroy() {
        mLottieAnimationView.cancelAnimation();
        mLottieAnimationView = null;
        super.onDestroy();
    }
}

