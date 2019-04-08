package com.dev.pedscn.trackyourspot

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_add_spot.*
import android.net.Uri
import android.support.v7.widget.Toolbar
import android.widget.Button
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import java.io.File

class AddSpot : AppCompatActivity(){

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private lateinit var selectedBodySide : String
    private lateinit var selectedBodyPart : String
    private lateinit var fullPhotoPath : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_spot)
        setSupportActionBar(add_spot_screen_toolbar as Toolbar)
        title = "Spot Preview"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        fullPhotoPath = intent.getStringExtra("fullPhotoPath")
        selectedBodySide = intent.getStringExtra("selectedBodySide")
        selectedBodyPart = intent.getStringExtra("selectedBodyPart")
        val spotImageName = intent.getStringExtra("spotImageName")

        Glide.with(this@AddSpot)
                .load(File(fullPhotoPath))
                .apply(RequestOptions().fitCenter())
                .into(spot_image)
        val editTextWidget = spot_name_widget
        val editText = spot_name_edittext
        editTextWidget.setHintTextAppearance(R.style.CustomHintEnabled)
        editTextWidget.isErrorEnabled = true

        val btnConfirmSpot = findViewById<Button>(R.id.btn_confirm_spot) //SDK version not compatible with inference.
        btnConfirmSpot.setOnClickListener {
            val editName = editText.text.toString()
            if (editName.isBlank()) {
                editTextWidget.error = "Name cannot be blank"
            }
            else if (editName.length>19) {
                editTextWidget.error = "Name must be under 20 characters"
            }
            else if (!editName.matches(Regex(pattern = "^[a-zA-Z0-9 ]+\$"))) {
                editTextWidget.error = "Only letters and numbers allowed"
            }
            else {
                val processedEditName = editName.replace(" ", "-")
                moveImageFile(spotImageName, processedEditName, selectedBodySide, selectedBodyPart)
                val intent = Intent(this, OldSpotScreen::class.java)
                intent.putExtra("selectedBodyPart", selectedBodyPart)
                intent.putExtra("selectedBodySide", selectedBodySide)
                startActivity(intent)
            }
        }

        val btnCancel = findViewById<Button>(R.id.btn_cancel) //SDK version not compatible with inference.
        btnCancel.setOnClickListener {
            onBackPressed()
        }
    }

    private fun deleteRecursive(fileOrDirectory: File) {
        if (fileOrDirectory.isDirectory)
            for (child in fileOrDirectory.listFiles()!!)
                deleteRecursive(child)
        fileOrDirectory.delete()
    }

    //Is the moveImageFile method even needed???? Apart from renaming //Is it dangerous?
    private fun moveImageFile(spotImageName: String, newSpotName: String, selectedBodySide: String, selectedBodyPart: String) { //Need better way of dealing with temps
        val photoDirectory = fullPhotoPath.removeSuffix(spotImageName)
        //Move image from temp folder to a new *newSpotName* folder
        val newDirPath = photoDirectory.replace("temp", "$selectedBodySide/$selectedBodyPart/$newSpotName")
        val newDir = File(newDirPath)
        //Check if the directory exists already, create it otherwise
        if(!newDir.exists()) newDir.mkdirs()
        //Make app available in the device's Gallery
        Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE).also { mediaScanIntent ->
            val f = File(fullPhotoPath)
            mediaScanIntent.data = Uri.fromFile(f)
            sendBroadcast(mediaScanIntent)
            //Rename the file to the new path
            f.renameTo(File(newDirPath + spotImageName))
            //Delete the temporary folder
            val tempFolder = File(photoDirectory)
            deleteRecursive(tempFolder)
        }
    }

    override fun onBackPressed() {
        deletePicAndClose()
    }

    private fun deletePicAndClose() {
        File(fullPhotoPath).delete()
        val intent = Intent(this, OldSpotScreen::class.java)
        intent.putExtra("selectedBodyPart", selectedBodyPart)
        intent.putExtra("selectedBodySide", selectedBodySide)
        startActivity(intent)
    }
}