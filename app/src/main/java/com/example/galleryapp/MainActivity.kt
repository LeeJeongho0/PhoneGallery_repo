package com.example.galleryapp

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.example.galleryapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 1. 인텐트를 통해서 갤러리앱에서 클릭을 하면 클릭된 이미지 Uri를 가져와서, content resolver를 이용해서 inputstream을 가져오고, BitmapFactory 를 통해서
        //    이미지뷰를 가져온다.
        val requestLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            val uri: Uri = it.data!!.data!!

            // 1.1 이미지를 가져오면 out of memory(OOM)가 발생할 수 있으므로, 화면에 출력될 원하는 사이즈 비율 설정
            val inSampleSize = calculateInSampleSize(
                uri,
                resources.getDimensionPixelSize(R.dimen.imgSize),
                resources.getDimensionPixelSize(R.dimen.imgSize)
            )
            // 1.2 비트맵 옵션설정 비율설정
            val option = BitmapFactory.Options()
            option.inSampleSize = inSampleSize
            try {
                // 1.3 contentResolver를 사용해서 uri를 통해 내가 원하는 정보를 가져온다. (uri -> inputStream)
                val inputStream = contentResolver.openInputStream(uri)
                // 1.4 inputStream으로 BitmapFactory를 이용해서 이미지를 가져온다. (out of memory를 방지하기 위해서, option 사이즈비율 저장함)
                var bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                // 1.5 이미지뷰에 비트맵을 저장
                bitmap?.let {
                    binding.ivPicture.setImageBitmap(bitmap)
                } ?: let {
                    Log.e("MainActivity", "bitmapFactory를 통해서 가져온 비트맵 null 발생")
                }
                inputStream?.close()
            } catch (e: java.lang.Exception) {
                Log.e("MainActivity", "${e.printStackTrace()}")
            }
        }

        // 2. 갤러리앱에 암시적 인텐트 방법으로 요청
        binding.btnCallImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestLauncher.launch(intent)
        }
    }

    // 이미지 비율을 계산하는 함수
    fun calculateInSampleSize(uri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val option = BitmapFactory.Options()
        // inJustDecodeBounds = true 이미지를 가져오지말고, 이미지의 정보만 줄 것을 요청
        option.inJustDecodeBounds = true

        try {
            // contentResolver로 이미지 정보를 다시 가져옴
            var inputStream = contentResolver.openInputStream(uri)
            // 진짜 inputStream을 통해서 비트맵을 가져오는 것이 아니라 , 비트맵 정보만 option에 저장해서 가져옴
            BitmapFactory.decodeStream(inputStream, null, option)
            inputStream?.close()
            inputStream = null
        } catch (e: java.lang.Exception) {
            Log.e("MainActivity", "calculateInSampleSize inputStream ${e.printStackTrace()}")
        }
        // 갤러리앱에 가져올 실제 이미지 사이즈
        val height = option.outHeight
        val width = option.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}