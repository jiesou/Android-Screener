// IUserService.aidl
package top.jiecs.screener;

// Declare any non-default types here with import statements

interface IUserService {
    void exit() = 1;
    /*
      refer to https://github.com/RikkaApps/Shizuku-API#userservice
    */
    void destroy() = 16777114;
    int getUid() = 1000;

    void setContentResolver(ContentResolver contentResolver);
    boolean applyPeakRefreshRate(int frameRate) = 2;
}