// IUserService.aidl
package top.jiecs.screener;

// Declare any non-default types here with import statements

interface IUserService {
    void exit() = 1;
    void destroy() = 16777114;
    int getUid() = 1000;

    boolean applyPeakRefreshRate(int frameRate) = 2;
}