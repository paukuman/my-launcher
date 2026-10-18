# MyLauncher

Launcher Android sederhana: jam & tanggal, pencarian app, grid 4 kolom,
tekan lama untuk Info/Copot. Wallpaper sistem tampil di belakang.

## Build lewat Termux + gh

    pkg update && pkg install git gh unzip
    termux-setup-storage
    unzip ~/storage/downloads/MyLauncher.zip -d ~
    cd ~/MyLauncher
    gh auth login
    git init -b main
    git add .
    git commit -m "init"
    gh repo create my-launcher --private --source=. --push

    gh run watch                      # pantau build
    gh run download -n MyLauncher-debug
    cp MyLauncher-debug/MyLauncher.apk ~/storage/downloads/

Lalu install APK dari folder Download, dan set sebagai Home default
di Pengaturan > Aplikasi default > Aplikasi layar utama.

## Rilis via tag (opsional)

    git tag v1.0 && git push --tags
    gh release download v1.0 -p MyLauncher.apk
