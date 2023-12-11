package vn.edu.hust.fileexamples

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import vn.edu.hust.fileexamples.databinding.ActivityMainBinding
import java.io.File

class MainActivity : AppCompatActivity() {
    var backgroundColor = Color.WHITE

    val MY_SETTINGS = "my_settings"
    val COLOR_PREF = "background_color"

    lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.buttonRed.setOnClickListener {
            backgroundColor = Color.RED
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonGreen.setOnClickListener {
            backgroundColor = Color.GREEN
            binding.root.setBackgroundColor(backgroundColor)
        }

        binding.buttonBlue.setOnClickListener {
            backgroundColor = Color.BLUE
            binding.root.setBackgroundColor(backgroundColor)
        }

        val prefs = getSharedPreferences(MY_SETTINGS, MODE_PRIVATE)
        backgroundColor = prefs.getInt(COLOR_PREF, Color.WHITE)
        binding.root.setBackgroundColor(backgroundColor)

        binding.buttonReadResource.setOnClickListener {
            val inputStream = resources.openRawResource(R.raw.my_resource)
            val content = inputStream.reader().readText()
            binding.textResource.text = content
            inputStream.close()
        }

        binding.buttonWriteInternal.setOnClickListener {
            val outputStream = openFileOutput("my_data.txt", MODE_PRIVATE)
            val writer = outputStream.writer()
            val content = binding.editTextInternal.text.toString()
            Log.v("TAG", content)
            writer.write(content)
            writer.flush()
            writer.close()
        }

        binding.buttonReadInternal.setOnClickListener {
            val inputStream = openFileInput("my_data.txt")
            val content = inputStream.reader().readText()
            inputStream.close()
            binding.editTextInternal.setText(content)
        }

        binding.buttonDelete.setOnClickListener {
            val file = File(filesDir.path + "/my_data.txt")
            if (file.exists())
                file.delete()
        }

        binding.buttonWriteExternal.setOnClickListener {
            val filePath = Environment.getExternalStorageDirectory().path + "/my_data.txt"
            val file = File(filePath)
            val outputStream = file.outputStream()
            val writer = outputStream.writer()
            val content = binding.editTextExternal.text.toString()
            Log.v("TAG", content)
            writer.write(content)
            writer.flush()
            writer.close()
        }

        binding.buttonReadExternal.setOnClickListener {
            val filePath = Environment.getExternalStorageDirectory().path + "/my_data.txt"
            val file = File(filePath)
            val inputStream = file.inputStream()
            val content = inputStream.reader().readText()
            inputStream.close()
            binding.editTextExternal.setText(content)
        }

        Log.v("TAG", Environment.getExternalStorageDirectory().path)
        if (Build.VERSION.SDK_INT < 30) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                Log.v("TAG", "Permission Denied => Request permission")
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1234)
            } else {
                Log.v("TAG", "Permission Granted")
            }
        } else {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                startActivity(intent)
            }
        }

        val root = Environment.getExternalStorageDirectory()
        val listFiles = root.listFiles()
        if (listFiles != null)
            for (item in listFiles) {
                if (item.isDirectory) {
                    Log.v("TAG", item.name + " - directory")
                } else if (item.isFile) {
                    Log.v("TAG", item.name + " - file")
                }
            }

        val dbPath = filesDir.path + "/mydb"
        db = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.CREATE_IF_NECESSARY)
        // createTable()

        // list all records
        val cs = db.rawQuery("select * from tblAMIGO where recID < 5", null)
        Log.v("TAG", "Num records: ${cs.count}")
        cs.moveToFirst()
        do {
            val recID = cs.getInt(0)
            val name = cs.getString(1)
            val phone = cs.getString(2)
            Log.v("TAG", "$recID - $name - $phone")
        }
        while (cs.moveToNext())
        cs.close()

        binding.buttonInsert.setOnClickListener {
            db.beginTransaction()
            try {
                val name = binding.editName.text.toString()
                val phone = binding.editPhone.text.toString()
                db.execSQL("insert into tblAMIGO(name, phone) values('$name', '$phone')")
                db.setTransactionSuccessful()
            } catch (ex: Exception) {
                ex.printStackTrace()
            } finally {
                db.endTransaction()
            }
        }
    }

    fun createTable() {
        db.beginTransaction()
        try {
            db.execSQL("create table tblAMIGO(" +
                    "recID integer primary key autoincrement," +
                    "name text," +
                    "phone text)")
            db.execSQL("insert into tblAMIGO(name, phone) values('AAA', '555-1111')")
            db.execSQL("insert into tblAMIGO(name, phone) values('BBB', '555-2222')")
            db.execSQL("insert into tblAMIGO(name, phone) values('CCC', '555-3333')")
            db.setTransactionSuccessful()
        } catch (ex: Exception) {
            ex.printStackTrace()
        } finally {
            db.endTransaction()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            Log.v("TAG", "Permission Denied")
        } else {
            Log.v("TAG", "Permission Granted")
        }
    }

    override fun onStop() {
        val prefs = getSharedPreferences(MY_SETTINGS, MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt(COLOR_PREF, backgroundColor)
        editor.apply()

        db.close()

        super.onStop()
    }
}