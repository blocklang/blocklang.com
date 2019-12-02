@echo off

for /f %%i in (repos.txt) do (
    if exist %%i (
        echo %%i repo exists, start pull...
        cd %%i
        git pull
        cd ..
    ) else (
        echo %%i repo not exist, start clone...
        git clone https://github.com/blocklang/%%i.git
    )
    echo ============================
    echo;
)