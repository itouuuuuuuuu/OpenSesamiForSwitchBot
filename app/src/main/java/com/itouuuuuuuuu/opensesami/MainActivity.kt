package com.itouuuuuuuuu.opensesami

import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.predictions.aws.AWSPredictionsPlugin
import com.amplifyframework.predictions.models.IdentifyActionType
import com.amplifyframework.predictions.result.IdentifyEntityMatchesResult

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSPredictionsPlugin())
            Amplify.configure(applicationContext)
            Log.i("MainActivity", "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e("MainActivity", "Could not initialize Amplify", error)
        }

        // 顔画像を取得
        val image = BitmapFactory.decodeResource(resources, R.drawable.woman)

        Amplify.Predictions.identify(IdentifyActionType.DETECT_ENTITIES, image,
                { result ->
                    val identifyResult = result as IdentifyEntityMatchesResult
                    val match = identifyResult.entityMatches.firstOrNull()
                    Log.i("AmplifyQuickstart", "${match?.externalImageId}")
                },
                { Log.e("AmplifyQuickstart", "Identify failed", it) }
        )
    }
}