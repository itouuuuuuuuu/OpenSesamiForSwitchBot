package com.itouuuuuuuuu.opensesami

import android.Manifest
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.amplifyframework.AmplifyException
import com.amplifyframework.auth.cognito.AWSCognitoAuthPlugin
import com.amplifyframework.core.Amplify
import com.amplifyframework.predictions.aws.AWSPredictionsPlugin
import com.amplifyframework.predictions.models.IdentifyActionType
import com.amplifyframework.predictions.result.IdentifyEntityMatchesResult
import com.itouuuuuuuuu.opensesami.api.SwitchBotApiService
import com.itouuuuuuuuu.opensesami.extentions.resize
import com.itouuuuuuuuu.opensesami.extentions.toBitmap
import com.itouuuuuuuuu.opensesami.model.SwitchBotPressRequest
import com.itouuuuuuuuu.opensesami.model.SwitchBotPressResponse
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
        private const val TAKE_PHOTO_INTERVAL = 2500L // ms
        private const val UNAUTHORIZED_THRESHOLD = 5 // ms
    }

    private lateinit var cameraExecutor: ExecutorService
    private var retryCount: Int = 0
    private var imageCapture: ImageCapture? = null
    private val handler = Handler()
    private var pressedSwitchBot = false

    private val switchBotApi by lazy { SwitchBotApiService().createService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Request camera permissions
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        retryCountTextView.text = getString(R.string.retry_count, 0)
        externalIdTextView.text = getString(R.string.external_image_id, null)
        confidenceTextView.text = getString(R.string.confidence, null)

        try {
            Amplify.addPlugin(AWSCognitoAuthPlugin())
            Amplify.addPlugin(AWSPredictionsPlugin())
            Amplify.configure(applicationContext)
            Log.i(TAG, "Initialized Amplify")
        } catch (error: AmplifyException) {
            Log.e(TAG, "Could not initialize Amplify", error)
        }
    }

    override fun onResume() {
        super.onResume()
        handler.postDelayed({ takePhoto() }, 100)  // すぐに開始すると画像取得できない
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera()
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        imageCapture.takePicture(cameraExecutor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                super.onCaptureSuccess(image)
                val bitmap = image.toBitmap().resize(0.1)

                try {
                    Amplify.Predictions.identify(IdentifyActionType.DETECT_ENTITIES, bitmap,
                        { result ->
                            val identifyResult = result as IdentifyEntityMatchesResult
                            val match = identifyResult.entityMatches.firstOrNull()
                            val confidence = match?.confidence
                            externalIdTextView.text = getString(R.string.external_image_id, match?.externalImageId)
                            confidenceTextView.text = getString(R.string.confidence, confidence?.toString())

                            confidence?.let {
                                if (!pressedSwitchBot && it > 80) {
                                    pressSwitchBot()
                                }
                            }
                        },
                        {
                            Log.e(TAG, "Identify failed", it)
                        }
                    )
                } catch (exception: Exception) {
                    Log.e(TAG, "Use case binding failed", exception)
                } finally {
                    image.close()

                    if (retryCount >= UNAUTHORIZED_THRESHOLD) {
                        unauthorized()
                        return
                    }

                    retryCountTextView.text = getString(R.string.retry_count, ++retryCount)
                    handler.postDelayed({ takePhoto() }, TAKE_PHOTO_INTERVAL)
                }
            }

            override fun onError(exception: ImageCaptureException) {
                Log.e(TAG, "Photo capture failed: ${exception.message}", exception)
            }

        })
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exception: Exception) {
                Log.e(TAG, "Use case binding failed", exception)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun pressSwitchBot() {
        pressedSwitchBot = true
        switchBotApi.press().enqueue(object : Callback<SwitchBotPressResponse> {
            override fun onResponse(call: Call<SwitchBotPressResponse>, response: Response<SwitchBotPressResponse>) {
            }

            override fun onFailure(call: Call<SwitchBotPressResponse>, t: Throwable) {
                pressedSwitchBot = false
            }
        })
    }

    private fun unauthorized() {
        finish()
    }
}