@echo off

for /d %%d in (*) do (
    cd %%d
    echo %%d, Start add remote gitee... 
    git remote add gitee https://gitee.com/blocklang/%%d.git
    echo ============================
    echo;
    cd ..
)