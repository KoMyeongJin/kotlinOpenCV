package com.example.opencvkotlin

import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.drawable.BitmapDrawable
import android.media.ExifInterface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.MotionEvent
import androidx.databinding.DataBindingUtil
import com.example.opencvkotlin.databinding.ActivityMainBinding
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.toast
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity() {


    private lateinit var binding: ActivityMainBinding
    private lateinit var mat: Mat
    private var prevDrawLineView: DrawLineView? = null
    private lateinit var drawLineView: DrawLineView
    private var fromX: Float = 0f
    private var fromY: Float = 0f
    private var duX: Float = 0f
    private var drawableWidth by Delegates.notNull<Int>()
    private var rectWidth by Delegates.notNull<Double>()
    private var drawViewWidth by Delegates.notNull<Int>()
    private var isDraw = true
    private val GET_GALLERY_IMAGE = 1
    private var tempFile: File? = null
    private var image: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var permissionListener = object : PermissionListener {
            override fun onPermissionGranted() {
                toast("Permission Granted")
            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                toast("Permission Denied\n$deniedPermissions")
            }

        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setPermissions(
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check()

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        image = (imgView.drawable as BitmapDrawable).bitmap

        mat = Mat()

        Utils.bitmapToMat(image, mat)


        drawableWidth = imgWidth(mat.nativeObjAddr)
        rectWidth = findRect(mat.nativeObjAddr)

        binding.zoom.setScrollEnabled(false)
        binding.zoom.setFlingEnabled(false)
        binding.zoom.setZoomEnabled(false)

        binding.drawView.setOnTouchListener { v, event ->
            onTouch(event)

            v.performClick()
        }
        binding.btnDraw.setOnClickListener {
            if (isDraw) {
                isDraw = false
                binding.drawView.setOnTouchListener(null)
                binding.zoom.setScrollEnabled(true)
                binding.zoom.setFlingEnabled(true)
                binding.zoom.setZoomEnabled(true)
            } else {
                isDraw = true
                binding.zoom.setScrollEnabled(false)
                binding.zoom.setFlingEnabled(false)
                binding.zoom.setZoomEnabled(false)
                binding.drawView.setOnTouchListener { v, event ->
                    onTouch(event)

                    v.performClick()
                }
            }
        }

        binding.btnLoad.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
            startActivityForResult(intent, GET_GALLERY_IMAGE)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        drawViewWidth = binding.drawView.width
        super.onWindowFocusChanged(hasFocus)
    }

    private external fun findRect(matAddrInput: Long): Double
    private external fun imgWidth(matAddrInput: Long): Int

    companion object {
        init {
            System.loadLibrary("native-lib")
        }
    }

    private fun onTouch(event: MotionEvent) {

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                fromX = event.x
                fromY = event.y

                Log.i("", "$fromX    $fromY")
                if (prevDrawLineView != null) {
                    binding.drawView.removeView(prevDrawLineView)
                }
            }

            MotionEvent.ACTION_MOVE -> {
                duX = event.x
                if (duX != 0f) {
                    if (prevDrawLineView != null) {
                        binding.drawView.removeView(prevDrawLineView)
                    }
                    drawLineView = DrawLineView(this, fromX, fromY, duX, fromY)
                    binding.drawView.addView(drawLineView, binding.drawView.layoutParams)
                    prevDrawLineView = drawLineView
                }
            }

            MotionEvent.ACTION_UP -> {
                duX = event.x
                var length =
                    (kotlin.math.abs(fromX - duX) * 2.5 / rectWidth * (drawableWidth.toFloat() / drawViewWidth) * 100).roundToInt() / 100.0
                tvResult.text = "$length CM"
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == GET_GALLERY_IMAGE && data != null) {
            var uri: Uri = data.data as Uri
            var cursor: Cursor? = null

            try {
                var proj = arrayOf(MediaStore.Images.Media.DATA)

                cursor = contentResolver.query(uri, proj, null, null, null)

                assert(cursor != null)
                var column_index = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)

                cursor!!.moveToFirst()

                tempFile = File(cursor.getString(column_index as Int))


            } finally {
                cursor?.close()
            }
            setImage()
        }
    }

    private fun setImage() {
        var options = BitmapFactory.Options()
        var originalBm = BitmapFactory.decodeFile(tempFile!!.absolutePath, options)
        var orientation = getOrientationOfImage(tempFile!!.absolutePath)

        var rotatedBitmap = getRotatedBitmap(originalBm, orientation)

        binding.imgView.setImageBitmap(rotatedBitmap)


        Utils.bitmapToMat(rotatedBitmap, mat)

        drawableWidth = imgWidth(mat.nativeObjAddr)
        rectWidth = findRect(mat.nativeObjAddr)


        //사각형 제대로 찾았는지 테스트용
        var bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bitmap)

        binding.imgView.setImageBitmap(bitmap)


    }

    private fun getOrientationOfImage(filePath: String): Int {
        var exif: ExifInterface? = null

        try {
            exif = ExifInterface(filePath)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        val orientation = exif!!.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )

        if (orientation != ExifInterface.ORIENTATION_UNDEFINED) {
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> return 90
                ExifInterface.ORIENTATION_ROTATE_180 -> return 180
                ExifInterface.ORIENTATION_ROTATE_270 -> return 270
            }
        }

        return 0
    }

    private fun getRotatedBitmap(bitmap: Bitmap, degrees: Int): Bitmap {

        if (degrees == 0) return bitmap

        val m = Matrix()

        m.setRotate(degrees.toFloat(), bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)

        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, m, true)

    }

}
