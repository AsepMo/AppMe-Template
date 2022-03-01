package com.appme.story.engine.app.utils;

import android.support.annotation.AnyThread;
import android.support.annotation.WorkerThread;
import android.support.annotation.Nullable;
import android.support.annotation.NonNull;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageStats;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Looper;
import android.os.RemoteException;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.text.TextUtils;
import android.view.inputmethod.InputMethodManager;

import com.appme.story.engine.app.folders.FileMe;
import com.appme.story.engine.app.folders.FolderMe;
import com.appme.story.engine.app.tasks.ShareTask;
import com.appme.story.engine.app.models.AppProcessInfo;
import com.appme.story.engine.app.models.ProcessInfo;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;


// TODO: Auto-generated Javadoc

/**

 */
public class AppUtil {
    public static final String TAG = AppUtil.class.getSimpleName();
    
    public static List<String[]> mProcessList = null;
    public static String getLabelName(Context context, PackageInfo packageInfo) {

        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            ApplicationInfo info = packageInfo.applicationInfo;
            String outputName = getSanitizedFileName(info.loadLabel(context.getPackageManager()).toString() + "_v" + packageInfo.versionName, false);
            return outputName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String getPackageVersionName(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            String versionName = info.versionName;
            return versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    public static int getPackageVersionCode(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            int versionCode = info.versionCode;
            return versionCode;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static String getVersion(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(),
                                                      0);
            String version = info.versionName;
            return version;
        } catch (Exception e) {
            e.printStackTrace();
            return "1.0";
        }
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
                                                      
            int versionCode = info.versionCode;
            return versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
    
    @AnyThread
    @Nullable
    public static String getSanitizedFileName(@NonNull String fileName, boolean replaceSpace) {
        if (fileName.equals(".") || fileName.equals("..")) {
            return null;
        }
        fileName = fileName.trim().replaceAll("[\\\\/:*?\"<>|]", "_");
        if (replaceSpace) {
            fileName = fileName.replaceAll("\\s", "_");
        }
        if (TextUtils.isEmpty(fileName)) {
            return null;
        }
        return fileName;
    }

    /**
     * Backup the given apk (both root and non root). This is similar to apk sharing feature except
     * that these are saved at /sdcard/AppManager/apks
     *
     * @return true on success, false on failure
     */
    @WorkerThread
    public static boolean backupApk(Context c, String packageName) {
        File backupPath = new File(FolderMe.getBackUpFolder().getAbsolutePath());
        if (!backupPath.exists()) {
            if (!backupPath.mkdirs()) return false;
        }
        // Fetch package info
        try {
            PackageManager manager = c.getPackageManager();
            PackageInfo info = manager.getPackageArchiveInfo(packageName, 0);
            
            String outputName = getSanitizedFileName(info.applicationInfo.loadLabel(manager).toString() + "_v" + info.versionName, false);
            if (outputName == null) outputName = packageName;
            FileOutputStream fileOutputStream = new FileOutputStream(new File(backupPath.getAbsolutePath(), outputName + ".apk"));
            FileInputStream fileInputStream = new FileInputStream(new File(packageName));
            try {
                byte[] arrby = new byte[4096];
                do {
                    int n2;
                    if ((n2 = fileInputStream.read(arrby)) == -1) {
                        fileInputStream.close();
                        fileOutputStream.close();
                        //android.widget.Toast.makeText(c, info.loadLabel(c.getPackageManager()).toString() + " " + "Berhasil Di Backup", android.widget.Toast.LENGTH_SHORT).show();                     
                        return true;
                    }
                    fileOutputStream.write(arrby, 0, n2);
                } while (true);
            }
            catch (Exception exception) {
                return false;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void deleteContentsOfDir(File dir) {
        if (dir == null || !dir.exists())
            return;
        File[] files = dir.listFiles();
        if (files != null && files.length > 0) {
            for (int i = 0; i < files.length; i++) {
                deleteDirIncludeSelf(files[i]);
            }
        }

    }


    public static void deleteDirIncludeSelf(File dir) {
        if (dir == null || !dir.exists()) return;
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (File file : files)
                if (file.isDirectory()) deleteDirIncludeSelf(file);
                else file.delete();
            dir.delete();
        } else dir.delete();
    }


    public static void closeInputStream(InputStream is) {
        if (is == null) {
            return;
        }
        try {
            is.close();
            is = null;
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public static void closeOutputStream(OutputStream os) {
        if (os == null) {
            return;
        }
        try {
            os.close();
            os = null;
        } catch (IOException e) {
            Log.i(TAG, e.getMessage());
        }
    }

    public static String md5s(String plainText) {
        String str = "";
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();

            int i;

            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < b.length; offset++) {
                i = b[offset];
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            str = buf.toString().substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();

        }
        return str;
    }
    
    /**
     * 描述：打开并安装文件.
     *
     * @param context the context
     * @param file    apk文件路径
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file),
                "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    /**
     * 描述：卸载程序.
     *
     * @param context     the context
     * @param packageName 包名
     */
    public static void uninstallApk(Context context, String packageName) {
        Intent intent = new Intent(Intent.ACTION_DELETE);
        Uri packageURI = Uri.parse("package:" + packageName);
        intent.setData(packageURI);
        context.startActivity(intent);
    }

    /**
     * 用来判断服务是否运行.
     *
     * @param context   the context
     * @param className 判断的服务名字 "com.xxx.xx..XXXService"
     * @return true 在运行 false 不在运行
     */
    public static boolean isServiceRunning(Context context, String className) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> servicesList = activityManager
                .getRunningServices(Integer.MAX_VALUE);
        Iterator<RunningServiceInfo> l = servicesList.iterator();
        while (l.hasNext()) {
            RunningServiceInfo si = (RunningServiceInfo) l.next();
            if (className.equals(si.service.getClassName())) {
                isRunning = true;
            }
        }
        return isRunning;
    }

    /**
     * 停止服务.
     *
     * @param context   the context
     * @param className the class name
     * @return true, if successful
     */
    public static boolean stopRunningService(Context context, String className) {
        Intent intent_service = null;
        boolean ret = false;
        try {
            intent_service = new Intent(context, Class.forName(className));
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (intent_service != null) {
            ret = context.stopService(intent_service);
        }
        return ret;
    }

    /**
     * Gets the number of cores available in this device, across all processors.
     * Requires: Ability to peruse the filesystem at "/sys/devices/system/cpu"
     *
     * @return The number of cores, or 1 if failed to get result
     */
    public static int getNumCores() {
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {

                @Override
                public boolean accept(File pathname) {
                    // Check if filename is "cpu", followed by a single digit
                    // number
                    if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                        return true;
                    }
                    return false;
                }

            });
            // Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    /**
     * 描述：判断网络是否有效.
     *
     * @param context the context
     * @return true, if is network available
     */
    public static boolean isNetworkAvailable(Context context) {
        try {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo info = connectivity.getActiveNetworkInfo();
                if (info != null && info.isConnected()) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    /**
     * Gps是否打开 需要<uses-permission
     * android:name="android.permission.ACCESS_FINE_LOCATION" />权限
     *
     * @param context the context
     * @return true, if is gps enabled
     */
    public static boolean isGpsEnabled(Context context) {
        LocationManager lm = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    /**
     * 判断当前网络是否是移动数据网络.
     *
     * @param context the context
     * @return boolean
     */
    public static boolean isMobile(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return true;
        }
        return false;
    }

    /**
     * 导入数据库.
     *
     * @param context the context
     * @param dbName  the db name
     * @param rawRes  the raw res
     * @return true, if successful
     */
    public static boolean importDatabase(Context context, String dbName,
                                         int rawRes) {
        int buffer_size = 1024;
        InputStream is = null;
        FileOutputStream fos = null;
        boolean flag = false;

        try {
            String dbPath = "/data/data/" + context.getPackageName()
                    + "/databases/" + dbName;
            File dbfile = new File(dbPath);
            // 判断数据库文件是否存在，若不存在则执行导入，否则直接打开数据库
            if (!dbfile.exists()) {
                // 欲导入的数据库
                if (!dbfile.getParentFile().exists()) {
                    dbfile.getParentFile().mkdirs();
                }
                dbfile.createNewFile();
                is = context.getResources().openRawResource(rawRes);
                fos = new FileOutputStream(dbfile);
                byte[] buffer = new byte[buffer_size];
                int count = 0;
                while ((count = is.read(buffer)) > 0) {
                    fos.write(buffer, 0, count);
                }
                fos.flush();
            }
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (Exception e) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (Exception e) {
                }
            }
        }
        return flag;
    }

    /**
     * 获取屏幕尺寸与密度.
     *
     * @param context the context
     * @return mDisplayMetrics
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        Resources mResources;
        if (context == null) {
            mResources = Resources.getSystem();

        } else {
            mResources = context.getResources();
        }
        // DisplayMetrics{density=1.5, width=480, height=854, scaledDensity=1.5,
        // xdpi=160.421, ydpi=159.497}
        // DisplayMetrics{density=2.0, width=720, height=1280,
        // scaledDensity=2.0, xdpi=160.42105, ydpi=160.15764}
        DisplayMetrics mDisplayMetrics = mResources.getDisplayMetrics();
        return mDisplayMetrics;
    }

    /**
     * 打开键盘.
     *
     * @param context the context
     */
    public static void showSoftInput(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(0,
                InputMethodManager.HIDE_NOT_ALWAYS);
    }

    /**
     * 关闭键盘事件.
     *
     * @param context the context
     */
    public static void closeSoftInput(Context context) {
        InputMethodManager inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        if (inputMethodManager != null
                && ((Activity) context).getCurrentFocus() != null) {
            inputMethodManager.hideSoftInputFromWindow(((Activity) context)
                            .getCurrentFocus().getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 获取包信息.
     *
     * @param context the context
     */
    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            String packageName = context.getPackageName();
            info = context.getPackageManager().getPackageInfo(packageName,
                    PackageManager.GET_ACTIVITIES);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return info;
    }

    public static String getPackage(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * 描述：获取运行的进程列表.
     *
     * @param context
     * @return
     */
    public static List<AppProcessInfo> getRunningAppProcesses(Context context) {
        ActivityManager activityManager = null;
        List<AppProcessInfo> list = null;
        PackageManager packageManager = null;
        try {
            activityManager = (ActivityManager) context
                    .getSystemService(Context.ACTIVITY_SERVICE);
            packageManager = context.getApplicationContext()
                    .getPackageManager();
            list = new ArrayList<AppProcessInfo>();
            // 所有运行的进程
            List<RunningAppProcessInfo> appProcessList = activityManager
                    .getRunningAppProcesses();
            ApplicationInfo appInfo = null;
            AppProcessInfo abAppProcessInfo = null;
            PackageInfo packageInfo = getPackageInfo(context);

            if (mProcessList != null) {
                mProcessList.clear();
            }
            mProcessList = getProcessRunningInfo();

            for (RunningAppProcessInfo appProcessInfo : appProcessList) {
                abAppProcessInfo = new AppProcessInfo(
                        appProcessInfo.processName, appProcessInfo.pid,
                        appProcessInfo.uid);
                appInfo = getApplicationInfo(context,
                        appProcessInfo.processName);
                // appInfo.flags;

                if (appInfo != null) {

                    if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
                        abAppProcessInfo.isSystem = true;
                    } else {
                        abAppProcessInfo.isSystem = false;
                    }
                    Drawable icon = appInfo.loadIcon(packageManager);
                    String appName = appInfo.loadLabel(packageManager)
                            .toString();
                    abAppProcessInfo.icon = icon;
                    abAppProcessInfo.appName = appName;
                } else {
                    // :服务的命名
                    if (appProcessInfo.processName.indexOf(":") != -1) {
                        appInfo = getApplicationInfo(context,
                                appProcessInfo.processName.split(":")[0]);
                        Drawable icon = appInfo.loadIcon(packageManager);
                        abAppProcessInfo.icon = icon;
                    }
                    abAppProcessInfo.isSystem = true;
                    abAppProcessInfo.appName = appProcessInfo.processName;
                }

                /*
                 * AbPsRow psRow = getPsRow(appProcessInfo.processName);
                 * if(psRow!=null){ abAppProcessInfo.memory = psRow.mem; }
                 */

                ProcessInfo processInfo = getMemInfo(appProcessInfo.processName);
                abAppProcessInfo.memory = processInfo.memory;
                abAppProcessInfo.cpu = processInfo.cpu;
                abAppProcessInfo.status = processInfo.status;
                abAppProcessInfo.threadsCount = processInfo.threadsCount;
                list.add(abAppProcessInfo);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * 描述：根据进程名返回应用程序.
     *
     * @param context
     * @param processName
     * @return
     */
    public static ApplicationInfo getApplicationInfo(Context context,
                                                     String processName) {
        if (processName == null) {
            return null;
        }

        PackageManager packageManager = context.getApplicationContext()
                .getPackageManager();
        List<ApplicationInfo> appList = packageManager
                .getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        for (ApplicationInfo appInfo : appList) {
            if (processName.equals(appInfo.processName)) {
                return appInfo;
            }
        }
        return null;
    }

    /**
     * 描述：kill进程.
     *
     * @param context
     * @param pid
     */
    public static void killProcesses(Context context, int pid,
                                     String processName) {

        String cmd = "kill -9 " + pid;
        String Command = "am force-stop " + processName + "\n";
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(sh.getOutputStream());
            os.writeBytes(Command + "\n");
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            sh.waitFor();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // AbLogUtil.d(AbAppUtil.class, "#kill -9 "+pid);
        Log.i(TAG,processName);
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = null;
        try {
            if (processName.indexOf(":") == -1) {
                packageName = processName;
            } else {
                packageName = processName.split(":")[0];
            }

            activityManager.killBackgroundProcesses(packageName);

            //
            Method forceStopPackage = activityManager.getClass()
                    .getDeclaredMethod("forceStopPackage", String.class);
            forceStopPackage.setAccessible(true);
            forceStopPackage.invoke(activityManager, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // /**
    // *
    // * 描述：执行PS.
    // */
    // public static List<AbPsRow> ps() {
    // List<AbPsRow> psRowlist = new ArrayList<AbPsRow>();
    // String ps = runScript("ps");
    // String[] lines = ps.split("\n");
    // psRowlist = new ArrayList<AbPsRow>();
    // for (String line : lines) {
    // AbPsRow row = new AbPsRow(line);
    // if (row.pid != null) psRowlist.add(row);
    // }
    // return psRowlist;
    // }
    //
    // /**
    // *
    // * 描述：获得这个进程的ps信息.
    // * @param processName
    // * @return
    // */
    // public static AbPsRow getPsRow(String processName) {
    // List<AbPsRow> psRowlist = ps();
    // for (AbPsRow row : psRowlist) {
    // if (processName.equals(row.cmd)) {
    // return row;
    // }
    // }
    // return null;
    // }

    /**
     * 描述：根据进程名获取CPU和内存信息.
     *
     * @param processName
     * @return
     */
    public static ProcessInfo getMemInfo(String processName) {
        ProcessInfo process = new ProcessInfo();
        if (mProcessList == null) {
            mProcessList = getProcessRunningInfo();
        }
        String processNameTemp = "";

        for (Iterator<String[]> iterator = mProcessList.iterator(); iterator
                .hasNext(); ) {
            String[] item = (String[]) iterator.next();
            processNameTemp = item[9];
            // AbLogUtil.d(AbAppUtil.class,
            // "##"+item[9]+",NAME:"+processNameTemp);
            if (processNameTemp != null && processNameTemp.equals(processName)) {
                // AbLogUtil.d(AbAppUtil.class,
                // "##"+item[9]+","+process.memory);
                // Process ID
                process.pid = Integer.parseInt(item[0]);
                // CPU
                process.cpu = item[2];
                // S
                process.status = item[3];
                // thread
                process.threadsCount = item[4];
                // Mem
                long mem = 0;
                if (item[6].indexOf("M") != -1) {
                    mem = Long.parseLong(item[6].replace("M", "")) * 1000 * 1024;
                } else if (item[6].indexOf("K") != -1) {
                    mem = Long.parseLong(item[6].replace("K", "")) * 1000;
                } else if (item[6].indexOf("G") != -1) {
                    mem = Long.parseLong(item[6].replace("G", "")) * 1000 * 1024 * 1024;
                }
                process.memory = mem;
                // UID
                process.uid = item[8];
                // Process Name
                process.processName = item[9];
                break;
            }
        }
        if (process.memory == 0) {
            Log.d(TAG, "##" + processName + ",top -n 1未找到");
        }
        return process;
    }

    /**
     * 描述：根据进程ID获取CPU和内存信息.
     *
     * @param pid
     * @return
     */
    public static ProcessInfo getMemInfo(int pid) {
        ProcessInfo process = new ProcessInfo();
        if (mProcessList == null) {
            mProcessList = getProcessRunningInfo();
        }
        String tempPidString = "";
        int tempPid = 0;
        int count = mProcessList.size();
        for (int i = 0; i < count; i++) {
            String[] item = mProcessList.get(i);
            tempPidString = item[0];
            if (tempPidString == null) {
                continue;
            }
            // AbLogUtil.d(AbAppUtil.class, "##"+item[9]+",PID:"+tempPid);
            tempPid = Integer.parseInt(tempPidString);
            if (tempPid == pid) {
                // AbLogUtil.d(AbAppUtil.class,
                // "##"+item[9]+","+process.memory);
                // Process ID
                process.pid = Integer.parseInt(item[0]);
                // CPU
                process.cpu = item[2];
                // S
                process.status = item[3];
                // thread
                process.threadsCount = item[4];
                // Mem
                long mem = 0;
                if (item[6].indexOf("M") != -1) {
                    mem = Long.parseLong(item[6].replace("M", "")) * 1000 * 1024;
                } else if (item[6].indexOf("K") != -1) {
                    mem = Long.parseLong(item[6].replace("K", "")) * 1000;
                } else if (item[6].indexOf("G") != -1) {
                    mem = Long.parseLong(item[6].replace("G", "")) * 1000 * 1024 * 1024;
                }
                process.memory = mem;
                // UID
                process.uid = item[8];
                // Process Name
                process.processName = item[9];
                break;
            }
        }
        return process;
    }

    /**
     * 描述：执行命令.
     *
     * @param command
     * @param workdirectory
     * @return
     */
    public static String runCommand(String[] command, String workdirectory) {
        String result = "";
        Log.d(TAG, "#" + command);
        try {
            ProcessBuilder builder = new ProcessBuilder(command);
            // set working directory
            if (workdirectory != null) {
                builder.directory(new File(workdirectory));
            }
            builder.redirectErrorStream(true);
            Process process = builder.start();
            InputStream in = process.getInputStream();
            byte[] buffer = new byte[1024];
            while (in.read(buffer) != -1) {
                String str = new String(buffer);
                result = result + str;
            }
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 描述：运行脚本.
     *
     * @param script
     * @return
     */
    public static String runScript(String script) {
        String sRet = "";
        try {
            final Process m_process = Runtime.getRuntime().exec(script);
            final StringBuilder sbread = new StringBuilder();
            Thread tout = new Thread(new Runnable() {
                public void run() {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(m_process.getInputStream()),
                            8192);
                    String ls_1 = null;
                    try {
                        while ((ls_1 = bufferedReader.readLine()) != null) {
                            sbread.append(ls_1).append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            tout.start();

            final StringBuilder sberr = new StringBuilder();
            Thread terr = new Thread(new Runnable() {
                public void run() {
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(m_process.getErrorStream()),
                            8192);
                    String ls_1 = null;
                    try {
                        while ((ls_1 = bufferedReader.readLine()) != null) {
                            sberr.append(ls_1).append("\n");
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            bufferedReader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            terr.start();

            int retvalue = m_process.waitFor();
            while (tout.isAlive()) {
                Thread.sleep(50);
            }
            if (terr.isAlive())
                terr.interrupt();
            String stdout = sbread.toString();
            String stderr = sberr.toString();
            sRet = stdout + stderr;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return sRet;
    }

    /**
     * 应用程序运行命令获取 Root权限，设备必须已破解(获得ROOT权限)
     *
     * @return 应用程序是/否获取Root权限
     */
    public static boolean getRootPermission(Context context) {
        String packageCodePath = context.getPackageCodePath();
        Process process = null;
        DataOutputStream os = null;
        try {
            String cmd = "chmod 777 " + packageCodePath;
            // 切换到root帐号
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(cmd + "\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            return false;
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    /**
     * 描述：获取进程运行的信息.
     *
     * @return
     */
    public static List<String[]> getProcessRunningInfo() {
        List<String[]> processList = null;
        try {
            String result = runCommandTopN1();
            processList = parseProcessRunningInfo(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return processList;
    }

    /**
     * 描述：top -n 1.
     *
     * @return
     */
    public static String runCommandTopN1() {
        String result = null;
        try {
            String[] args = {"/system/bin/top", "-n", "1"};
            result = runCommand(args, "/system/bin/");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    // /**
    // *
    // * 描述：获取进程运行的信息.
    // * @return
    // */
    // public static AbCPUInfo getCPUInfo() {
    // AbCPUInfo CPUInfo = null;
    // try {
    // String result = runCommandTopN1();
    // CPUInfo = parseCPUInfo(result);
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return CPUInfo;
    // }

    /**
     * 描述：解析数据.
     *
     * @param info User 39%, System 17%, IOW 3%, IRQ 0% PID PR CPU% S #THR VSS
     *             RSS PCY UID Name 31587 0 39% S 14 542288K 42272K fg u0_a162
     *             cn.amsoft.process 313 1 17% S 12 68620K 11328K fg system
     *             /system/bin/surfaceflinger 32076 1 2% R 1 1304K 604K bg
     *             u0_a162 /system/bin/top
     * @return
     */
    public static List<String[]> parseProcessRunningInfo(String info) {
        List<String[]> processList = new ArrayList<String[]>();
        int Length_ProcStat = 10;
        String tempString = "";
        boolean bIsProcInfo = false;
        String[] rows = null;
        String[] columns = null;
        rows = info.split("[\n]+");
        // 使用正则表达式分割字符串
        for (int i = 0; i < rows.length; i++) {
            tempString = rows[i];
            // AbLogUtil.d(AbAppUtil.class, tempString);
            if (tempString.indexOf("PID") == -1) {
                if (bIsProcInfo == true) {
                    tempString = tempString.trim();
                    columns = tempString.split("[ ]+");
                    if (columns.length == Length_ProcStat) {
                        // 把/system/bin/的去掉
                        if (columns[9].startsWith("/system/bin/")) {
                            continue;
                        }
                        // AbLogUtil.d(AbAppUtil.class,
                        // "#"+columns[9]+",PID:"+columns[0]);
                        processList.add(columns);
                    }
                }
            } else {
                bIsProcInfo = true;
            }
        }
        return processList;
    }

    /**
     *
     * 描述：解析数据.
     *
     * @param info
     *            User 39%, System 17%, IOW 3%, IRQ 0%
     * @return
     */
    // public static AbCPUInfo parseCPUInfo(String info) {
    // AbCPUInfo CPUInfo = new AbCPUInfo();
    // String tempString = "";
    // String[] rows = null;
    // String[] columns = null;
    // rows = info.split("[\n]+");
    // // 使用正则表达式分割字符串
    // for (int i = 0; i < rows.length; i++) {
    // tempString = rows[i];
    // //AbLogUtil.d(AbAppUtil.class, tempString);
    // if (tempString.indexOf("User") != -1 && tempString.indexOf("System") !=
    // -1) {
    // tempString = tempString.trim();
    // columns = tempString.split(",");
    // for(int j = 0; j < columns.length; j++){
    // String col = columns[j].trim();
    // String[] cpu = col.split(" ");
    // if(j == 0){
    // CPUInfo.User = cpu[1];
    // }else if(j == 1){
    // CPUInfo.System = cpu[1];
    // }else if(j == 2){
    // CPUInfo.IOW = cpu[1];
    // }else if(j == 3){
    // CPUInfo.IRQ = cpu[1];
    // }
    // }
    // }
    // }
    // return CPUInfo;
    // }

    /**
     * 描述：获取可用内存.
     *
     * @param context
     * @return
     */
    public static long getAvailMemory(Context context) {
        // 获取android当前可用内存大小
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        MemoryInfo memoryInfo = new MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        // 当前系统可用内存 ,将获得的内存大小规格化

        return memoryInfo.availMem;
    }

    /**
     * 描述：总内存.
     *
     * @param context
     * @return
     */
    public static long getTotalMemory(Context context) {
        // 系统内存信息文件
        String file = "/proc/meminfo";
        String memInfo;
        String[] strs;
        long memory = 0;

        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader, 8192);
            // 读取meminfo第一行，系统内存大小
            memInfo = bufferedReader.readLine();
            strs = memInfo.split("\\s+");
            for (String str : strs) {
                Log.d(TAG, str + "\t");
            }
            // 获得系统总内存，单位KB
            memory = Integer.valueOf(strs[1]).intValue() ;
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Byte转位KB或MB
        return memory*1024;
    }

    public static long getPkgSize(final Context context, String pkgName) throws NoSuchMethodException,
    InvocationTargetException,
    IllegalAccessException {
        long pkgSize=0;
        // getPackageSizeInfo是PackageManager中的一个private方法，所以需要通过反射的机制来调用
        Method method = PackageManager.class.getMethod("getPackageSizeInfo",
                                                       new Class[]{String.class, IPackageStatsObserver.class});
        // 调用 getPackageSizeInfo 方法，需要两个参数：1、需要检测的应用包名；2、回调
        method.invoke(context.getPackageManager(), new Object[]{
                          pkgName,
                          new IPackageStatsObserver.Stub() {
                              @Override
                              public void onGetStatsCompleted(PackageStats pStats, boolean succeeded) throws RemoteException {
                                  // 子线程中默认无法处理消息循环，自然也就不能显示Toast，所以需要手动Looper一下
                                  Looper.prepare();
                                  // 从pStats中提取各个所需数据


                                  //  pkgSize= pStats.cacheSize+pStats.dataSize+pStats.codeSize;
//                        Toast.makeText(context,
//                                "缓存大小=" + Formatter.formatFileSize(context, pStats.cacheSize) +
//                                        "\n数据大小=" + Formatter.formatFileSize(context, pStats.dataSize) +
//                                        "\n程序大小=" + Formatter.formatFileSize(context, pStats.codeSize),
//                                Toast.LENGTH_LONG).show();
                                  // 遍历一次消息队列，弹出Toast
                                  Looper.loop();
                              }
                          }
                      });

        return pkgSize;
    }

    public static Intent createFileOpenIntent(File file)
    {
        Intent intent = new Intent(Intent.ACTION_VIEW);     
        intent.setDataAndType(Uri.fromFile(file), FileMe.getFileMimeType(file));
        return intent;
    }

    public static void shareFiles(Activity c, ArrayList<File> a, int fab_skin) {

        ArrayList<Uri> uris = new ArrayList<>();
        boolean b = true;
        for (File f : a) {
            uris.add(Uri.fromFile(f));
        }

        String mime = FileMe.getFileMimeType(a.get(0));
        if (a.size() > 1)
            for (File f : a) {
                if (!mime.equals(FileMe.getFileMimeType(f))) {
                    b = false;
                }
            }

        if (!b || mime==(null))
            mime = "*/*";
        try {

            new ShareTask(c, uris, fab_skin).execute(mime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void launchBrowser(Activity from, String url) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        Uri content_url = Uri.parse(url);
        intent.setData(content_url);
        from.startActivity(intent);
    }

    public static boolean isIntentSafe(Activity activity, Uri uri) {
        Intent mapCall = new Intent(Intent.ACTION_VIEW, uri);
        PackageManager packageManager = activity.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(mapCall, 0);
        return activities.size() > 0;
    }

    public static float dp2px(Resources resources, float dp) {
        final float scale = resources.getDisplayMetrics().density;
        return  dp * scale + 0.5f;
    }

    public static float sp2px(Resources resources, float sp){
        final float scale = resources.getDisplayMetrics().scaledDensity;
        return sp * scale;
    }
}

