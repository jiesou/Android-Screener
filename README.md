# Android-Screener
A simple Shizuku tool for easily adjusting screen resolution and frame rate

> Still under development

> I am busy with my studies and have no time to update this app (for now). Contributions welcome

## Screenshots (Preview)

<img src="https://github.com/jiesou/Android-Screener/assets/84175239/9279a68e-8660-4119-b69a-31bb7b387c41" width="240px" alt="Screener Home Page">

## TODO

- [x] Get Shizuku permissions
- [x] Basic resolution changing function
- [x] Automatically undo if no operation within 10 seconds
- [x] Basic DPI density changing function
- [ ] Basic frame rate changing function
- [ ] Multi-user support
- [x] Automatically calculate dpi and resolution based on scaling
- [ ] Store display mode and apply it
- [ ] Edit display mode
- [ ] QS switching

## Notice

If you've broken your phone, you can use the `wm` command from your computer ADB to recover it.

```
adb shell wm size reset
adb shell wm density reset
```

## Download

The apk on the Releases page is updated manually and will not be synchronized with the latest updates, and the program is not yet complete enough to be “released”!

So it's best to download the apk from the [Actions](https://github.com/jiesou/Android-Screener/actions) page first, if the artifacts in Github Actions is expired and cannot be downloaded, you can *fork* this project and rebuild it.

