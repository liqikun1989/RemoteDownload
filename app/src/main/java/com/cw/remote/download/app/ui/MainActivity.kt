package com.cw.remote.download.app.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.os.storage.StorageVolume
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cw.remote.download.app.R
import com.cw.remote.download.app.base.BaseActivity
import com.cw.remote.download.app.base.NullViewModel
import com.cw.remote.download.app.data.FileListItem
import com.cw.remote.download.app.databinding.ActivityMainBinding
import com.cw.remote.download.app.databinding.ItemFileListBinding
import com.cw.remote.download.app.dialog.QRdialogFragment
import com.cw.remote.download.app.server.MainService
import com.dylanc.longan.doOnClick
import com.dylanc.longan.doOnLongClick
import com.dylanc.longan.isWifiConnected
import com.dylanc.longan.toast
import com.permissionx.guolindev.PermissionX
import java.io.File
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class MainActivity : BaseActivity<NullViewModel>() {
    override val layoutId: Int
        get() = R.layout.activity_main
    override val isBindingViewModel: () -> Unit
        get() = { }
    override val mViewModelClass: Class<NullViewModel>?
        get() = null
    private lateinit var adapter: RecyclerView.Adapter<FileListViewHolder>
    private val mFileList = mutableListOf<FileListItem>()
    private lateinit var mCurrentFileListItem: FileListItem
    private var mRootPath: String = ""
    private lateinit var mainBinding: ActivityMainBinding
    private val mLastLocationMap = mutableMapOf<String, Array<Int>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val permissionList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mutableListOf(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
        } else {
            mutableListOf(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        PermissionX.init(this)
            .permissions(permissionList)
            .onExplainRequestReason { scope, deniedList ->
                scope.showRequestReasonDialog(deniedList, "权限申请", "OK", "Cancel")
            }
            .request { allGranted, grantedList, deniedList ->
                if (!allGranted) {
                    toast("拒绝文件管理权限,程序无法正常使用")
                    mFileList.clear()
                    adapter.notifyDataSetChanged()
                }
            }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (mRootPath.isEmpty()) {
                    finish()
                    return
                }
                val file = File(mCurrentFileListItem.path)
                if (mRootPath == file.path) {
                    mRootPath = ""
                    mainBinding.tvMainCurrent.text = ""
                    mLastLocationMap.clear()
                    buildRootData()
                    adapter.notifyDataSetChanged()
                } else if (mRootPath !== file.path) {
                    file.parentFile?.apply {
                        mFileList.clear()
                        val listFiles = this.listFiles()
                        sortList(listFiles)
                        listFiles?.forEach {
                            val fileListItem = FileListItem(
                                getIcon(File(it.path)),
                                it.name,
                                it.path,
                                false,
                                it.isDirectory
                            )
                            mFileList.add(fileListItem)
                        }
                        mCurrentFileListItem = FileListItem(
                            R.mipmap.icon_folder,
                            this.name,
                            this.path,
                            false,
                            isDirectory = true
                        )
                        mainBinding.tvMainCurrent.text = this.path
                        adapter.notifyDataSetChanged()
                        val array = mLastLocationMap[this.path]
                        (mainBinding.rvFileList.layoutManager as LinearLayoutManager)
                            .scrollToPositionWithOffset(array?.get(1) ?: 0, array?.get(0) ?: 0)
                    }
                }
            }
        })

        startService(Intent(this, MainService::class.java))
    }

    override fun init(params: Bundle?) {
        mainBinding = mDataBinding as ActivityMainBinding
        mainBinding.rvFileList.layoutManager = LinearLayoutManager(this)
        adapter = object : RecyclerView.Adapter<FileListViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileListViewHolder {
                val inflate = DataBindingUtil.inflate<ItemFileListBinding>(
                    LayoutInflater.from(this@MainActivity),
                    R.layout.item_file_list, parent, false
                )
                return FileListViewHolder(inflate)
            }

            override fun getItemCount(): Int {
                return mFileList.size
            }

            override fun onBindViewHolder(holder: FileListViewHolder, position: Int) {
                val item = mFileList[position]
                holder.item.fileListItem = item
                holder.item.root.tag = item
                holder.item.executePendingBindings()
                if (item.isDirectory) {
                    holder.item.root.doOnClick(500) {
                        val tag = holder.item.root.tag as FileListItem
                        val tagFile = File(tag.path)
                        if (mRootPath.isEmpty()) {
                            mRootPath = tag.path
                        } else {
                            val layoutManager = mainBinding.rvFileList.layoutManager
                            //获取可视的第一个view
                            val topView = layoutManager?.getChildAt(0)
                            topView?.apply {
                                //获取与该view的顶部的偏移量
                                val lastOffset = top
                                //得到该View的数组位置
                                val lastPosition = layoutManager.getPosition(this)
                                //存储位置信息
                                mLastLocationMap[mCurrentFileListItem.path] = arrayOf(lastOffset, lastPosition)
                            }
                        }
                        mCurrentFileListItem = mFileList[mFileList.indexOf(tag)]
                        tagFile.apply {
                            mFileList.clear()
                            val listFiles = this.listFiles()
                            sortList(listFiles)
                            listFiles?.forEach {
                                val fileListItem = FileListItem(
                                    getIcon(File(it.path)),
                                    it.name,
                                    it.path,
                                    false,
                                    it.isDirectory
                                )
                                mFileList.add(fileListItem)
                            }
                            notifyDataSetChanged()
                            mainBinding.tvMainCurrent.text = mCurrentFileListItem.path
                        }
                    }
                } else {
                    holder.item.root.doOnLongClick {
                        if (isWifiConnected) {
                            val tag = holder.item.root.tag as FileListItem
                            QRdialogFragment.newInstance(tag.path).show(supportFragmentManager, "QRDialog")
                        } else {
                            toast(R.string.connect_wifi)
                        }
                    }
                }
            }
        }

        buildRootData()

        mainBinding.rvFileList.adapter = adapter
    }

    private fun buildRootData() {
        mFileList.clear()
        val externalFilesDirs = getExternalFilesDirs(null)
        val externalStorageDirectory = Environment.getExternalStorageDirectory()
        getStoragePath(this, true)
        getTFDir(this)
        externalFilesDirs.forEachIndexed { index, it ->
            val fileListItem = FileListItem(
                R.mipmap.icon_folder,
                getString(R.string.sd_card, index),
                it.parentFile?.parentFile?.parentFile?.parentFile?.path ?: "",
                isShowPath = true,
                isDirectory = true
            )
            mFileList.add(fileListItem)
        }
    }

    private fun sortList(listFiles: Array<File>?) {
        listFiles?.sortWith { o1, o2 ->
            if (o1.isDirectory && o2.isFile) {
                -1
            } else if (o1.isFile && o2.isDirectory) {
                1
            } else {
                o1.name.compareTo(o2.name)
            }
        }
    }

    fun getIcon(file: File): Int {
        if (file.isDirectory) {
            return R.mipmap.icon_folder
        }
        if (file.name.startsWith(".")) {
            return R.mipmap.icon_unknown_file
        }
        if (!file.name.contains(".")) {
            return R.mipmap.icon_unknown_file
        }
        val split = file.name.split(".")
        when (split[split.size - 1]) {
            "jpg", "jpeg", "png" -> return R.mipmap.icon_pictrue
            "txt" -> return R.mipmap.icon_txt_file
            "mp4", "flv" -> return R.mipmap.icon_video
        }
        return R.mipmap.icon_unknown_file
    }

    private fun getStoragePath(mContext: Context, is_removale: Boolean): String? {
        val mStorageManager: StorageManager = mContext.getSystemService(Context.STORAGE_SERVICE) as StorageManager

        try {
            val storageVolumeClazz = Class.forName("android.os.storage.StorageVolume")
            val getVolumeList: Method = mStorageManager.javaClass.getDeclaredMethod("getVolumeList")
            val getPath: Method = storageVolumeClazz.getDeclaredMethod("getPath")
            val isRemovable: Method = storageVolumeClazz.getDeclaredMethod("isRemovable")
            val storageVolumes = getVolumeList.invoke(mStorageManager) as Array<*>
            for (element in storageVolumes) {
                val storageVolumeElement: StorageVolume = element as StorageVolume
                val path = getPath.invoke(storageVolumeElement) as String
                val removable = isRemovable.invoke(storageVolumeElement) as Boolean
                if (is_removale == removable) {
                    return path
                }
            }
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        } catch (e: InvocationTargetException) {
            e.printStackTrace()
        } catch (e: NoSuchMethodException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }
        return null
    }

    fun getTFDir(context: Context): String? {
        var sdcardDir: String? = null
        val storageManager = context.applicationContext.getSystemService(STORAGE_SERVICE) as StorageManager
        var volumeInfoClazz: Class<*>? = null
        var diskInfoClazz: Class<*>? = null
        try {
            diskInfoClazz = Class.forName("android.os.storage.DiskInfo")
            val isSd = diskInfoClazz.getMethod("isSd")
            volumeInfoClazz = Class.forName("android.os.storage.VolumeInfo")
            val getType = volumeInfoClazz.getMethod("getType")
            val getDisk = volumeInfoClazz.getMethod("getDisk")
            val path: Field = volumeInfoClazz.getDeclaredField("path")
            val getVolumes = storageManager.javaClass.getMethod("getVolumes")
            val result = getVolumes.invoke(storageManager) as List<Class<*>>
            for (i in result.indices) {
                val volumeInfo: Any = result[i]
                if (getType.invoke(volumeInfo) as Int == 0) {
                    val disk = getDisk.invoke(volumeInfo)
                    if (disk != null) {
                        if (isSd.invoke(disk) as Boolean) {
                            sdcardDir = path.get(volumeInfo) as String
                            break
                        }
                    }
                }
            }
            return if (sdcardDir == null) {
                Log.w(TAG, "sdcardDir null")
                null
            } else {
                Log.i(TAG, "sdcardDir " + sdcardDir + File.separator)
                sdcardDir + File.separator
            }
        } catch (e: Exception) {
            Log.i(TAG, "sdcardDir e " + e.message)
            e.printStackTrace()
        }
        Log.w(TAG, "sdcardDir null")
        return null
    }

    class FileListViewHolder(val item: ItemFileListBinding) : RecyclerView.ViewHolder(item.root) {
    }
}