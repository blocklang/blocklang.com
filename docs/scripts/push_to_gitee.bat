@echo off

for /d %%d in (*) do (
    cd %%d
    echo %%d, Start push... 
    rem git remote add gitee https://gitee.com/blocklang/%%d.git
    git push gitee master
    echo ============================
    echo;
    cd ..
)